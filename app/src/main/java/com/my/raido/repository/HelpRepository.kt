package com.my.raido.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.my.raido.Helper.NetworkHelper
import com.my.raido.Utils.NetworkResult
import com.my.raido.api.HelpAPI
import com.my.raido.di.exception.NetworkExceptionHandler
import com.my.raido.models.response.help.HelpDetailModelResponse
import com.my.raido.models.response.help.HelpModelResponse
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class HelpRepository @Inject constructor(private val helpApi: HelpAPI, private val networkHelper: NetworkHelper, private val exceptionHandler: NetworkExceptionHandler) {

    //********************** Help Query Handle ******************************************
    val _helpQueryResponseLiveData = MutableLiveData<NetworkResult<HelpModelResponse>>()
    val helpQueryResponseLiveData: LiveData<NetworkResult<HelpModelResponse>>
        get() = _helpQueryResponseLiveData

    suspend fun helpQueryApi() {
        _helpQueryResponseLiveData.postValue(NetworkResult.Loading())
        if(networkHelper.isNetworkConnected()) {
            try {
                val response = helpApi.helpQueryAPI()
                Log.d("TAG", "helpQueryApi: response => ${response.body()}")
                if (response.isSuccessful) {
                    handleHelpQueryResponse(response)
                } else {
                    try {
                        Log.d("TAG", "helpQueryApi: response => ${response.errorBody()}")
                        _helpQueryResponseLiveData.postValue(
                            NetworkResult.Error(response.errorBody()?.string()
                                ?.let { JSONObject(it).getString("message") })
                        )
                    } catch (e: Exception) {
                        Log.d("TAG", "helpQueryApi: error on catch part for bookRideApi")
                    }
                }

            } catch (e: Exception) {
                _helpQueryResponseLiveData.postValue(
                    NetworkResult.Error(
                        exceptionHandler.handleException(
                            e
                        )
                    )
                )
            }
        }else{
            _helpQueryResponseLiveData.postValue(
                NetworkResult.Error("No internet connection" )
            )
        }
    }

    private fun handleHelpQueryResponse(response: Response<HelpModelResponse>) {
        if (response.isSuccessful && response.body() != null) {
            _helpQueryResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        }
        else if(response.errorBody()!=null){
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _helpQueryResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("message")))
        }
        else{
            _helpQueryResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
        }
    }

//********************** Help Query Handle ******************************************

//********************** Help Query Detail ******************************************
    val _helpQueryDetailResponseLiveData = MutableLiveData<NetworkResult<HelpDetailModelResponse>>()
    val helpQueryDetailResponseLiveData: LiveData<NetworkResult<HelpDetailModelResponse>>
        get() = _helpQueryDetailResponseLiveData

    suspend fun helpQueryDetailApi(queryId: String) {
        _helpQueryDetailResponseLiveData.postValue(NetworkResult.Loading())
        if(networkHelper.isNetworkConnected()) {
            try {
                val response = helpApi.helpQueryDetailAPI(queryId = queryId)
                Log.d("TAG", "helpQueryDetailApi: response => ${response}")
                if (response.isSuccessful) {
                    handleHelpQueryDetailResponse(response)
                } else {
                    try {
                        Log.d("TAG", "helpQueryDetailApi: response => ${response.errorBody()?.string()}")
                        _helpQueryDetailResponseLiveData.postValue(
                            NetworkResult.Error(response.errorBody()?.string()
                                ?.let { JSONObject(it).getString("message") })
                        )
                    } catch (e: Exception) {
                        Log.d("TAG", "helpQueryDetailApi: error on catch part for helpQueryDetailApi")
                        _helpQueryDetailResponseLiveData.postValue(
                            NetworkResult.Error("Something went wrong on that"))
                    }
                }

            } catch (e: Exception) {
                _helpQueryDetailResponseLiveData.postValue(
                    NetworkResult.Error(
                        exceptionHandler.handleException(
                            e
                        )
                    )
                )
            }
        }else{
            _helpQueryDetailResponseLiveData.postValue(
                NetworkResult.Error("No internet connection" )
            )
        }
    }

    private fun handleHelpQueryDetailResponse(response: Response<HelpDetailModelResponse>) {
        if (response.isSuccessful && response.body() != null) {
            _helpQueryDetailResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        }
        else if(response.errorBody()!=null){
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _helpQueryDetailResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("message")))
        }
        else{
            _helpQueryDetailResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
        }
    }

//********************** Help Query Detail ******************************************

}