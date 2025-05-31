package com.my.raido.ui.viewmodels.cabViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.my.raido.Utils.NetworkResult
import com.my.raido.constants.SocketConstants
import com.my.raido.models.RecentRideDetailResponse
import com.my.raido.models.RecentRidesModel
import com.my.raido.models.cab.NearByRidersModelResponse
import com.my.raido.models.cab.RideBookRequest
import com.my.raido.models.cab.RideBookResponse
import com.my.raido.models.response.ResponseModel
import com.my.raido.repository.CabRepository
import com.my.raido.services.BookingStatus
import com.my.raido.socket.SocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class CabViewModel @Inject constructor(
    private val cabRepository: CabRepository,
    private val socketManager: SocketManager
): ViewModel(){

    init {
//        initSocketListeners()
    }

    private fun initSocketListeners() {
        socketManager.setOnSocketConnectedListener(object : SocketManager.OnSocketConnectedListener {
            override fun onConnected() {

                Log.d("CabViewModel", "onConnected: called this event...")

                socketManager.listenToEvent("welcomeMessage1") { data ->
                    Log.d("CabViewModel", "onViewCreated: welcome message 1")
                }


                socketManager.listenToEvent(SocketConstants.RIDER_RIDE_START) { data ->
                    Log.d("CabViewModel", "onViewCreated: ride started success")
                    triggerBookingOperation(BookingStatus.RIDE_STARTED)
                }

                socketManager.listenToEvent(SocketConstants.USER_INFO_RIDE_COMPLETE) { data ->
                    Log.d("CabViewModel", "onConnected: Ride completed success..")
                    if (data.getBoolean("status")) {
                        if (data.getString("message") == "Ride Complete") {

                            triggerBookingOperation(BookingStatus.RIDE_COMPLETED)

                            _rideCompleteData.postValue(data)

                        }
                    }

                }

                socketManager.listenToEvent("receive_message_by_user") { data ->
                    Log.d("CabViewModel", "onViewCreated: driver message receive to user => $data")
                }

                socketManager.listenToEvent(SocketConstants.RIDER_LOCATION_CHANGE) { response ->
                    Log.d("CabViewModel", "onCreate: driver location data is ${response}")
//                    _driveLocationData.postValue(response)
                }

            }
        })


    }

//************* Manage Booking Operations *********************************************************
    private val _statusToPerform = MutableLiveData<BookingStatus>()
    val statusToPerform: LiveData<BookingStatus> = _statusToPerform

    fun triggerBookingOperation(status: BookingStatus) {
        _statusToPerform.value = status
    }
//************* Manage Booking Operations *********************************************************

//************* Manage Booking Operations *********************************************************
    private val _rideCompleteData = MutableLiveData<JSONObject>()
    val rideCompleteData: LiveData<JSONObject> = _rideCompleteData

//************* Manage Booking Operations *********************************************************

//************* Driver Location Update *********************************************************
    private val _driveLocationData = MutableLiveData<JSONObject>()
    val driveLocationData: LiveData<JSONObject> = _driveLocationData

//************* Driver Location Update *********************************************************



//    *********************************** Set Location via marker pin *****************************************
    private val _markerLocation = MutableLiveData<LatLng>()
    val markerLocation: LiveData<LatLng> get() = _markerLocation

    // Update the marker location
    fun updateMarkerLocation(latLng: LatLng) {
        _markerLocation.value = latLng
    }

    private val _setLocationFor = MutableLiveData<String>()
    val setLocationFor: LiveData<String> get() = _setLocationFor

    fun updateLocationFor(key: String) {
        _setLocationFor.value = key
    }
//    *********************************** Set Location via marker pin *****************************************

//    ********************** Create Booking ************************************************************
    // LiveData to store user information
    private val _createBookingData = MutableLiveData<JSONObject>()
    val createBookingData: LiveData<JSONObject> get() = _createBookingData

    // Function to update user data
    fun setCreateBookingData(data: JSONObject) {
        _createBookingData.value = data
    }

//    ********************** Create Booking ************************************************************


//    ********************** Ride Data ************************************************************
    // LiveData to store user information
    private val _rideData = MutableLiveData<String?>()
    val rideData: LiveData<String?> get() = _rideData

    // Function to update user data
    fun setRiderData(data: String?) {
        _rideData.value = data
    }

//    ********************** Ride Data ************************************************************


    //    **************************** Selected Payment Mode ******************************************
    private val _selectedPaymentMode = MutableLiveData<String>().apply { value = "wallet" }// 🔹 Mutable LiveData
    val selectedPaymentMode: LiveData<String> get() = _selectedPaymentMode // 🔹 Read-Only LiveData

    fun updatePaymentMode(paymentType: String) {
        _selectedPaymentMode.postValue(paymentType) // 🔹 LiveData Update
    }
//    **************************** Selected Payment Mode ******************************************

//********************** NearBy Cabs ******************************************
    val nearByCabsResponseLiveData: LiveData<NetworkResult<NearByRidersModelResponse>>
        get() = cabRepository.nearByCabsResponseLiveData

    fun fetchCabs(){
        viewModelScope.launch {
            cabRepository.fetchDashboardDetail()
        }
    }

    fun clearProfileRes(){
        cabRepository._nearByCabsResponseLiveData.postValue(NetworkResult.Empty())
    }

//    ******************************* NearBy Cabs ***********************************************

//********************** NearBy Cabs ******************************************
    val updateNonServiceDistrictResponseLiveData: LiveData<NetworkResult<ResponseModel>>
        get() = cabRepository.nonServiceDistrictResponseLiveData

    fun updateNonServiceDistrict(district: String){
        viewModelScope.launch {
            cabRepository.updateNonServiceDistrict(district = district)
        }
    }

    fun clearNonServiceDistrictRes(){
        cabRepository._nonServiceDistrictResponseLiveData.postValue(NetworkResult.Empty())
    }

//********************** Recent Rides History ******************************************
    val recentRidesResponseLiveData: LiveData<NetworkResult<RecentRidesModel>>
        get() = cabRepository.recentRidesResponseLiveData

    fun fetchRideList(){
        viewModelScope.launch {
            cabRepository.fetchRecentRidesHistory()
        }
    }

    fun clearRes(){
        cabRepository._recentRidesResponseLiveData.postValue(NetworkResult.Empty())
    }

//********************** Recent Rides History ******************************************

//********************** Recent Ride Detail ******************************************
    val recentRideDetailResponseLiveData: LiveData<NetworkResult<RecentRideDetailResponse>>
        get() = cabRepository.recentRideDetailResponseLiveData

    fun fetchRideDetail(rideId: String){
        viewModelScope.launch {
            cabRepository.fetchRecentRideDetail(rideId)
        }
    }

    fun clearRecentRideDetailRes(){
        cabRepository._recentRideDetailResponseLiveData.postValue(NetworkResult.Empty())
    }

//********************** Recent Ride Detail ******************************************

//********************** get Ride Path with fare ******************************************
    val rideFareDetailResponseLiveData: LiveData<NetworkResult<RideBookResponse>>
        get() = cabRepository.rideFareDetailResponseLiveData

    fun fetchRideFareDetail(rideBookRequest: RideBookRequest){
        viewModelScope.launch {
            cabRepository.fetchRideFareDetail(rideBookRequest)
        }
    }

    fun clearRideFareDetailRes(){
        cabRepository._rideFareDetailResponseLiveData.postValue(NetworkResult.Empty())
    }

//********************** get Ride Path with fare ******************************************

//********************** Rate Driver ******************************************
    val rateDriverResponseLiveData: LiveData<NetworkResult<ResponseModel>>
        get() = cabRepository.ratedriverResponseLiveData

    fun rateDriverApi(rideId: String, rating: String){
        viewModelScope.launch {
            cabRepository.rateDriverApi(rideId = rideId, rating = rating)
        }
    }

    fun clearRateDriverRes(){
        cabRepository._ratedriverResponseLiveData.postValue(NetworkResult.Empty())
    }

//********************** Rate Driver ******************************************


}