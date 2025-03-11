package com.my.raido.ui.drivers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.raido.models.Driver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriverLocationViewModel @Inject constructor(
    private val driverLocationService: DriverLocationService
) : ViewModel() {

    private val _availableDrivers = MutableLiveData<List<Driver>>()
    val availableDrivers: LiveData<List<Driver>> = _availableDrivers

    init {
        observeDriverLocations()
    }

    private fun observeDriverLocations() {
        viewModelScope.launch {
            driverLocationService.getAvailableDrivers().collect { drivers ->
                _availableDrivers.postValue(drivers)
            }
        }
    }
}
