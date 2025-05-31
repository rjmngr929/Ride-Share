package com.my.raido.di

import android.util.Log
import com.my.raido.Utils.TokenManager
import com.my.raido.constants.SocketConstants.SOCKET_BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import java.net.URI
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SocketModule {

    private val TRANSPORTS = arrayOf(WebSocket.NAME)

    @Provides
    @Singleton
    fun provideSocket(tokenManager: TokenManager): Socket = IO.socket(URI.create(SOCKET_BASE_URL), socketOptions(tokenManager)).apply {
        on(Socket.EVENT_CONNECT_ERROR) { args ->
            Log.e("SocketModule", "Connection Error: ${args.firstOrNull()}")
        }
    }

    private fun socketOptions(tokenManager: TokenManager): IO.Options {
        val token = tokenManager.getToken()
        return IO.Options.builder()
            .setTransports(arrayOf(WebSocket.NAME))
            .setUpgrade(true)
            .setRememberUpgrade(true) //false
            .setReconnection(true)
            .setReconnectionDelay(1000)
            .setTimeout(10000)
            .setAuth(mapOf("token" to token))
            .build()
    }

}