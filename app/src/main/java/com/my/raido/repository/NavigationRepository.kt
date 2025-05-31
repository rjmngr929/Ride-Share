package com.my.raido.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mapbox.mapboxsdk.geometry.LatLng
import com.my.raido.Helper.NetworkHelper
import com.my.raido.Utils.NetworkResult
import com.my.raido.api.NavigationApi
import com.my.raido.di.exception.NetworkExceptionHandler
import com.my.raido.models.response.AccessTokenDto
import com.ola.maps.mapslibrary.models.OlaLatLng
import com.ola.maps.navigation.v5.model.route.RouteInfoData
import com.ola.maps.sdk.model.reversegeocode.response.ReverseGeocodingResponse
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NavigationRepository @Inject constructor(
    private val navigationApi: NavigationApi,
    private val networkHelper: NetworkHelper,
    private val exceptionHandler: NetworkExceptionHandler
) {

    private val _locationUpdates = MutableLiveData<LatLng>()
    val locationUpdates: LiveData<LatLng> get() = _locationUpdates




//    // Fetch route from API
//    suspend fun fetchRoute(origin: OlaLatLng, destination: OlaLatLng): RouteInfoData? {
//        val originStr = "${origin.latitude},${origin.longitude}"
//        val destinationStr = "${destination.latitude},${destination.longitude}"
//        val response = navigationApi.fetchRoute(originStr, destinationStr, "B99cOWJwLYnmj1ewxa8RoZYQTqXQbfYfJlRZFrKb")
//        Log.d("TAG", "fetchRoute: navigation data is ${response.body()}")
//        return if (response.isSuccessful) {
//            response.body()
//        } else {
//            null
//        }
////        return null
//    }

//  ************************ Fetch Access Token  ***********************************************************
    val _fetchAccessTokenResponseLiveData = MutableLiveData<NetworkResult<AccessTokenDto>>()
    val fetchAccessTokenResponseLiveData: LiveData<NetworkResult<AccessTokenDto>>
        get() = _fetchAccessTokenResponseLiveData

    suspend fun fetchAccessTokenAPI(clientId: String, clientSecret: String) {
        _fetchAccessTokenResponseLiveData.postValue(NetworkResult.Loading())
        if(networkHelper.isNetworkConnected()) {
            try {

                val response = navigationApi.getAccessToken(clientId, clientSecret)
                Log.d("TAG", "fetchAccessTokenAPI: fetch access token data => ${response}")
                if (response.isSuccessful) {
                    Log.d("TAG", "fetchAccessTokenAPI: fetch access token data => ${response}")
                    handleFetchAccessTokenResponse(response)
                } else {
                    try {
                        _fetchAccessTokenResponseLiveData.postValue(
                            NetworkResult.Error(response.errorBody()?.string()
                                ?.let { JSONObject(it).getString("message") }))
                    }catch (e: Exception){
                        Log.d("TAG", "fetchAccessTokenAPI: fetch access token data => upper ${e}")
                    }
                }
            }catch (e: Exception){
                Log.d("TAG", "fetchAccessTokenAPI: fetch access token data => lower ${e}")
                _fetchAccessTokenResponseLiveData.postValue(NetworkResult.Error(exceptionHandler.handleException(e)))
            }
        }else{
            _fetchAccessTokenResponseLiveData.postValue(
                NetworkResult.Error("No internet connection" )
            )
        }
    }

    private fun handleFetchAccessTokenResponse(response: Response<AccessTokenDto>) {
        if (response.isSuccessful && response.body() != null) {
            _fetchAccessTokenResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        }
        else if(response.errorBody()!=null){
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _fetchAccessTokenResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("message")))
        }
        else{
            _fetchAccessTokenResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
        }
    }
//  ************************ Fetch Access Token  ***********************************************************


