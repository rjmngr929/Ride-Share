package com.my.raido.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.raido.Utils.NetworkResult
import com.my.raido.models.response.AccessTokenDto
import com.my.raido.repository.NavigationRepository
import com.ola.maps.mapslibrary.models.OlaLatLng
import com.ola.maps.navigation.v5.model.route.RouteInfoData
import com.ola.maps.sdk.model.reversegeocode.response.ReverseGeocodingResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val repository: NavigationRepository
) : ViewModel() {


//    ************************* Fetch Access Token ************************************************
private var isApiCalled = false




    val fetchAccessTokenResponseLiveData: LiveData<NetworkResult<AccessTokenDto>>
    get() = repository.fetchAccessTokenResponseLiveData

    fun fetchAccessTokenAPI(clientId: String, clientSecret: String){
        if (isApiCalled) return

        isApiCalled = true
        viewModelScope.launch {
            repository.fetchAccessTokenAPI(clientId, clientSecret)
        }
    }

    fun clearFetchAccessTokenRes(){
        repository._fetchRouteResponseLiveData.postValue(NetworkResult.Empty())
    }
//    ************************* Fetch Access Token ************************************************

//    ********************* Fetch Route ************************************************
//    private val _routeData = MutableLiveData<RouteInfoData?>()
//    val routeData: LiveData<RouteInfoData?> get() = _routeData
//
//    fun fetchRoute(origin: OlaLatLng, destination: OlaLatLng) {
//        viewModelScope.launch {
//            val routesRes = repository.fetchRoute(origin, destination)
////            Log.d("TAG", "fetchRoute: response receive for fetch route => ${routesRes?.sourceFrom}")
//            _routeData.postValue(routesRes!!)
//        }
//    }
//
//    fun clearRoute(){
//        _routeData.postValue(null)
//    }

    val fetchRouteResponseLiveData: LiveData<NetworkResult<RouteInfoData>>
        get() = repository.fetchRouteResponseLiveData

    fun fetchRouteAPI(origin: OlaLatLng, destination: OlaLatLng){
        viewModelScope.launch {
            repository.fetchRouteAPI(origin, destination)
        }
    }

    fun clearFetchRouteRes(){
        repository._fetchRouteResponseLiveData.postValue(NetworkResult.Empty())
    }


//    ********************* Fetch Route ************************************************


//    ************** Fetch District ****************************************
//    private val _geoCodingData = MutableLiveData<ReverseGeocodingResponse?>()
//    val geoCodingData: LiveData<ReverseGeocodingResponse?> get() = _geoCodingData
//
//    fun fetchDistrict(latlng: OlaLatLng) {
//        viewModelScope.launch {
//            val geoCodingRes = repository.fetchDistrict(latlng)
////            Log.d("TAG", "fetchRoute: response receive for fetch route => ${routesRes?.sourceFrom}")
//            _geoCodingData.postValue(geoCodingRes!!)
//        }
//    }

    val fetchDistrictResponseLiveData: LiveData<NetworkResult<ReverseGeocodingResponse>>
        get() = repository.fetchDistrictResponseLiveData

    fun fetchDistrictAPI(latlng: OlaLatLng){
        viewModelScope.launch {
            repository.fetchDistrictAPI(latlng)
        }
    }

    fun clearFetchDistrictRes(){
        repository._fetchDistrictResponseLiveData.postValue(NetworkResult.Empty())
    }



//    ************** Fetch District ****************************************


}
