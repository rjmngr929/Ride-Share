package com.my.raido.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.my.raido.models.response.RideHistories
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MasterViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    // LiveData to store user information
    private val _dashboardData = MutableLiveData<RideHistories?>()
    val dashboardData: LiveData<RideHistories?> get() = _dashboardData

    // Function to update user data
    fun setDashboardData(data: RideHistories?) {
        _dashboardData.value = data
    }

//************************* Wallet Balance ***************************************
    // LiveData to store user information
    private val _walletBalanceData = MutableLiveData<String>("0.00")
    val walletBalanceData: LiveData<String> get() = _walletBalanceData

    // Function to update user data
    fun setWalletBalanceData(data: String) {
        Log.d("TAG", "setWalletBalanceData: set wallet balance on master viewModel => ${data}")
        _walletBalanceData.value = data
    }
//    ************************ Wallet Balance ******************************

//************************* Cancel Ride Before Accept ***************************************
    private val _cancelRideData = MutableLiveData<Boolean>(false)
    val cancelRideData: LiveData<Boolean> get() = _cancelRideData

    // Function to update user data
    fun setCancelRideData(data: Boolean) {
        _cancelRideData.value = data
    }
//    ************************ Cancel Ride Before Accept ******************************



}
