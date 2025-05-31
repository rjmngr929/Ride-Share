package com.my.raido.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.my.raido.Helper.NetworkHelper
import com.my.raido.Utils.NetworkResult
import com.my.raido.api.CabAPI
import com.my.raido.di.exception.NetworkExceptionHandler
import com.my.raido.models.RecentRideDetailResponse
import com.my.raido.models.RecentRidesModel
import com.my.raido.models.cab.NearByRidersModelResponse
import com.my.raido.models.cab.RideBookRequest
import com.my.raido.models.cab.RideBookResponse
import com.my.raido.models.cab.toMap
import com.my.raido.models.response.ResponseModel
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class CabRepository @Inject constructor(private val cabApi: CabAPI, private val networkHelper: NetworkHelper, private val exceptionHandler: NetworkExceptionHandler) {

//********************** Near By Cabs ******************************************
    val _nearByCabsResponseLiveData = MutableLiveData<NetworkResult<NearByRidersModelResponse>>()
    val nearByCabsResponseLiveData: LiveData<NetworkResult<NearByRidersModelResponse>>
        get() = _nearByCabsResponseLiveData

    suspend fun fetchDashboardDetail() {
        _nearByCabsResponseLiveData.postValue(NetworkResult.Loading())
        if(networkHelper.isNetworkConnected()) {
            try {
    //            Log.d("TAG", "fetchNearbyCabs: response data => ${profileDetailRequest.toMap()} ")

                val response = cabApi.fetchDashboardDetail()
                Log.d("TAG", "fetchDashboardDetail:  API Called!!...$response")
                if (response.isSuccessful) {
                    handleNearByCabResponse(response)
                } else {
                    Log.d("TAG", "fetchNearbyCabs: error on response unsuccessfull => ${response.errorBody()?.string().let {
                        if (it != null) {
                            JSONObject(it)
                        }
                    }}")
                    try {
                        Log.d("TAG", "fetchNearbyCabs: error on catch part ${response.errorBody()?.string()}")
                        _nearByCabsResponseLiveData.postValue(
                            NetworkResult.Error(response.errorBody()?.string()
                                ?.let { JSONObject(it).getString("message") }))
                    }catch (e: Exception){
                        Log.d("TAG", "fetchNearbyCabs: error on catch part ${e}")
                    }
                }
            }catch (e: Exception){
                Log.d("TAG", "fetchNearbyCabs: error on catch part network => ${e}")
                _nearByCabsResponseLiveData.postValue(NetworkResult.Error(exceptionHandler.handleException(e)))
            }
        }else{
            _nearByCabsResponseLiveData.postValue(
                NetworkResult.Error("No internet connection" )
            )
        }
    }

    private fun handleNearByCabResponse(response: Response<NearByRidersModelResponse>) {
        if (response.isSuccessful && response.body() != null) {
            _nearByCabsResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        }
        else if(response.errorBody()!=null){
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _nearByCabsResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("message")))
        }
        else{
            _nearByCabsResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
        }
    }
//********************** Near By Cabs ******************************************

//********************** Update Non service District ******************************************
    val _nonServiceDistrictResponseLiveData = MutableLiveData<NetworkResult<ResponseModel>>()
    val nonServiceDistrictResponseLiveData: LiveData<NetworkResult<ResponseModel>>
        get() = _nonServiceDistrictResponseLiveData

    suspend fun updateNonServiceDistrict(district: String) {
        _nonServiceDistrictResponseLiveData.postValue(NetworkResult.Loading())
        if(networkHelper.isNetworkConnected()) {
            try {
    //            Log.d("TAG", "fetchNearbyCabs: response data => ${profileDetailRequest.toMap()} ")
                val response =cabApi.updateNonServiceDistrict(district = district)
                Log.d("TAG", "updateNonServiceDistrict: response data => $response")
                if (response.isSuccessful) {
                    handleNonServiceDistrictResponse(response)
                } else {
                    Log.d("TAG", "updateNonServiceDistrict: error on response unsuccessfull => ${response.errorBody()?.string().let {
                        if (it != null) {
                            JSONObject(it)
                        }
                    }}")
                    try {
                        _nonServiceDistrictResponseLiveData.postValue(
                            NetworkResult.Error(response.errorBody()?.string()
                                ?.let { JSONObject(it).getString("message") }))
                    }catch (e: Exception){
                        Log.d("TAG", "updateNonServiceDistrict: error on catch part ${e}")
                    }
                }
            }catch (e: Exception){
                Log.d("TAG", "updateNonServiceDistrict: error on catch part network => ${e}")
                _nonServiceDistrictResponseLiveData.postValue(NetworkResult.Error(exceptionHandler.handleException(e)))
            }
        }else{
            _nonServiceDistrictResponseLiveData.postValue(
                NetworkResult.Error("No internet connection" )
            )
        }
    }

    private fun handleNonServiceDistrictResponse(response: Response<ResponseModel>) {
        if (response.isSuccessful && response.body() != null) {
            _nonServiceDistrictResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        }
        else if(response.errorBody()!=null){
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _nonServiceDistrictResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("message")))
        }
        else{
            _nonServiceDistrictResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
        }
    }
