package com.my.raido.ui.home.bottomsheet_fragments.ride_book.setLocationOnMap

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.model.LatLng
import com.my.raido.R
import com.my.raido.data.prefs.SharedPrefManager
import com.my.raido.databinding.ActivitySetLocationOnMapBinding
import com.my.raido.ui.viewmodels.cabViewModel.CabViewModel
import com.ola.maps.mapslibrary.listeners.OlaMapsCameraListenerManager
import com.ola.maps.mapslibrary.models.OlaMapsConfig
import com.ola.maps.mapslibrary.utils.MapTileStyle
import com.ola.maps.navigation.ui.v5.MapStatusCallback
import com.ola.maps.navigation.v5.navigation.OlaMapView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetLocationOnMapActivity : AppCompatActivity() , MapStatusCallback { //OnMapReadyCallback

    companion object{
        private const val TAG = "Set Location On Map Activity"
    }

    private lateinit var binding: ActivitySetLocationOnMapBinding

    @Inject
    lateinit var sharedPrefManager: SharedPrefManager

    private val cabViewModel: CabViewModel by viewModels()

    private lateinit var centerMarker: View

    private var focusFrom = "dest"

    private lateinit var olaMapView: OlaMapView


    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySetLocationOnMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        olaMapView = binding.googleMapMarklocation
        olaMapView.onCreate(savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Retrieve ViewModel using ViewModelProvider
//        sharedViewModel = ViewModelProvider(this)[MapSharedViewModel::class.java]

        focusFrom = intent.getStringExtra("focusFrom") ?: "dest"

        cabViewModel.updateLocationFor(focusFrom)

        centerMarker = binding.centerMarker


        val bottomSheetFragment = SetLocationOnMapFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_marklocation, bottomSheetFragment)
            .addToBackStack(null)
            .commit()

//  *********************** OlaMapSdk **********************************************************
        try {
            //call initialize function of OlaMapView with custom configuration
            olaMapView.initialize(
                mapStatusCallback = this,
                olaMapsConfig = OlaMapsConfig.Builder()
                    .setApplicationContext(applicationContext) //pass the application context here, it is mandatory
                    .setMapBaseUrl("https://api.olamaps.io") // pass the Base URL of Ola-Maps here (Stage/Prod URL), it is mandatory
                    .setApiKey("B99cOWJwLYnmj1ewxa8RoZYQTqXQbfYfJlRZFrKb")
                    .setProjectId("d9485b50-6b6c-4f42-8a7a-14bd33a758f7") //Pass the Origination ID here, it is mandatory
                    .setMapTileStyle(MapTileStyle.DEFAULT_LIGHT_STANDARD) //pass the MapTileStyle here, it is Optional.
                    .setMinZoomLevel(3.0)
                    .setMaxZoomLevel(21.0)
                    .setZoomLevel(14.0)
                    .build()
            )

            olaMapView.toggleLocationComponent(false)
        }catch (err: Exception){
            Log.d(TAG, "onCreate: error on Map Load in set location on map activity $err")
        }
//  *********************** OlaMapSdk **********************************************************


    }

    override fun onMapReady() {
        Toast.makeText(this, "Map is ready to use", Toast.LENGTH_SHORT).show()

// Set an initial camera position
//            val initialLatLng = sharedPrefManager.getLatLng(Constants.CURRENT_LATLNG) //if(::initialLatLng.isInitialized) initialLatLng else LatLng(18.517645, 73.858836)// Example coordinates
//            val currentLocation = initialLatLng?.let {
//                OlaLatLng(
//                    it.latitude,
//                    it.longitude
//                )
//            }
//            olaMapView.moveCameraToLatLong(currentLocation, 15.0, 1000)
            olaMapView.moveToCurrentLocation()

            olaMapView.setOnOlaMapsCameraIdleListener(object : OlaMapsCameraListenerManager.OnOlaMapsCameraIdleListener {
                override fun onOlaMapsCameraIdle() {
                    val centerLatLng = olaMapView.getCurrentOlaCameraPosition()?.target

                    // Fetch the latitude and longitude from the center of the map
                    val selectedLatitude = centerLatLng?.latitude
                    val selectedLongitude = centerLatLng?.longitude

                    // Perform any operation with the center location
                    Log.d("MapsActivity", "Center Location: Lat = $selectedLatitude, Lon = $selectedLongitude")

                    val currLatLng = LatLng(selectedLatitude ?: 0.0, selectedLongitude ?: 0.0)
                    // Update ViewModel or perform other operations
                    cabViewModel.updateMarkerLocation(currLatLng)
                }
            })
    }

    //call onStop() of SDK before removing the OlaMaps
    override fun onStop() {
        super.onStop()
        olaMapView?.onStop()
    }

    override fun onDestroy() {
        olaMapView?.onDestroy()
        super.onDestroy()
    }

    override fun onMapLoadFailed(p0: String?) {
        Log.d(TAG, "onMapLoadFailed: errro on map load => ${p0}")
        Toast.makeText(this, "error on map load $p0", Toast.LENGTH_SHORT).show()
    }


}