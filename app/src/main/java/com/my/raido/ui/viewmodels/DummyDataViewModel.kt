package com.my.raido.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.my.raido.models.simulateModel.Driver
import com.my.raido.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DummyDataViewModel @Inject constructor(
    private val repository: LocationRepository
) : ViewModel() {

    fun generateDrivers(numDrivers: Int, lat:Double, lng: Double) {
        repository.generateDummyDrivers(numDrivers, lat, lng)
    }

    fun generatePassengers(numPassengers: Int) {
        repository.generateDummyPassengers(numPassengers)
    }

    fun listenForDriverUpdates(onDriverUpdate: (Driver) -> Unit) {
        repository.listenForDriverUpdates(onDriverUpdate)
    }

    fun startDriverMovementSimulation(driverId: String) {
        repository.simulateDriverMovement(driverId)
    }

    fun fetchNearbyDrivers(
        userLat: Double,
        userLng: Double,
        radiusInKm: Double,
        onResult: (List<Driver>) -> Unit
    ) {
        repository.fetchNearbyDrivers(userLat, userLng, radiusInKm, onResult)
    }
}
