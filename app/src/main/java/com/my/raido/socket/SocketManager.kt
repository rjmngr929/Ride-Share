package com.my.raido.socket

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.my.raido.Utils.DebounceUtils
import com.my.raido.constants.SocketConstants.TRANSACTION_EVENT
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import javax.inject.Inject

class SocketManager @Inject constructor(
    private val mSocket: Socket,
    private val listeners: SocketListeners
) {


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
//        socketOn()
    }
    val onDisconnect = Emitter.Listener {
        Log.d(TAG, "SocketManager Disconnected " + mSocket.connected())
        Handler(Looper.getMainLooper()).postDelayed({
            if (!mSocket.connected()) {
                socketConnect()
            }
        }, 5000) // 5 sec delay before reconnect
    }

    val onConnectError = Emitter.Listener { args: Array<Any> ->
        Log.d(TAG, "SocketManager Error connecting..." + args[0].toString())
//        socketOff()
    }


    fun sendData(event: String, message: JSONObject) {
        mSocket.emit(event, message)
    }

    fun listenToEvent(event: String, callback: (data: JSONObject) -> Unit) {
        mSocket.on(event) { args ->
            if (args.isNotEmpty() && args[0] is JSONObject) {
                Handler(Looper.getMainLooper()).post {
                    callback(args[0] as JSONObject)
                }
            }
        }
    }


    companion object {
        private const val TAG = "SocketManager"
    }

}