package com.my.raido.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.my.raido.Helper.NetworkHelper
import com.my.raido.Utils.NetworkResult
import com.my.raido.api.AuthAPI
import com.my.raido.di.exception.NetworkExceptionHandler
import com.my.raido.models.login.OTPVerifyResponse
import com.my.raido.models.login.OtpVerifyRequest
import com.my.raido.models.login.toMap
import com.my.raido.models.response.ResponseModel
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class AuthRepository @Inject constructor(private val authAPI: AuthAPI, private val networkHelper: NetworkHelper, private val exceptionHandler: NetworkExceptionHandler) {


//********************** OTP Send Opertaion ******************************************
    val _otpSendResponseLiveData = MutableLiveData<NetworkResult<ResponseModel>>()
    val otpSendResponseLiveData: LiveData<NetworkResult<ResponseModel>>
        get() = _otpSendResponseLiveData

    suspend fun loginUser(mobileNumberRequest: String) {
        _otpSendResponseLiveData.postValue(NetworkResult.Loading())
        if(networkHelper.isNetworkConnected()) {
            try {
                val response = authAPI.sendOtp(mobileNumberRequest)
                if (response.isSuccessful) {
                    handleOtpSendResponse(response)
                } else {
                    try {
                        _otpSendResponseLiveData.postValue(
                            NetworkResult.Error(response.errorBody()?.string()
                                ?.let { JSONObject(it).getString("message") })
                        )
                    } catch (e: Exception) {
                        Log.d("TAG", "loginUser: error on catch part for loginUser")
                    }
                }

            } catch (e: Exception) {
                _otpSendResponseLiveData.postValue(
                    NetworkResult.Error(
                        exceptionHandler.handleException(
                            e
                        )
                    )
                )
            }
        }else{
            _otpSendResponseLiveData.postValue(
                NetworkResult.Error("No internet connection" )
            )
        }
    }

    private fun handleOtpSendResponse(response: Response<ResponseModel>) {
        if (response.isSuccessful && response.body() != null) {
            _otpSendResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        }
        else if(response.errorBody()!=null){
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _otpSendResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("message")))
        }
        else{
            _otpSendResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
        }
    }
//********************** OTP Send Opertaion ******************************************





//********************** OTP Verify Opertaion ******************************************
    private val _otpVerifyResponseLiveData = MutableLiveData<NetworkResult<OTPVerifyResponse>>()
    val otpVerifyResponseLiveData: LiveData<NetworkResult<OTPVerifyResponse>>
        get() = _otpVerifyResponseLiveData

    suspend fun verifyOtp(otpVerifyRequest: OtpVerifyRequest) {
        _otpVerifyResponseLiveData.postValue(NetworkResult.Loading())
        if(networkHelper.isNetworkConnected()) {
            try {
                val response =authAPI.verifyOtp(otpVerifyRequest.toMap())

                Log.d("TAG", "verifyOtp: response data => request ${otpVerifyRequest.toMap()}")
                Log.d("TAG", "verifyOtp: response data => ${response}")

                if (response.isSuccessful) {
                    handleVerifyOtpResponse(response)
                } else {
                    try {
    //                    Log.d("TAG", "verifyOtp: response data => ${response.errorBody()?.string()?.let { JSONObject(it).getString("message") }}")

    //                    _otpVerifyResponseLiveData.postValue(NetworkResult.Error("xyz djnkjdjkd"))
                        _otpVerifyResponseLiveData.postValue(NetworkResult.Error(
                            response.errorBody()?.string()?.let { JSONObject(it).getString("message") }
                        ))
                    }catch (e: Exception){
                        Log.d("TAG", "loginUser: error on catch part for loginUser")
                    }
                }
            }catch (e: Exception){
                Log.d("TAG", "verifyOtp: response data => catch error ${e}")
                _otpVerifyResponseLiveData.postValue(NetworkResult.Error(exceptionHandler.handleException(e)))
            }
        }else{
            _otpVerifyResponseLiveData.postValue(
                NetworkResult.Error("No internet connection" )
            )
        }
    }

    private fun handleVerifyOtpResponse(response: Response<OTPVerifyResponse>) {
        if (response.isSuccessful && response.body() != null) {
            _otpVerifyResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        }
        else if(response.errorBody()!=null){
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _otpVerifyResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("message")))
        }
        else{
            _otpVerifyResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
        }
    }
//********************** OTP Verify Opertaion ******************************************




}