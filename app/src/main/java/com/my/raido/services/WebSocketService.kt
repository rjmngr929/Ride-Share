package com.my.raido.services

import com.my.raido.ui.WebSocketEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketService @Inject constructor() {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private val eventChannel = Channel<WebSocketEvent>(Channel.BUFFERED)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var retryAttempts = 0
    private val maxRetries = 3

    val events = eventChannel.receiveAsFlow()

    fun connect(url: String) {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                coroutineScope.launch { eventChannel.send(WebSocketEvent.Connected) }
                retryAttempts = 0 // Reset retry attempts on successful connection
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                coroutineScope.launch { eventChannel.send(WebSocketEvent.MessageReceived(text)) }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                coroutineScope.launch { eventChannel.send(WebSocketEvent.ConnectionFailed(t)) }
                handleReconnection(url)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                coroutineScope.launch { eventChannel.send(WebSocketEvent.Disconnected) }
            }
        })
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    private fun handleReconnection(url: String) {
        if (retryAttempts < maxRetries) {
            retryAttempts++
            coroutineScope.launch {
                delay(retryAttempts * 2000L)  // Exponential backoff for retries
                connect(url)
            }
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "Client closed connection")
        webSocket = null
    }
}
