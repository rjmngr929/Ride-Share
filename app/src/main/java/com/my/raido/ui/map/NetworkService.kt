package com.my.raido.ui.map

import com.my.simulator.WebSocket
import com.my.simulator.WebSocketListener

class NetworkService {

    fun createWebSocketListener(webSocketListener: WebSocketListener) : WebSocket {
        return WebSocket(webSocketListener)
    }

}