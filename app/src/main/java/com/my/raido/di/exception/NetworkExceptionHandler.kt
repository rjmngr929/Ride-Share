package com.my.raido.di.exception

import android.util.Log
import okio.IOException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException

class NetworkExceptionHandler {
    fun handleException(exception: Throwable): String {
        return when (exception) {
            is ConnectException -> "Network is unreachable. Please check your connection."
            is SocketTimeoutException -> "Connection timed out. Please try again later."
            is IOException -> "An unexpected network error occurred."
            is HttpException -> handleHttpException(exception)
            else -> "An unexpected error occurred."
        }
    }

    fun handleHttpException(exception: HttpException): String {
        Log.d("TAG", "handleHttpException: error => $exception ")
        return when (exception.code()) {
            400 -> "Bad Request"
            401 -> "Unauthorized"
            403 -> "Forbidden"
            404 -> "Not Found"
            500 -> "Internal Server Error"
            else -> "HTTP error ${exception.code()}: ${exception.message()}"
        }
    }
}
