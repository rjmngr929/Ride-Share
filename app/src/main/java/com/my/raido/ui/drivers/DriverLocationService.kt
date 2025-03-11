package com.my.raido.ui.drivers

import com.my.raido.models.Driver
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriverLocationService @Inject constructor() {

    fun getAvailableDrivers(): Flow<List<Driver>> = flow {
        while (true) {
            // Simulate real-time data with dummy drivers
            val dummyDrivers = listOf(
                Driver(id = "driver1", latitude = 26.228025, longitude = 73.039971, status = "available"),
                Driver(id = "driver2", latitude = 26.228629, longitude = 73.039992, status = "available"),
                Driver(id = "driver3", latitude = 26.218512, longitude = 73.041288, status = "available"),
                Driver(id = "driver4", latitude = 26.222362, longitude = 73.039786, status = "available"),
                Driver(id = "driver5", latitude = 26.220680, longitude = 73.040465, status = "available"),
            )
            emit(dummyDrivers)

            // Delay to simulate periodic updates
            delay(3000) // Emit new data every 3 seconds
        }
    }
}
