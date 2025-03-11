package com.my.raido.repository

import android.util.Log
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import javax.inject.Inject

class DriverLocationRepository @Inject constructor(
    private val database: DatabaseReference
) {
    private val geoFire = GeoFire(database.child("available_drivers"))
//    private val geoFire = GeoFire(database.child("driver_locations"))

    fun updateDriverLocation(driverId: String, latitude: Double, longitude: Double, onComplete: (Boolean) -> Unit) {
        geoFire.setLocation(driverId, GeoLocation(latitude, longitude)){key, error ->
            if (error == null) {

            } else {
                onComplete(false)
            }
        }
    }

    fun getNearbyDrivers(latitude: Double, longitude: Double, radius: Double, onComplete: (List<String>) -> Unit) {
        Log.d("NearbyDrivers", "Driver ID: call ho ra hai")
        val geoQuery = geoFire.queryAtLocation(GeoLocation(latitude, longitude), radius)
        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onKeyEntered(key: String, location: GeoLocation) {
                Log.d("NearbyDrivers", "Driver ID: $key is within 2km at ${location.latitude}, ${location.longitude}")
                // A driver entered the area
                onComplete(listOf(key))
            }

            override fun onKeyExited(key: String) {
                // A driver exited the area
                Log.d("NearbyDrivers", "Driver ID: key existed")
            }

            override fun onKeyMoved(key: String, location: GeoLocation) {
                // A driver moved within the area
                Log.d("NearbyDrivers", "Driver ID: onKeyMoved")
            }

            override fun onGeoQueryReady() {
                // All drivers within the query radius have been returned
                Log.d("NearbyDrivers", "Driver ID: onGeoQueryReady")
            }

            override fun onGeoQueryError(error: DatabaseError) {
                // Handle error
                Log.d("NearbyDrivers", "Driver ID: onGeoQueryError => ${error.details}")
            }
        })
    }
}
