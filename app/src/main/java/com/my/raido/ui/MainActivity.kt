package com.my.raido.ui

import android.Manifest
import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kotlinpermissions.PermissionStatus
import com.kotlinpermissions.allGranted
import com.kotlinpermissions.anyPermanentlyDenied
import com.kotlinpermissions.anyShouldShowRationale
import com.kotlinpermissions.extension.permissionsBuilder
import com.kotlinpermissions.request.PermissionRequest
import com.my.raido.Helper.DummyDataGenerator
import com.my.raido.Helper.LocaleHelper
import com.my.raido.R
import com.my.raido.Utils.showGrantedToast
import com.my.raido.Utils.showPermanentlyDeniedDialog
import com.my.raido.Utils.showRationaleDialog
import com.my.raido.data.prefs.SharedPrefManager
import com.my.raido.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), PermissionRequest.Listener {

    companion object{
        private const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding


    private lateinit var localeHelper: LocaleHelper

    private lateinit var sharedPreference: SharedPrefManager

    private var isHindi = false

    private var isNightModeOn = false

    private var isFirstStart = false


    private val request by lazy {
        permissionsBuilder(Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION).build()
    }

    override fun attachBaseContext(newBase: Context) {
        localeHelper = LocaleHelper()
        // Apply the saved language context before anything else
        val context = localeHelper.setLocale(newBase, if(isHindi) "hi" else "en")
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        sharedPreference = SharedPrefManager(this)

        localeHelper = LocaleHelper()

        request.addListener(this)

        isHindi = sharedPreference.getBoolean("isHindi", false)

        isNightModeOn = sharedPreference.getBoolean("isNightModeOn", false)

        isFirstStart = sharedPreference.getBoolean("isFirstStart", true)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && isFirstStart ){
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
//        } else{
//            when {
//                isNightModeOn -> {
//                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//                } else -> {
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//            }
//            }
//        }



        binding.btnTestActivityPermissions.setOnClickListener {
            request.send()
        }

        binding.btnChangeLanActivity.setOnClickListener {
            sharedPreference.putBoolean("isHindi", !isHindi )
            Log.d(TAG, "onCreate: isHindi => ${isHindi}")
            changeLanguage(if(isHindi) "hi" else "en")
        }

//        binding.btnSwitchTheme.setOnClickListener {
//            if (isNightModeOn) {
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//                sharedPreference.putBoolean("isNightModeOn", false)
//                sharedPreference.putBoolean("isFirstStart", false)
//                //recreate activity to make changes visible
//                recreate()
//            } else {
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//                sharedPreference.putBoolean("isNightModeOn", true)
//                sharedPreference.putBoolean("isFirstStart", false)
//                recreate()
//
//            }
//        }


    }


    override fun onPermissionsResult(result: List<PermissionStatus>) {
        when {
            result.anyPermanentlyDenied() -> showPermanentlyDeniedDialog(result, "Please allowed Permission")
            result.anyShouldShowRationale() -> showRationaleDialog(result, request, "Please allowed Permission")
            result.allGranted() -> showGrantedToast(result)
        }
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun changeLanguage(languageCode: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val context = localeHelper.setLocale(this@MainActivity, languageCode)
            val config = context.resources.configuration
            resources.updateConfiguration(config, resources.displayMetrics)
            recreate()  // Recreate activity to apply new language
        }

    }


}