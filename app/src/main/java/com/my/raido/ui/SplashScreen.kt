package com.my.raido.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import com.my.raido.Helper.LocaleHelper
import com.my.raido.R
import com.my.raido.Utils.AlertDialogUtility
import com.my.raido.Utils.NetworkResult
import com.my.raido.Utils.TokenManager
import com.my.raido.constants.Constants
import com.my.raido.data.prefs.SharedPrefManager
import com.my.raido.socket.SocketManager
import com.my.raido.ui.auth.AuthActivity
import com.my.raido.ui.home.HomeActivity
import com.my.raido.ui.home.bookRide.BookRideActivity
import com.my.raido.ui.viewmodels.MasterViewModel
import com.my.raido.ui.viewmodels.userViewModel.UserDataViewModel
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

    @Inject
    lateinit var sharedPrefManager: SharedPrefManager

    @Inject
    lateinit var alertDialogService: AlertDialogUtility

    @Inject
    lateinit var socketManager: SocketManager

    private val userViewModel: UserDataViewModel by viewModels()
    private val masterViewModel: MasterViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        // Apply the saved language context before anything else
        val context = LocaleHelper().setLocale(newBase, "en")
        super.attachBaseContext(context)
    }


    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)


        if(tokenManager.getToken() != null){
            userViewModel.dashboardApi()
        }else{
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }



        userViewModel.dashboardDataResponseLiveData.observe(this, Observer {
            Log.d(TAG, "dashboard: response received  => ${it}")
            when (it) {
                is NetworkResult.Success -> {


                    Log.d(TAG, "dashboard: response received => ${it}")

                    val responseData = it.data

                    Log.d(TAG, "dashboard: response received => ${responseData?.rideHistories}")

                    if(responseData != null){
                        val otpCode = responseData.rideOtp
                        val imgUrl = responseData.imgUrl
                        val walletBalance = responseData.wallet

                        masterViewModel.setWalletBalanceData(walletBalance)

                        sharedPrefManager.putString(Constants.RIDE_OTP, otpCode)
                        sharedPrefManager.putString(Constants.IMG_URL, imgUrl)



                        if(responseData.rideHistories != null){
                            socketManager.socketConnect()
                            masterViewModel.setDashboardData(responseData.rideHistories)
                            val intent = Intent(this, BookRideActivity::class.java)
                            startActivity(intent)
                            finish()
                        }else{
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }


                    userViewModel.dashboardDataResponseLiveData.removeObservers(this)
//                    userViewModel.clearDashboardRes()

                }
                is NetworkResult.Error -> {

                    alertDialogService.alertDialogAnim(
                        this,
                        it.message.toString(),
                        R.raw.failed
                    )
                    Log.d(TAG, "dashboard: response received => Error = ${it.message}")
                }
                is NetworkResult.Loading ->{
//                    binding.shimmerEffect.shimmerLayout.visible()
//                    binding.selectVehicleSection.gone()
                }
                is NetworkResult.Empty -> {
                }
            }
        })


//        if(tokenManager.getToken() != null){
//            startActivity(Intent(this, HomeActivity::class.java))
//        }else{
//            startActivity(Intent(this, AuthActivity::class.java))
//        }
//        finish()

//        startActivity(Intent(this, RiderHistoryDetailActivity::class.java))

    }


}