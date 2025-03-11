package com.my.raido.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapSharedViewModel @Inject constructor() : ViewModel() {

    private val _markerLocation = MutableLiveData<LatLng>()
    val markerLocation: LiveData<LatLng> get() = _markerLocation

    private val _setLocation = MutableLiveData<setAddressOnMarker>()
    val markerLocationAddress: LiveData<setAddressOnMarker> get() = _setLocation

    // Update the marker location
    fun updateMarkerLocation(latLng: LatLng) {
        _markerLocation.value = latLng
    }

    fun updateMarkerLocationAddress(address: setAddressOnMarker) {
        _setLocation.value = address
    }


    private val _setLocationFor = MutableLiveData<String>()
    val setLocationFor: LiveData<String> get() = _setLocationFor

    fun updateLocationFor(key: String) {
        _setLocationFor.value = key
    }

// ***************************** set pickup drop location ***********************************************
    private val _pickupLocation = MutableLiveData<LatLng>()
    val pickupLocation: LiveData<LatLng> get() = _pickupLocation

    // Update the marker pickup
    fun updatePickupLocation(latLng: LatLng) {
        _pickupLocation.value = latLng
    }

    private val _dropLocation = MutableLiveData<LatLng>()
    val dropLocation: LiveData<LatLng> get() = _dropLocation

    // Update the marker pickup
    fun updateDropLocation(latLng: LatLng) {
        _dropLocation.value = latLng
    }
// ***************************** set pickup drop location ***********************************************

}

data class setAddressOnMarker(val address: String, val district: String, val state: String, val pincode: String, val country: String)