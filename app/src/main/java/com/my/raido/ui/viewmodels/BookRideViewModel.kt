package com.my.raido.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.my.raido.Helper.SelectVehicle
import com.my.raido.models.RideDataModels.RideRequest
import com.my.raido.repository.DriverLocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BookRideViewModel @Inject constructor(
    private val driverLocationRepository: DriverLocationRepository,
    private val database: DatabaseReference
) : ViewModel() {

    val rideRequestStatus = MutableLiveData<String>()

    val driverId = MutableLiveData<Int?>()

    // Send a ride request from rider
    fun sendRideRequest(riderId: String, pickupLat: Double, pickupLng: Double, dropLat: Double, dropLng: Double, paymentMode: String, farePrice: String, fcmToken: String, distance: String, serviceCategory: Int, rideDuration: String, selectedvehicleType: SelectVehicle, bookingId: String, pickupAddress: String, dropAddress: String) {
        val rideRequest = RideRequest(userId =  riderId, pickupLat =  pickupLat, pickupLng =  pickupLng, pickupAddress =  pickupAddress, dropLat =  dropLat, dropLng =  dropLng, dropAddress =  dropAddress, paymentMode =  paymentMode, farePrice =  farePrice, fcmToken = fcmToken, distance =  distance, serviceCategory = serviceCategory, rideDuration =  rideDuration, selectedVehicleType =  selectedvehicleType.name, bookingId =  bookingId)

        // Create a unique ID for the ride request
        val rideRequestId = database.child("ride_requests").push().key

//        rideRequestId?.let {
//            // Save the ride request in the Firebase Realtime Database
//            database.child("ride_requests").child(it).setValue(rideRequest)
//                .addOnSuccessListener {
//                    rideRequestStatus.postValue("Request Sent")
//                    // Notify nearby drivers
//                    notifyNearbyDrivers(pickupLat, pickupLng)
//                }
//                .addOnFailureListener {
//                    rideRequestStatus.postValue("Request Failed")
//                }
//        }

        // Save the ride request in the Firebase Realtime Database
        database.child("ride_requests").child(riderId).setValue(rideRequest)
            .addOnSuccessListener {
                rideRequestStatus.postValue("Request Sent")
                Log.d("TAG", "sendRideRequest: ride request success...")
                // Notify nearby drivers
                notifyNearbyDrivers(pickupLat, pickupLng)
            }
            .addOnFailureListener {
                rideRequestStatus.postValue("Request Failed")
                Log.d("TAG", "sendRideRequest: ride request failed...")
            }

    }

    // Notify nearby drivers using GeoFire
    private fun notifyNearbyDrivers(pickupLat: Double, pickupLng: Double) {
        val radius = 2.0 // You can set the radius to your preferred value (in kilometers)

        driverLocationRepository.getNearbyDrivers(pickupLat, pickupLng, radius) { nearbyDrivers ->
            Log.d("TAG", "notifyNearbyDrivers: near by driver data => ${nearbyDrivers.size}")
            // Send notifications to the nearby drivers
//            nearbyDrivers.forEach { driverId ->
//                sendNotificationToDriver(driverId)
//            }
        }
    }

    // Send a notification to the driver
    private fun sendNotificationToDriver(driverId: String) {
        // Fetch the driver's FCM token and send a notification
        database.child("drivers").child(driverId).child("deviceToken").get()
            .addOnSuccessListener { snapshot ->
                val deviceToken = snapshot.value as? String
                deviceToken?.let {
                    // Send push notification using FCM (use your FCM service here)
//                    FirebaseMessaging.getInstance().sendRemoteMessage(mapOf(
//                        "to" to deviceToken,
//                        "data" to mapOf(
//                            "title" to "New Ride Request",
//                            "body" to "You have a new ride request. Accept or Reject it."
//                        )
//                    ))
                }
            }
    }

    fun acceptRide(userId: Int, onComplete: (String?, Int?) -> Unit){
        database.child("ride_requests").child(userId.toString()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Data exists
                    val driverIdVal = snapshot.child("driverId").getValue(Int::class.java)
                    val status = snapshot.child("status").getValue(String::class.java)
                    driverId.postValue(driverIdVal)

                    Log.d("TAG", "onDataChange: ride accepted data => $driverIdVal => $status")
                    onComplete(status, driverIdVal)
                } else {
                    // Data does not exist
                    println("Driver data not found.")
                    onComplete("error", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("TAG", "onCancelled: error occor while accepting ride")
                onComplete("error", null)
            }

        })
    }



}