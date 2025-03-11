package com.my.raido.ui

sealed class WebSocketEvent {
    object Connected : WebSocketEvent()
    data class MessageReceived(val message: String) : WebSocketEvent()
    data class ConnectionFailed(val error: Throwable) : WebSocketEvent()
    object Disconnected : WebSocketEvent()
}
