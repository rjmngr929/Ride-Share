package com.my.raido.Utils

import android.content.Context
import android.content.SharedPreferences
import com.my.raido.constants.Constants.PREFS_TOKEN_FILE
import com.my.raido.constants.Constants.USER_TOKEN
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class TokenManager @Inject constructor(@ApplicationContext context: Context) {
    private var prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_TOKEN_FILE, Context.MODE_PRIVATE)



    fun saveToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    fun getToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun removeToken(){
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, null)
        editor.apply()
    }
}