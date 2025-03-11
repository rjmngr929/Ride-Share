package com.my.raido.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.my.raido.models.simulateModel.Driver
import com.my.raido.models.simulateModel.Passenger
import java.lang.Math.cos
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

@Singleton
class LocationRepository @Inject constructor(
    private val database: FirebaseDatabase,
    private val databaseReference: DatabaseReference
) {

    private val vehicleAry = arrayListOf("Bike", "Cabs", "Auto", "Bike Lite")

    // Earth radius in kilometers
    private val EARTH_RADIUS_KM = 6371.0

    // Calculate the Haversine distance between two points
    private fun calculateDistance(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM * c
    }

    // Generate random lat-lng within a given radius
    private fun getRandomLatLng(centerLat: Double, centerLng: Double, radiusInKm: Double): Pair<Double, Double> {
        val earthRadiusInKm = 6371.0

        val radiusInRadians = radiusInKm / earthRadiusInKm
        val randomAngle = 2 * Math.PI * Math.random()
        val randomDistance = sqrt(Math.random()) * radiusInRadians

        val newLat = asin(
            sin(Math.toRadians(centerLat)) * cos(randomDistance) +
                    cos(Math.toRadians(centerLat)) * sin(randomDistance) * cos(randomAngle)
        )
        val newLng = Math.toRadians(centerLng) + atan2(
            sin(randomAngle) * sin(randomDistance) * cos(Math.toRadians(centerLat)),
            cos(randomDistance) - sin(Math.toRadians(centerLat)) * sin(newLat)
        )

        return Pair(Math.toDegrees(newLat), Math.toDegrees(newLng))
    }

    // Generate dummy drivers
    fun generateDummyDrivers(numDrivers: Int, lat:Double, lng: Double) {
        val driversRef = databaseReference.child("drivers")
        for (i in 1..numDrivers) {
            val (latitude, longitude) = getRandomLatLng(lat, lng, 3.0)
            val id = "driver_$i"
            val riderId = 5 + i
            val status = if (Random.nextBoolean()) "Available" else "Unavailable"

            val driver = Driver(id, riderId, latitude, longitude, vehicleAry.random(), onDuty = Random.nextBoolean(), status = status)
            driversRef.child(id).setValue(driver)
        }
    }

    // Generate dummy passengers
    fun generateDummyPassengers(numPassengers: Int) {
        val passengersRef = databaseReference.child("passengers")
        for (i in 1..numPassengers) {
            val (latitude, longitude) = getRandomLatLng(26.237805, 73.014610, 5.0)
            val id = "passenger_$i"
            val riderId = 5 + i

            val passenger = Passenger(id, riderId, latitude, longitude)
            passengersRef.child(id).setValue(passenger)
        }
    }

    // Listen for driver updates
    fun listenForDriverUpdates(onDriverUpdate: (Driver) -> Unit) {
        val driversRef = databaseReference.child("drivers")
        driversRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (driverSnapshot in snapshot.children) {
                    val driver = driverSnapshot.getValue(Driver::class.java)
                    driver?.let { onDriverUpdate(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase Error", "Failed to read driver data", error.toException())
            }
        })
    }

    // Simulate driver movement
    fun simulateDriverMovement(driverId: String) {
        val driversRef = databaseReference.child("drivers/$driverId")

        Timer().schedule(object : TimerTask() {
            override fun run() {
                val (latitude, longitude) = getRandomLatLng(26.237805, 73.014610, 5.0)
                driversRef.child("latitude").setValue(latitude)
                driversRef.child("longitude").setValue(longitude)
            }
        }, 0, 5000)
    }

    // Fetch nearby drivers
    fun fetchNearbyDrivers(
        userLat: Double,
        userLng: Double,
        radiusInKm: Double,
        onResult: (List<Driver>) -> Unit
    ) {
        val driversRef = databaseReference.child("drivers")
        val nearbyDrivers = mutableListOf<Driver>()

        driversRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (driverSnapshot in snapshot.children) {
                    val driver = driverSnapshot.getValue(Driver::class.java)
                    driver?.let {
                        val distance = calculateDistance(userLat, userLng, it.latitude, it.longitude)
                        if (distance <= radiusInKm) {
                            nearbyDrivers.add(it)
                        }
                    }
                }
                Log.d("TAG", "fetchNearbyDrivers:  driver list size => ${nearbyDrivers.size}")
                onResult(nearbyDrivers)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase Error", "Failed to fetch drivers", error.toException())
            }


        })
    }

}
