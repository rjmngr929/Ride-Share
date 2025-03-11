package com.my.raido.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.my.raido.R
import com.my.raido.constants.Constants
import com.my.raido.data.prefs.SharedPrefManager
import com.my.raido.databinding.ActivitySetLocationOnMapBinding
import com.my.raido.ui.home.bottomsheet_fragments.SetLocationOnMapFragment
import com.ola.maps.mapslibrary.listeners.OlaMapsCameraListenerManager
import com.ola.maps.mapslibrary.models.OlaLatLng
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

//    @Inject
//    lateinit var sharedViewModel: MapSharedViewModel

    private lateinit var sharedViewModel: MapSharedViewModel

    private lateinit var map: GoogleMap

    private lateinit var source: LatLng
    private lateinit var destination: LatLng

    private val map_view_bundle_key = "My_Bundle"

    private lateinit var mapView: MapView

    private lateinit var centerMarker: View

    private lateinit var initialLatLng: LatLng

    private var focusFrom = "dest"

    private lateinit var olaMapView: OlaMapView

//    private lateinit var olaMapView: OlaMapView
//    private lateinit var olaMap: OlaMap

//    val mapControlSettings = MapControlSettings.Builder()
//        .setRotateGesturesEnabled(true)
//        .setScrollGesturesEnabled(true)
//        .setZoomGesturesEnabled(true)
//        .setCompassEnabled(false)
//        .setTiltGesturesEnabled(true)
//        .setDoubleTapGesturesEnabled(true)
//        .build()


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
        sharedViewModel = ViewModelProvider(this)[MapSharedViewModel::class.java]

        focusFrom = intent.getStringExtra("focusFrom") ?: "dest"

        sharedViewModel.updateLocationFor(focusFrom)

//        mapView = binding.googleMapMarklocation
//        var mapViewBundle: Bundle? = null
//        if (savedInstanceState != null) {
//            mapViewBundle = savedInstanceState.getBundle(map_view_bundle_key)
//        }
//        mapView.onCreate(mapViewBundle)
////        val mapFragment = binding.googleMap as SupportMapFragment
//        mapView.getMapAsync(this)

        // Center marker is a custom ImageView placed in the middle of the screen
        centerMarker = binding.centerMarker


        val bottomSheetFragment = SetLocationOnMapFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_marklocation, bottomSheetFragment)
            .addToBackStack(null)
            .commit()



//  *********************** OlaMapSdk **********************************************************
        // Initialize the OlaMapView
//        olaMapView.getMap(apiKey = "1mthwdRIjnbT77e4xJUcLserilOY5BhRtn5sQa4S",
//            olaMapCallback = object : OlaMapCallback {
//                override fun onMapReady(olaMap: OlaMap) {
//
//                    this@SetLocationOnMapActivity.olaMap = olaMap
//
//                    // Set an initial camera position
//                    val initialLatLng = sharedPrefManager.getLatLng(Constants.CURRENT_LATLNG) //if(::initialLatLng.isInitialized) initialLatLng else LatLng(18.517645, 73.858836)// Example coordinates
//                    val currentLocation = initialLatLng?.let {
//                        OlaLatLng(
//                            it.latitude,
//                            it.longitude
//                        )
//                    }
//                    olaMap.moveCameraToLatLong(currentLocation, 15.0, 1000)
//
//                    olaMap.setOnOlaMapsCameraIdleListener(object : OlaMapsCameraListenerManager.OnOlaMapsCameraIdleListener {
//                        override fun onOlaMapsCameraIdle() {
//                            val centerLatLng = olaMap.getCurrentOlaCameraPosition()?.target
//
//                            // Fetch the latitude and longitude from the center of the map
//                            val selectedLatitude = centerLatLng?.latitude
//                            val selectedLongitude = centerLatLng?.longitude
//
//                            // Perform any operation with the center location
//                            Log.d("MapsActivity", "Center Location: Lat = $selectedLatitude, Lon = $selectedLongitude")
//
//                            val currLatLng = LatLng(selectedLatitude ?: 0.0, selectedLongitude ?: 0.0)
//                            // Update ViewModel or perform other operations
//                            sharedViewModel.updateMarkerLocation(currLatLng)
//                        }
//                    })
//                }
//
//                override fun onMapError(error: String) {
//                    // Handle map error
//                }
//            },mapControlSettings
//        )

        try {
            //call onCreate function of OlaMapView after layout initialize
//            olaMapView.onCreate(savedInstanceState)

            //call initialize function of OlaMapView with custom configuration
            olaMapView.initialize(
                mapStatusCallback = this,
                olaMapsConfig = OlaMapsConfig.Builder()
                    .setApplicationContext(applicationContext) //pass the application context here, it is mandatory
                    .setMapBaseUrl("https://api.olamaps.io") // pass the Base URL of Ola-Maps here (Stage/Prod URL), it is mandatory
                    .setApiKey("1mthwdRIjnbT77e4xJUcLserilOY5BhRtn5sQa4S")
                    .setProjectId("bd6f4c73-2798-4281-93cc-eaca0bd184b5") //Pass the Origination ID here, it is mandatory
                    .setMapTileStyle(MapTileStyle.DEFAULT_LIGHT_STANDARD) //pass the MapTileStyle here, it is Optional.
                    .setMinZoomLevel(3.0)
                    .setMaxZoomLevel(21.0)
                    .setZoomLevel(14.0)
                    .build()
            )
        }catch (err: Exception){
            Log.d(TAG, "onCreate: error on Map Load in set location on map activity $err")
        }
//        ******************************************************************************************************

//  *********************** OlaMapSdk **********************************************************


    }

//    override fun onMapReady(googleMap: GoogleMap) {
//        // Enable the blue dot (My Location Layer)
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return
//        }
//
//        map = googleMap
//
//        // Set an initial camera position
//        val initialLatLng = sharedPrefManager.getLatLng(Constants.CURRENT_LATLNG) //if(::initialLatLng.isInitialized) initialLatLng else LatLng(18.517645, 73.858836)// Example coordinates
//        initialLatLng?.let { CameraUpdateFactory.newLatLngZoom(it, 15f) }
//            ?.let { map.moveCamera(it) }
//
//        // Fetch the marker location after dragging the map
//        map.setOnCameraIdleListener {
//            // Get the map's center location (LatLng)
//            val centerLatLng = map.cameraPosition.target
//
//            // Fetch the latitude and longitude from the center of the map
//            val selectedLatitude = centerLatLng.latitude
//            val selectedLongitude = centerLatLng.longitude
//
//            // Perform any operation with the center location
//            Log.d("MapsActivity", "Center Location: Lat = $selectedLatitude, Lon = $selectedLongitude")
//            sharedViewModel.updateMarkerLocation(centerLatLng)
//
//
//        }
//    }

    override fun onMapReady() {
        Toast.makeText(this, "Map is ready to use", Toast.LENGTH_SHORT).show()

// Set an initial camera position
            val initialLatLng = sharedPrefManager.getLatLng(Constants.CURRENT_LATLNG) //if(::initialLatLng.isInitialized) initialLatLng else LatLng(18.517645, 73.858836)// Example coordinates
            val currentLocation = initialLatLng?.let {
                OlaLatLng(
                    it.latitude,
                    it.longitude
                )
            }
            olaMapView.moveCameraToLatLong(currentLocation, 15.0, 1000)

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
                    sharedViewModel.updateMarkerLocation(currLatLng)
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