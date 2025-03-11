package com.my.raido.ui.viewmodels.cabViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.my.raido.Helper.SelectVehicle
import com.my.raido.Utils.NetworkResult
import com.my.raido.models.DriverDataModel
import com.my.raido.models.RecentRideDetailResponse
import com.my.raido.models.RecentRidesModel
import com.my.raido.models.cab.NearByRidersModelResponse
import com.my.raido.models.cab.RideBookRequest
import com.my.raido.models.cab.RideBookResponse
import com.my.raido.models.response.DriverDetail
import com.my.raido.models.response.DriverDetailModel
import com.my.raido.repository.CabRepository
import com.my.raido.repository.DriverLocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@HiltViewModel
class CabViewModel @Inject constructor(
    private val cabRepository: CabRepository,
    private val database: DatabaseReference,
    private val driverLocationRepository: DriverLocationRepository,
): ViewModel(){


    // LiveData to hold the user data
    private val _selectedVehicle = MutableLiveData<SelectVehicle>()
    val selectedVehicle: LiveData<SelectVehicle> get() = _selectedVehicle

    // Method to update the user data
    fun updateVehicleType(vehicleType: SelectVehicle) {
        _selectedVehicle.value = vehicleType
    }

    // LiveData to hold the user data
    private val _driverDetails = MutableLiveData<DriverDetail>()
    val driverDetails: LiveData<DriverDetail> get() = _driverDetails

    // LiveData to hold the user data
    private val _rideBookResponse = MutableLiveData<RideBookResponse>()
    val rideBookResponse: LiveData<RideBookResponse> get() = _rideBookResponse

    // Method to update the user data
    fun updateRideBookResponse(response: RideBookResponse) {
        _rideBookResponse.value = response
    }

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

    fun fetchCabs(lat: Double, lng: Double){
        viewModelScope.launch {
            cabRepository.fetchNearbyCabs(lat = lat, lng = lng)
        }
    }

    fun clearProfileRes(){
        cabRepository._nearByCabsResponseLiveData.postValue(NetworkResult.Empty())
    }

//    ******************************* NearBy Cabs ***********************************************
    private val _nearbyDrivers = MutableLiveData<List<DriverDataModel>>()
    val nearbyDrivers: LiveData<List<DriverDataModel>> get() = _nearbyDrivers

    fun fetchDrivers(lat: Double, lng: Double){
//        val radius = 2.0 // You can set the radius to your preferred value (in kilometers)
//
//        driverLocationRepository.getNearbyDrivers(lat, lng, radius) { nearbyDrivers ->
//            Log.d("TAG", "notifyNearbyDrivers: near by driver data => ${nearbyDrivers.size}")
//            // Send notifications to the nearby drivers
////            nearbyDrivers.forEach { driverId ->
////                sendNotificationToDriver(driverId)
////            }
//        }

        viewModelScope.launch {
            cabRepository.fetchNearbyCabs(lat = lat, lng = lng)
        }

        val driversRef = database.child("available_drivers")

        driversRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nearbyDriversList = mutableListOf<DriverDataModel>()

                for (driverSnapshot in snapshot.children) {
                    val driver = driverSnapshot.getValue(DriverDataModel::class.java)

                    if (driver != null && driver.onDuty && driver.status == "Available") {
                        val distance = calculateDistance(lat, lng, driver.latitude, driver.longitude)

                        if (distance <= 2.0) { // 2 km radius filter
                            nearbyDriversList.add(driver)
                        }
                    }
                }

                // 🔹 LiveData update karega UI ko automatically
                _nearbyDrivers.postValue(nearbyDriversList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching drivers: ${error.message}")
            }
        })
    }

    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c // Distance in km
    }


//********************** NearBy Cabs ******************************************

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

//********************** get Driver Data ******************************************
    val driverDetailResponseLiveData: LiveData<NetworkResult<DriverDetailModel>>
        get() = cabRepository.driverDetailResponseLiveData

    fun fetchDriverDetail(driverId: String){
        viewModelScope.launch {
            cabRepository.fetchDriverDetail(driverId)
        }
    }

    fun updateDriverData(data : DriverDetail){
        Log.d("TAG", "updateDriverData: updated data => ${data.driverName}")
        _driverDetails.postValue(data)
    }

    fun clearDriverDetailRes(){
        cabRepository._driverDetailResponseLiveData.postValue(NetworkResult.Empty())
    }

//********************** get Driver Data ******************************************

// ******************* Assign Driver Data **********************************************************
    fun fetchDataForDriver(userId: String, onComplete: (String?) -> Unit){
        database.child("ride_requests").child("$userId").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val driverId = snapshot.child("driverId").getValue(Int::class.java)
                    Log.d("fetchDataForDriver", "Driver id is : $driverId")

                    database.child("available_drivers").child("$driverId").addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val rideHistoryId = snapshot.child("riderHistoryId").getValue(String::class.java)
                                Log.d("fetchDataForDriver", "ride history id is : $rideHistoryId")
                                onComplete(rideHistoryId)
                            } else {
                                onComplete(null)
                                Log.d("fetchDataForDriver", "No ride history found")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            onComplete(null)
                            Log.e("fetchDataForDriver", "Database Error: in ride history ${error.message}")
                        }
                    })

                } else {
                    onComplete(null)
                    Log.d("fetchDataForDriver", "No ride history id found ")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(null)
                Log.e("fetchDataForDriver", "Database Error: in driver id ${error.message}")
            }
        })
    }
// ******************* Assign Driver Data **********************************************************


}