//********************** Update Non service District ******************************************

    //********************** Recent Ride History Data ******************************************
    val _recentRidesResponseLiveData = MutableLiveData<NetworkResult<RecentRidesModel>>()
    val recentRidesResponseLiveData: LiveData<NetworkResult<RecentRidesModel>>
        get() = _recentRidesResponseLiveData

    suspend fun fetchRecentRidesHistory() {
        _recentRidesResponseLiveData.postValue(NetworkResult.Loading())
        if(networkHelper.isNetworkConnected()) {
            try {
                val response =cabApi.getRecentRides()
                if (response.isSuccessful) {
                    Log.d("TAG", "fetchRecentRidesHistory: recent Rides data => ${response}")
                    handleRecentRidesDataResponse(response)
                } else {
                    try {
                        _recentRidesResponseLiveData.postValue(
                            NetworkResult.Error(response.errorBody()?.string()
                                ?.let { JSONObject(it).getString("message") }))
                    }catch (e: Exception){
                        Log.d("TAG", "fetchRecentRidesHistory: recent Rides data => upper ${e}")
                    }
                }
            }catch (e: Exception){
                Log.d("TAG", "fetchRecentRidesHistory: recent Rides data => lower ${e}")
                _recentRidesResponseLiveData.postValue(NetworkResult.Error(exceptionHandler.handleException(e)))
            }
        }else{
            _recentRidesResponseLiveData.postValue(
                NetworkResult.Error("No internet connection" )
            )
        }
    }

    private fun handleRecentRidesDataResponse(response: Response<RecentRidesModel>) {
        if (response.isSuccessful && response.body() != null) {
            _recentRidesResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        }
        else if(response.errorBody()!=null){
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _recentRidesResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("message")))
        }
        else{
            _recentRidesResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
        }
    }
//********************** Recent Ride History Data ******************************************

    //********************** Recent Ride History Detail ******************************************
    val _recentRideDetailResponseLiveData = MutableLiveData<NetworkResult<RecentRideDetailResponse>>()
    val recentRideDetailResponseLiveData: LiveData<NetworkResult<RecentRideDetailResponse>>
        get() = _recentRideDetailResponseLiveData

    suspend fun fetchRecentRideDetail(rideId: String) {
        _recentRideDetailResponseLiveData.postValue(NetworkResult.Loading())
        if(networkHelper.isNetworkConnected()) {
            try {
                val response =cabApi.getRecentRideDetail(rideId = rideId)
                Log.d("TAG", "fetchRecentRideDetailHistory: recent Rides data => ${response}")
                if (response.isSuccessful) {
                    Log.d("TAG", "fetchRecentRideDetailHistory: recent Rides data => ${response}")
                    handleRecentRideDetailResponse(response)
                } else {
                    try {
                        _recentRideDetailResponseLiveData.postValue(
                            NetworkResult.Error(response.errorBody()?.string()
                                ?.let { JSONObject(it).getString("message") }))
                    }catch (e: Exception){
                        Log.d("TAG", "fetchRecentRideDetailHistory: recent Rides data => upper ${e}")
                    }
                }
            }catch (e: Exception){
                Log.d("TAG", "fetchRecentRideDetailHistory: recent Rides data => lower ${e}")
                _recentRideDetailResponseLiveData.postValue(NetworkResult.Error(exceptionHandler.handleException(e)))
            }
        }else{
            _recentRideDetailResponseLiveData.postValue(
                NetworkResult.Error("No internet connection" )
            )
        }
    }

    private fun handleRecentRideDetailResponse(response: Response<RecentRideDetailResponse>) {
        if (response.isSuccessful && response.body() != null) {
            _recentRideDetailResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        }
        else if(response.errorBody()!=null){
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _recentRideDetailResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("message")))
        }
        else{
            _recentRideDetailResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
        }
    }