//  ************************ Fetch Route ***********************************************************
    val _fetchRouteResponseLiveData = MutableLiveData<NetworkResult<RouteInfoData>>()
    val fetchRouteResponseLiveData: LiveData<NetworkResult<RouteInfoData>>
        get() = _fetchRouteResponseLiveData

    suspend fun fetchRouteAPI(origin: OlaLatLng, destination: OlaLatLng) {
        _fetchRouteResponseLiveData.postValue(NetworkResult.Loading())
        if(networkHelper.isNetworkConnected()) {
            try {
                val originStr = "${origin.latitude},${origin.longitude}"
                val destinationStr = "${destination.latitude},${destination.longitude}"

                val response = navigationApi.fetchRoute(originStr, destinationStr, "B99cOWJwLYnmj1ewxa8RoZYQTqXQbfYfJlRZFrKb")
                Log.d("NavigationRepository", "fetchRouteAPI: fetch route data => ${response}")
                if (response.isSuccessful) {
                    Log.d("NavigationRepository", "fetchRouteAPI: fetch route data => ${response}")
                    handleFetchRouteResponse(response)
                } else {
                    try {
                        _fetchRouteResponseLiveData.postValue(
                            NetworkResult.Error(response.errorBody()?.string()
                                ?.let { JSONObject(it).getString("message") }))
                    }catch (e: Exception){
                        Log.d("NavigationRepository", "fetchRouteAPI: fetch route data => upper ${e}")
                    }
                }
            }catch (e: Exception){
                Log.d("NavigationRepository", "fetchRouteAPI: fetch route data => lower ${e}")
                _fetchRouteResponseLiveData.postValue(NetworkResult.Error(exceptionHandler.handleException(e)))
            }
        }else{
            _fetchRouteResponseLiveData.postValue(
                NetworkResult.Error("No internet connection" )
            )
        }
}

    private fun handleFetchRouteResponse(response: Response<RouteInfoData>) {
        if (response.isSuccessful && response.body() != null) {
            _fetchRouteResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        }
        else if(response.errorBody()!=null){
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _fetchRouteResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("message")))
        }
        else{
            _fetchRouteResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
        }
    }
//  ************************ Fetch Route ***********************************************************



    // Fetch district from API
//    suspend fun fetchDistrict(latlng: OlaLatLng): ReverseGeocodingResponse? {
//        try {
//            val currentLatLng = "${latlng.latitude}, ${latlng.longitude}"
//            val response = navigationApi.fetchDistrict(currentLatLng, "B99cOWJwLYnmj1ewxa8RoZYQTqXQbfYfJlRZFrKb")
////
//            return if (response.isSuccessful) {
//                response.body()
//            } else {
//                null
//            }
//        }catch (err: Exception){
//            return null
//        }
//
////        return null
//    }

//  ************************ Fetch Route ***********************************************************
    val _fetchDistrictResponseLiveData = MutableLiveData<NetworkResult<ReverseGeocodingResponse>>()
    val fetchDistrictResponseLiveData: LiveData<NetworkResult<ReverseGeocodingResponse>>
        get() = _fetchDistrictResponseLiveData

    suspend fun fetchDistrictAPI(latlng: OlaLatLng) {
        _fetchDistrictResponseLiveData.postValue(NetworkResult.Loading())
        if(networkHelper.isNetworkConnected()) {
            try {
                val currentLatLng = "${latlng.latitude}, ${latlng.longitude}"
                val response = navigationApi.fetchDistrict(currentLatLng, "B99cOWJwLYnmj1ewxa8RoZYQTqXQbfYfJlRZFrKb")
                Log.d("TAG", "fetchDistrictAPI: fetch district data => ${response}")
                if (response.isSuccessful) {
                    Log.d("TAG", "fetchDistrictAPI: fetch district data => ${response}")
                    handleFetchDistrictResponse(response)
                } else {
                    try {
                        _fetchDistrictResponseLiveData.postValue(
                            NetworkResult.Error(response.errorBody()?.string()
                                ?.let { JSONObject(it).getString("message") }))
                    }catch (e: Exception){
                        Log.d("TAG", "fetchRouteAPI: fetch district data => upper ${e}")
                    }
                }
            }catch (e: Exception){
                Log.d("TAG", "fetchRouteAPI: fetch district data => lower ${e}")
                _fetchDistrictResponseLiveData.postValue(NetworkResult.Error(exceptionHandler.handleException(e)))
            }
        }else{
            _fetchDistrictResponseLiveData.postValue(
                NetworkResult.Error("No internet connection" )
            )
        }
    }

    private fun handleFetchDistrictResponse(response: Response<ReverseGeocodingResponse>) {
        if (response.isSuccessful && response.body() != null) {
            _fetchDistrictResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        }
        else if(response.errorBody()!=null){
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _fetchDistrictResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("message")))
        }
        else{
            _fetchDistrictResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
        }
    }
//  ************************ Fetch Route ***********************************************************

}
