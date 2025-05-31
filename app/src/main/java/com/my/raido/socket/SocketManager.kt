package com.my.raido.socket

import android.content.Context
import android.util.Log
import com.my.raido.Utils.DebounceUtils
import com.my.raido.Utils.DebounceUtils.handler
import com.my.raido.constants.Constants
import com.my.raido.constants.SocketConstants
import com.my.raido.constants.SocketConstants.TRANSACTION_EVENT
import com.my.raido.data.prefs.SharedPrefManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketManager @Inject constructor(
    private val mSocket: Socket,
    private val listeners: SocketListeners,
    @ApplicationContext context: Context,
    private val prefs : SharedPrefManager
) {

    private var onSocketConnectedListener: OnSocketConnectedListener? = null

    private var lastEmitTime = 0L

    private val pendingEventListeners = mutableListOf<Pair<String, (JSONObject) -> Unit>>()
    private var isUserUpdated = false

    fun setOnSocketConnectedListener(listener: OnSocketConnectedListener) {
        this.onSocketConnectedListener = listener
    }

    private fun scheduleReconnect() {
        handler.postDelayed({
            if (!mSocket.connected()) {
                socketConnect()
            }
        }, 2000) // Retry after 2 seconds
    }

    fun socketConnect() {
        if (!mSocket.connected()) {
            DebounceUtils.debounce100(object : DebounceUtils.DebounceCallback {
                override fun run() {
                    mSocket.on(Socket.EVENT_CONNECT, onConnect)
                    mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect)
                    mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
                    mSocket.connect()
                }
            })
        }
    }

    fun socketDisconnect() {
//        socketOff()
        mSocket.disconnect()
    }

    private fun socketOn() {
        socketOff()
        mSocket.on(TRANSACTION_EVENT, listeners.onTransactionsListening)
    }


    private fun socketOff() {
        mSocket.off(TRANSACTION_EVENT)
    }

    /**
     * Listeners for Connect, Disconnect & Error
     */
    val onConnect = Emitter.Listener {
        Log.d(
            TAG,
            "SocketManager isConnected " + mSocket.connected() + " |  isActive  " + mSocket.isActive
        )

        onSocketConnectedListener?.onConnected()

        var bookingId = prefs.getString(Constants.TEMP_BOOKING_ID)

        if(!bookingId.isNullOrEmpty()){
            sendData(SocketConstants.USER_UPDATE, JSONObject().apply {
                put("temp_bookings_id", bookingId)
                put("socket_id", mSocket.id())
            }) {
                Log.d(TAG, "SocketManager socket id updated on database => ${mSocket.id()}")
//                runOnUiThread {
//                    showToast(
//                        "user connect emit success!!..."
//                    )
//                }
            }


//            listenToEvent(SocketConstants.RIDER_LOCATION_CHANGE) { response ->
//                Log.d(TAG, "SocketManager rider location update => ${response}")
//            }

        }


        isUserUpdated = true

        // Now register all pending listeners
        pendingEventListeners.forEach { (event, callback) ->
            listenToEvent(event, callback)
        }
        pendingEventListeners.clear()


    }



    val onDisconnect = Emitter.Listener {
        Log.d(TAG, "SocketManager Disconnected " + mSocket.connected())
        scheduleReconnect()
    }

    val onConnectError = Emitter.Listener { args: Array<Any> ->
        Log.d(TAG, "SocketManager Error connecting..." + args[0].toString())
//        socketOff()
        scheduleReconnect()
    }

    fun isSocketConnected() : Boolean = mSocket.connected()

    fun sendData(event: String, message: JSONObject, callback: (status: Boolean) -> Unit) {
//        if (mSocket.connected()) {
//                mSocket.emit(event, message)
//                Log.d(TAG, "Data Emitted: $message on event: $event")
//            callback(true)
//        } else {
//            Log.e(TAG, "Socket is not connected! Cannot emit event: $event")
//            callback(false)
//        }


        val now = System.currentTimeMillis()
        if (event == SocketConstants.USER_UPDATE && now - lastEmitTime < 1000) {
            Log.d(TAG, "Skipped duplicate USER_UPDATE emit")
            return
        }

        lastEmitTime = now

        ensureConnected {
            mSocket.emit(event, message)
            Log.d(TAG, "Data Emitted: $message on event: $event")
            callback(true)
        }
    }

//    fun listenToEvent(event: String, callback: (data: JSONObject) -> Unit) {
//        mSocket.on(event) { args ->
//            if (args.isNotEmpty() && args[0] is JSONObject) {
//                Handler(Looper.getMainLooper()).post {
//                    callback(args[0] as JSONObject)
//                }
//            }
//        }
//    }

    fun registerSafeListener(event: String, callback: (data: JSONObject) -> Unit) {
        if (isSocketConnected() && isUserUpdated) {
            listenToEvent(event, callback)
        } else {
            pendingEventListeners.add(Pair(event, callback))
        }
    }

    fun listenToEvent(event: String, callback: (data: JSONObject) -> Unit){

        if (isSocketConnected() && isUserUpdated) {


        ensureConnected {
            mSocket.on(event) { args ->
//                Log.d(TAG, "listenToEvent: listen master on socket => ${args}")
                if (args.isNotEmpty() && args[0] is JSONObject) {
                    Log.d(TAG, "listenToEvent: listen master on socket => ${args[0] as JSONObject}")
                    handler.post { callback(args[0] as JSONObject) }
                }
            }
        }

        } else {
            pendingEventListeners.add(Pair(event, callback))
        }


    }

//    fun listenToEvent(event: String, callback: (data: JSONObject) -> Unit): Emitter.Listener {
//        val listener = Emitter.Listener { args ->
//            if (args.isNotEmpty() && args[0] is JSONObject) {
//                handler.post {
//                    callback(args[0] as JSONObject)
//                }
//            }
//        }
//
//        ensureConnected {
//            mSocket.on(event, listener)
//        }
//
//        return listener
//    }

    fun removeListener(event: String, listener: Emitter.Listener) {
        mSocket.off(event, listener)
    }


    private fun ensureConnected(action: () -> Unit) {
        if (!mSocket.connected()) {
            socketConnect()
            handler.postDelayed({
                if (mSocket.connected()) action()
            }, 1000) // Small delay to allow connection
        } else {
            action()
        }
    }

    fun getSocketId(): String? {
        return if(mSocket.connected()) mSocket.id() else null
    }

    interface OnSocketConnectedListener {
        fun onConnected()
    }

    companion object {
        private const val TAG = "SocketManager"
    }

}