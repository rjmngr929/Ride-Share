package com.my.raido.di.exception

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class NetworkInterceptor(private val exceptionHandler: NetworkExceptionHandler) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            Log.d("TAG", "intercept: request data => ${chain.request()}")
            chain.proceed(chain.request())
        } catch (e: Exception) {
            val errorMessage = exceptionHandler.handleException(e)
            throw IOException(errorMessage, e)
        }
    }
}