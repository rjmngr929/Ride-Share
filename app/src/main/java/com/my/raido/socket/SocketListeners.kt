package com.my.raido.socket

import android.util.Log
import com.my.raido.data.prefs.SharedPrefManager
import io.socket.emitter.Emitter
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject



class SocketListeners @Inject constructor(
    private val pref: SharedPrefManager
) {

    companion object {
        private const val TAG = "SocketListeners"
    }


    /**
     * This Listener will Listen for Transactions bulk update socket
     */
    var onTransactionsListening =
        Emitter.Listener { args: Array<Any> ->
            try {
                val messageJson = JSONObject(args[0].toString())
                Log.d(
                    TAG,
                    "SocketHelper setListening: json----   $messageJson"
                )
            } catch (e: JSONException) {
                Log.d(
                    TAG,
                    "SocketHelper  call: error " + e.message
                )
                e.printStackTrace()
            }
        }


    /**
     * This Listener will Listen for pre order count socket
     */

}