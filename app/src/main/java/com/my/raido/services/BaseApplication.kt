package com.my.raido.services

import android.app.Application
import com.my.raido.socket.SocketManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BaseApplication: Application(){

    @Inject
    lateinit var socketManager: SocketManager

    override fun onCreate() {
        super.onCreate()
    }



}