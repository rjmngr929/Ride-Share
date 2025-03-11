package com.my.raido.Helper

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.my.raido.models.simulateModel.Driver
import com.my.raido.models.simulateModel.Passenger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.lang.Math.cos
import java.util.Timer
import java.util.TimerTask
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class DummyDataGenerator {

    val database = FirebaseDatabase.getInstance()


    private val random = Random

    // Define bounding box for Jhalamand, Jodhpur
    private val minLatitude = 26.2068
    private val maxLatitude = 26.2321
    private val minLongitude = 72.9785
    private val maxLongitude = 73.0028


    val vehicleAry = arrayListOf("Bike", "Cabs", "Auto", "Bike Lite")

    // Generate a random location within the bounding box
    private fun getRandomLatLng(centerLat: Double, centerLng: Double, radiusInKm: Double): Pair<Double, Double> {
            val earthRadiusInKm = 6371.0

            // Convert radius from kilometers to radians
            val radiusInRadians = radiusInKm / earthRadiusInKm

            // Generate random angle and distance
            val randomAngle = 2 * Math.PI * Math.random() // Random angle in radians
            val randomDistance = sqrt(Math.random()) * radiusInRadians // Random distance in radians

            // Calculate new latitude
            val newLat = asin(
                sin(Math.toRadians(centerLat)) * cos(randomDistance) +
                        cos(Math.toRadians(centerLat)) * sin(randomDistance) * cos(randomAngle)
            )

            // Calculate new longitude
            val newLng = Math.toRadians(centerLng) + atan2(
                sin(randomAngle) * sin(randomDistance) * cos(Math.toRadians(centerLat)),
                cos(randomDistance) - sin(Math.toRadians(centerLat)) * sin(newLat)
            )

            // Convert results from radians back to degrees
            return Pair(Math.toDegrees(newLat), Math.toDegrees(newLng))
    }

    fun generateDummyDrivers(numDrivers: Int) {
        val driversRef = database.getReference("drivers")
        Log.d("TAG", "generateDummyDrivers: generate drivers => $numDrivers")
        for (i in 1..numDrivers) {

            val (latitude, longitude) = getRandomLatLng(26.237805, 73.014610, 5.0)



            // Use Google Maps Roads API to snap the generated coordinates to a road
            snapToRoad(latitude, longitude) { snappedLatLng ->
                val id = "driver_$i"
                val riderId = 5 + i
                val status = if (Random.nextBoolean()) "Available" else "Unavailable" // Random Status

                val driver = Driver(id, riderId, snappedLatLng.first, snappedLatLng.second, vehicleAry.random(), onDuty = Random.nextBoolean() ,  status = status)

                // Save to Firebase
                driversRef.child(id).setValue(driver).addOnSuccessListener {
                    Log.d("TAG", "generateDummyDrivers: on value set success => ${it}")
                }.addOnFailureListener {
                    Log.d("TAG", "generateDummyDrivers: on value set failed => ${it}")
                }.addOnCompleteListener {
                    Log.d("TAG", "generateDummyDrivers: on value set complete => ${it}")
                }
            }




//            val id = "driver_$i"
//            val riderId = 5 + i
////            val latitude = Random.nextDouble(19.0, 20.0)  // Random Latitudes
////            val longitude = Random.nextDouble(72.0, 73.0)  // Random Longitudes
//            val status = if (Random.nextBoolean()) "Available" else "Unavailable" // Random Status
//
//            val driver = Driver(id, riderId, latitude, longitude,  vehicleAry.random(), status = status)
//            driversRef.child(id).setValue(driver).addOnSuccessListener {
//                Log.d("TAG", "generateDummyDrivers: on value set success => ${it}")
//            }.addOnFailureListener {
//                Log.d("TAG", "generateDummyDrivers: on value set failed => ${it}")
//            }.addOnCompleteListener {
//                Log.d("TAG", "generateDummyDrivers: on value set complete => ${it}")
//            }



        }
    }

    fun snapToRoad(latitude: Double, longitude: Double, callback: (Pair<Double, Double>) -> Unit) {
        // Define the Google Maps Roads API URL
        val apiKey = "YOUR_GOOGLE_API_KEY"  // Replace with your actual API key
        val url = "https://roads.googleapis.com/v1/snapToRoads?path=$latitude,$longitude&key=$apiKey"

        // Use OkHttp to make the API request
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        // Run the API request in a background thread
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    // Parse the JSON response
                    val jsonResponse = JSONObject(responseBody)
                    val snappedPoints = jsonResponse.getJSONArray("snappedPoints")
                    if (snappedPoints.length() > 0) {
                        val snappedPoint = snappedPoints.getJSONObject(0)
                        val snappedLatitude = snappedPoint.getDouble("location.latitude")
                        val snappedLongitude = snappedPoint.getDouble("location.longitude")

                        // Return the snapped location
                        withContext(Dispatchers.Main) {
                            callback(Pair(snappedLatitude, snappedLongitude))
                        }
                    } else {
                        Log.d("TAG", "No snapped points found for ($latitude, $longitude)")
                    }
                } else {
                    Log.d("TAG", "Roads API call failed: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("TAG", "Error in Roads API request: ${e.message}")
            }
        }
    }

    fun generateDummyPassengers(numPassengers: Int) {
        val passengersRef = database.getReference("passengers")

        for (i in 1..numPassengers) {
            val (latitude, longitude) = getRandomLatLng(26.237805, 73.014610, 5.0)

            val id = "passenger_$i"
            val riderId = 5 + i
//            val latitude = Random.nextDouble(19.0, 20.0)  // Random Latitudes
//            val longitude = Random.nextDouble(72.0, 73.0)  // Random Longitudes

            val passenger = Passenger(id, riderId, latitude, longitude)
            passengersRef.child(id).setValue(passenger)
        }
    }

    fun listenForDriverUpdates() {
        val driversRef = database.getReference("drivers")
        driversRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (driverSnapshot in snapshot.children) {
                    val driver = driverSnapshot.getValue(Driver::class.java)
                    driver?.let {
                        // Update your UI with driver data (e.g., showing driver on the map)
                        Log.d("Driver Update", "Driver: ${it.riderId}, Location: ${it.latitude}, ${it.longitude}")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase Error", "Failed to read driver data", error.toException())
            }
        })
    }

    fun simulateDriverMovement(driverId: String) {
        val driversRef = database.getReference("drivers/$driverId")

        // Move driver every 5 seconds
        Timer().schedule(object : TimerTask() {
            override fun run() {
                val newLatitude = Random.nextDouble(19.0, 20.0)
                val newLongitude = Random.nextDouble(72.0, 73.0)

                // Update driver's location in database
                driversRef.child("latitude").setValue(newLatitude)
                driversRef.child("longitude").setValue(newLongitude)
            }
        }, 0, 5000)  // Updates every 5 seconds
    }

    fun simulateDriverOnPolyline(
        driverId: String,
        routePoints: List<LatLng>,
        intervalMs: Long = 2000 // Update interval in milliseconds
    ) {
        val driverRef = database.getReference("drivers/$driverId")

//        // Decode the polyline into a list of points
//        val routePoints = decodePolyline(polyline)
        if (routePoints.isEmpty()) {
            Log.e("Driver Simulation", "No points found in polyline")
            return
        }

        var currentIndex = 0

        Timer().schedule(object : TimerTask() {
            override fun run() {
                if (currentIndex >= routePoints.size) {
                    Log.d("Driver Simulation", "Driver $driverId reached destination")
                    this.cancel() // Stop the timer when the destination is reached
                    return
                }

//                val (latitude, longitude) = routePoints[currentIndex]
                val latitude = routePoints[currentIndex].latitude
                val longitude = routePoints[currentIndex].longitude

                // Update driver's location in Firebase
                driverRef.child("latitude").setValue(latitude)
                driverRef.child("longitude").setValue(longitude)
                    .addOnCompleteListener {
                        Log.d("Driver Simulation", "Driver $driverId updated to $latitude, $longitude")
                    }
                    .addOnFailureListener {
                        Log.e("Driver Simulation", "Failed to update driver location", it)
                    }

                currentIndex++
            }
        }, 0, intervalMs) // Start immediately and update every `intervalMs` milliseconds
    }

}