//********************** Recent Ride History Detail ***********************************


//********************** get Ride Path with fare ******************************************
    val _rideFareDetailResponseLiveData = MutableLiveData<NetworkResult<RideBookResponse>>()
    val rideFareDetailResponseLiveData: LiveData<NetworkResult<RideBookResponse>>
        get() = _rideFareDetailResponseLiveData

    suspend fun fetchRideFareDetail(rideBookRequest: RideBookRequest) {
        _rideFareDetailResponseLiveData.postValue(NetworkResult.Loading())
        if(networkHelper.isNetworkConnected()) {
            try {
                Log.d("TAG", "fetchRideFareDetail: post data for that => ${rideBookRequest.toMap()}")
                val response =cabApi.getAvailableRiders(rideBookRequest)
                Log.d("TAG", "fetchRideFareDetail: Ride Fare data => ${response}")
                if (response.isSuccessful) {
                    Log.d("TAG", "fetchRideFareDetail: Ride Fare data => ${response.body()?.drivePath}")
                    handleRideFareDetailResponse(response)
                } else {
                    try {
                        _rideFareDetailResponseLiveData.postValue(
                            NetworkResult.Error(response.errorBody()?.string()
                                ?.let { JSONObject(it).getString("message") }))
                    }catch (e: Exception){
                        Log.d("TAG", "fetchRideFareDetail: Ride Fare data => upper ${e}")
                    }
                }
            }catch (e: Exception){
                Log.d("TAG", "fetchRideFareDetail: Ride Fare data => lower ${e}")
                _rideFareDetailResponseLiveData.postValue(NetworkResult.Error(exceptionHandler.handleException(e)))
            }
        }else{
            _rideFareDetailResponseLiveData.postValue(
                NetworkResult.Error("No internet connection" )
            )
        }
    }

    private fun handleRideFareDetailResponse(response: Response<RideBookResponse>) {
        if (response.isSuccessful && response.body() != null) {
            _rideFareDetailResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        }
        else if(response.errorBody()!=null){
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _rideFareDetailResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("message")))
        }
        else{
            _rideFareDetailResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
        }
    }
//********************** get Ride Path with fare ***********************************

//********************** Rate Driver ******************************************
    val _ratedriverResponseLiveData = MutableLiveData<NetworkResult<ResponseModel>>()
    val ratedriverResponseLiveData: LiveData<NetworkResult<ResponseModel>>
        get() = _ratedriverResponseLiveData

    suspend fun rateDriverApi(rideId: String, rating: String) {
        _ratedriverResponseLiveData.postValue(NetworkResult.Loading())
        if(networkHelper.isNetworkConnected()) {
            try {
                Log.d("TAG", "rateDriverApi: response data => ${rideId} and ${rating}")
                val response =cabApi.rateDriver(riderId = rideId, rating= rating)
                Log.d("TAG", "rateDriverApi: response data => ${response}")
                if (response.isSuccessful) {
                    handleRateDriverResponse(response)
                } else {
                    try {
                        _ratedriverResponseLiveData.postValue(
                            NetworkResult.Error(response.errorBody()?.string()
                                ?.let { JSONObject(it).getString("message") }))
                    }catch (e: Exception){
                        Log.d("TAG", "rateDriverApi: response data => upper ${e}")
                    }
                }
            }catch (e: Exception){
                Log.d("TAG", "rateDriverApi: response data => lower ${e}")
                _ratedriverResponseLiveData.postValue(NetworkResult.Error(exceptionHandler.handleException(e)))
            }
        }else{
            _ratedriverResponseLiveData.postValue(
                NetworkResult.Error("No internet connection" )
            )
        }
    }

    private fun handleRateDriverResponse(response: Response<ResponseModel>) {
        if (response.isSuccessful && response.body() != null) {
            _ratedriverResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        }
        else if(response.errorBody()!=null){
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _ratedriverResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("message")))
        }
        else{
            _ratedriverResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
        }
    }
//********************** Rate Driver ***********************************



}