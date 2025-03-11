package com.my.raido.ui.auth


import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.raido.Utils.NetworkResult
import com.my.raido.models.login.OTPVerifyResponse
import com.my.raido.models.login.OtpVerifyRequest
import com.my.raido.models.response.ResponseModel
import com.my.raido.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
): ViewModel(){

//********************** OTP Send Opertaion ******************************************
    val otpSendResponseLiveData: LiveData<NetworkResult<ResponseModel>>
        get() = authRepository.otpSendResponseLiveData


    fun sendOtp(mobileNumber: String){
        viewModelScope.launch {
            authRepository.loginUser(mobileNumber)
        }
    }

//********************** OTP Send Opertaion ******************************************

    fun clearOtpSendRes(){
        authRepository._otpSendResponseLiveData.postValue(NetworkResult.Empty())
    }

//********************** OTP Verify Opertaion ******************************************
    val otpVerifyResponseLiveData: LiveData<NetworkResult<OTPVerifyResponse>>
        get() = authRepository.otpVerifyResponseLiveData

    fun submitOtp(otpVerifyRequest: OtpVerifyRequest){
        viewModelScope.launch {
            authRepository.verifyOtp(otpVerifyRequest)
        }
    }
//********************** OTP Verify Opertaion ******************************************




}