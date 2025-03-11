package com.my.raido.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.my.raido.Helper.LocaleHelper
import com.my.raido.Utils.TokenManager
import com.my.raido.ui.auth.AuthActivity
import com.my.raido.ui.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashScreenActivity : AppCompatActivity() {

    companion object{
        private val TAG = "Splash Screen Activity"
    }

    private var deviceId : String = ""

    @Inject
    lateinit var tokenManager: TokenManager

    override fun attachBaseContext(newBase: Context) {
        // Apply the saved language context before anything else
        val context = LocaleHelper().setLocale(newBase, "en")
        super.attachBaseContext(context)
    }

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        if(tokenManager.getToken() != null){
            startActivity(Intent(this, HomeActivity::class.java))
        }else{
            startActivity(Intent(this, AuthActivity::class.java))
        }
        finish()

//        startActivity(Intent(this, RiderHistoryDetailActivity::class.java))

    }

}