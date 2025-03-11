package com.my.raido.ui.home.bookRide

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.my.raido.Helper.SelectVehicle
import com.my.raido.R
import com.my.raido.Utils.AlertDialogUtility
import com.my.raido.constants.Constants
import com.my.raido.Utils.NetworkResult
import com.my.raido.Utils.getLoadingDialog
import com.my.raido.Utils.hideLoader
import com.my.raido.Utils.showToast
import com.my.raido.data.prefs.SharedPrefManager
import com.my.raido.databinding.ActivityBookRideBinding
import com.my.raido.models.cab.RideBookRequest
import com.my.raido.models.simulateModel.Driver
import com.my.raido.services.BookingStatus
import com.my.raido.services.ManageBooking
import com.my.raido.ui.home.MapSharedViewModel
import com.my.raido.ui.home.bottomsheet_fragments.SelectVehicleTypeFragment
import com.my.raido.ui.viewmodels.BookRideViewModel
import com.my.raido.ui.viewmodels.DummyDataViewModel
import com.my.raido.ui.viewmodels.cabViewModel.CabViewModel
import com.ola.maps.mapslibrary.models.OlaLatLng
import com.ola.maps.mapslibrary.models.OlaMapsConfig
import com.ola.maps.mapslibrary.models.OlaMarkerOptions
import com.ola.maps.mapslibrary.models.OlaPolylineOptions
import com.ola.maps.mapslibrary.utils.MapTileStyle
import com.ola.maps.navigation.ui.v5.MapStatusCallback
import com.ola.maps.navigation.v5.navigation.OlaMapView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class BookRideActivity : AppCompatActivity() , ManageBooking, MapStatusCallback { //OnMapReadyCallback

    companion object{
        private const val TAG = "Book Ride Activity"
    }

    private lateinit var binding: ActivityBookRideBinding

    private val mapSharedViewModel: MapSharedViewModel by viewModels()
    private val cabViewModel: CabViewModel by viewModels()

    @Inject
    lateinit var sharedPreference: SharedPrefManager

    @Inject
    lateinit var alertDialogService: AlertDialogUtility

    private val dummyDataViewModel: DummyDataViewModel by viewModels()

    private val bookRideViewModel: BookRideViewModel by viewModels()

    private lateinit var polyline: Polyline

    private lateinit var olaMapView: OlaMapView

//    private lateinit var map: GoogleMap
    private val driverMarkers = mutableMapOf<String, Marker>()

    private var cabAry = ArrayList<Driver>()
    private var bikeAry = ArrayList<Driver>()
    private var autoAry = ArrayList<Driver>()
    private var bikeLiteAry = ArrayList<Driver>()

    private lateinit var marker: Marker

//    private lateinit var source: PassOlaLatLng
//    private lateinit var destination: PassOlaLatLng

    private val map_view_bundle_key = "My_Bundle"

//    private lateinit var mapView: MapView

    private lateinit var loader: AlertDialog

    private var sourceAddress = ""
    private var destinationAddress = ""

    private lateinit var sourceLatLng : LatLng
    private lateinit var destinationLatLng : LatLng

    private var vehicleId by Delegates.notNull<Int>()

    private lateinit var decodedPoints:  List<LatLng>

//    private lateinit var decodedPointsOla:  List<OlaLatLng>
//
//    private lateinit var olaMapView: OlaMapView
//    private lateinit var olaMap: OlaMap
//    private lateinit var polylineOlaMap: com.ola.mapsdk.view.Polyline
//
//
//    val mapControlSettings = MapControlSettings.Builder()
//        .setRotateGesturesEnabled(false)
//        .setScrollGesturesEnabled(true)
//        .setZoomGesturesEnabled(true)
//        .setCompassEnabled(false)
//        .setTiltGesturesEnabled(false)
//        .setDoubleTapGesturesEnabled(false)
//        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookRideBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        olaMapView = binding.mapView
        olaMapView.onCreate(savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loader = getLoadingDialog(this)

//        source = sharedPreference.getLatLng(Constants.PICKUP_LOCATION)!!
//        destination = sharedPreference.getLatLng(Constants.DROP_LOCATION)!!

//        sourceAddress = intent.getStringExtra("sourceText")!!
//        destinationAddress = intent.getStringExtra("destText")!!

        sourceAddress = sharedPreference.getString(Constants.PICKUP_ADDRESS)!!
        destinationAddress = sharedPreference.getString(Constants.DROP_ADDRESS)!!

        try {
            sourceLatLng = sharedPreference.getLatLng(Constants.PICKUP_LOCATION)!!
            destinationLatLng = sharedPreference.getLatLng(Constants.DROP_LOCATION)!!
        }catch (err: Exception){
            Log.d(TAG, "onCreate: error occur on that => ${err}")
        }
   

//        source = intent.getParcelableExtra<PassOlaLatLng>("sourceText")!!
//        destination = intent.getParcelableExtra<PassOlaLatLng>("destText")!!

        //        *********************** OlaMapSdk **********************************************************
        // Initialize the OlaMapView
//        olaMapView = binding.mapView
//
//        olaMapView.getMap(apiKey = "1mthwdRIjnbT77e4xJUcLserilOY5BhRtn5sQa4S",
//            olaMapCallback = object : OlaMapCallback {
//                override fun onMapReady(olaMap: OlaMap) {
//
//                    this@BookRideActivity.olaMap = olaMap
//
//                    val currentLocation = OlaLatLng(sourceLatLng.latitude, sourceLatLng.longitude)
//                    val destinationLocation = OlaLatLng(destinationLatLng.latitude, destinationLatLng.longitude)
//
//                    olaMap.moveCameraToLatLong(currentLocation, 14.0, 1000)
//
//
//                    Handler(Looper.getMainLooper()).postDelayed({
//                        val markerOptions1 = OlaMarkerOptions.Builder()
//                            .setMarkerId("marker1")
//                            .setPosition(currentLocation)
//                            .setIconSize(0.6f)
//                            .setIsAnimationEnable(true)
//                            .build()
//                        olaMap.addMarker(markerOptions1)
//
//                        val markerOptions2 = OlaMarkerOptions.Builder()
//                            .setMarkerId("marker2")
//                            .setPosition(destinationLocation)
//                            .setIconSize(0.6f)
//                            .setIsAnimationEnable(true)
//                            .build()
//                        olaMap.addMarker(markerOptions2)
//                    }, 1000)
//
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

        }
//        *********************** OlaMapSdk **********************************************************

        val bottomSheetFragment = SelectVehicleTypeFragment()
//        val bundle = Bundle()
//        bundle.putParcelable("sourcelatLng", sourceLatLng)
//        bundle.putParcelable("destlatLng", destinationLatLng)
//        bottomSheetFragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_bookride, bottomSheetFragment)
            .addToBackStack(null)
            .commit()



        vehicleId = when(sharedPreference.getEnum(Constants.VEHICLE_TYPE, SelectVehicle::class.java ,SelectVehicle.BIKE)){
            SelectVehicle.BIKE -> {
                sharedPreference.getInt(Constants.BIKE_ID, 0)
            }
            SelectVehicle.CABS -> {
                sharedPreference.getInt(Constants.CAB_ID, 0)
            }
            SelectVehicle.AUTO -> {
                sharedPreference.getInt(Constants.AUTO_ID, 0)
            }
            else -> {
                sharedPreference.getInt(Constants.BIKE_ID, 0)
            }
        }

//        Log.d(
//            TAG, "onCreate: pickup drop location => $vehicleId and  ${
//                RideBookRequest(
//                    pickuplat = sourceLatLng.latitude,
//                    pickupLng = sourceLatLng.longitude,
//                    dropLat = destinationLatLng.latitude,
//                    dropLng = destinationLatLng.longitude,
//                    vehicleId = vehicleId,
//                    district = "jodhpur"
//                ).toMap()
//            }"
//        )


        Log.d(TAG, "onCreate: api data => ${sourceLatLng}, ${destinationLatLng}, $vehicleId")

        cabViewModel.fetchRideFareDetail(
            RideBookRequest(
                pickuplat = sourceLatLng.latitude,
                pickupLng = sourceLatLng.longitude,
                dropLat = destinationLatLng.latitude,
                dropLng = destinationLatLng.longitude,
                vehicleId = vehicleId,
                district = "jodhpur"
            )
        )

//      source = LatLng(26.284311, 73.016209)
//      destination = LatLng(26.276004, 73.008284)



//        mapView = binding.googleMap
//        var mapViewBundle: Bundle? = null
//        if (savedInstanceState != null) {
//            mapViewBundle = savedInstanceState.getBundle(map_view_bundle_key)
//        }
//        mapView.onCreate(mapViewBundle)
////        val mapFragment = binding.googleMap as SupportMapFragment
//        mapView.getMapAsync(this)






        cabViewModel.selectedVehicle.observe(this, Observer {vehicle ->
            Log.d(TAG, "bindObservers: selected vehicle is => ${vehicle} = ${bikeAry.size}")
            when(vehicle){
                SelectVehicle.BIKE -> {
                    updateMotors(bikeAry, R.drawable.bike_map)
                }
                SelectVehicle.AUTO -> {
                    updateMotors(autoAry, R.drawable.car_map)
                }
                SelectVehicle.CABS -> {
                    updateMotors(cabAry, R.drawable.ic_car)
                }
                SelectVehicle.BIKE_LITE -> {
                    updateMotors(bikeLiteAry, R.drawable.bike_map)
                }
                else -> {
                    updateMotors(bikeAry, R.drawable.bike_map)
                }
            }
        })


//        ************************************************************

//        dummyDataViewModel.listenForDriverUpdates { driver ->
//            Log.d(TAG, "bindObservers: driver data => ${driver}")
//            when(driver.vehicleType){
//                "Bike" -> {
//                    bikeAry.add(driver)
//                }
//                "Cabs" -> {
//                    cabAry.add(driver)
//                }
//                "Auto" -> {
//                    autoAry.add(driver)
//                }
//                "Bike Lite" -> {
//                    bikeLiteAry.add(driver)
//                }
//                else -> {
//                    bikeAry.add(driver)
//                }
//            }
////            updateMotors(bikeAry, R.drawable.bike_map)
//        }

        // Fetch drivers within 1 km
        dummyDataViewModel.fetchNearbyDrivers(sourceLatLng.latitude, sourceLatLng.longitude, 1.0) { nearbyDrivers ->
            bikeAry.clear()
            autoAry.clear()
            cabAry.clear()
            bikeLiteAry.clear()
            for (driver in nearbyDrivers) {
                Log.d("Nearby Driver", "Driver: ${driver.riderId}, Location: ${driver.latitude}, ${driver.longitude} vehicleType: ${driver.vehicleType}")
                when(driver.vehicleType){
                    "Bike" -> {
                        bikeAry.add(driver)
//                        if(sharedPreference.getEnum(Constants.VEHICLE_TYPE, SelectVehicle::class.java ,SelectVehicle.BIKE) == SelectVehicle.BIKE){
//                            updateMotors(bikeAry, R.drawable.bike_map)
//                        }
                    }
                    "Cabs" -> {
                        cabAry.add(driver)
//                        if(sharedPreference.getEnum(Constants.VEHICLE_TYPE, SelectVehicle::class.java ,SelectVehicle.BIKE) == SelectVehicle.CABS){
//                            updateMotors(cabAry, R.drawable.ic_car)
//                        }
                    }
                    "Auto" -> {
                        autoAry.add(driver)
//                        if(sharedPreference.getEnum(Constants.VEHICLE_TYPE, SelectVehicle::class.java ,SelectVehicle.BIKE) == SelectVehicle.AUTO){
//                            updateMotors(autoAry, R.drawable.car_map)
//                        }
                    }
                    "Bike Lite" -> {
                        bikeLiteAry.add(driver)
//                        if(sharedPreference.getEnum(Constants.VEHICLE_TYPE, SelectVehicle::class.java ,SelectVehicle.BIKE) == SelectVehicle.BIKE_LITE){
//                            updateMotors(bikeLiteAry, R.drawable.bike_map)
//                        }
                    }
                    else -> {
                        bikeAry.add(driver)
//                        if(sharedPreference.getEnum(Constants.VEHICLE_TYPE, SelectVehicle::class.java ,SelectVehicle.BIKE) == SelectVehicle.BIKE){
//                            updateMotors(bikeAry, R.drawable.bike_map)
//                        }
                    }
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                val selectedVehicle = sharedPreference.getEnum(Constants.VEHICLE_TYPE, SelectVehicle::class.java ,SelectVehicle.BIKE)

                when(selectedVehicle){
                    SelectVehicle.BIKE -> {
                        updateMotors(bikeAry, R.drawable.bike_map)
                    }
                    SelectVehicle.AUTO -> {
                        updateMotors(autoAry, R.drawable.car_map)
                    }
                    SelectVehicle.CABS -> {
                        updateMotors(cabAry, R.drawable.ic_car)
                    }
                    SelectVehicle.BIKE_LITE -> {
                        updateMotors(bikeLiteAry, R.drawable.bike_map)
                    }
                    else -> {
                        updateMotors(bikeAry, R.drawable.bike_map)
                    }
                }
            }, 500)



        }




    }

//    fun decodePolylineInBackground(encoded: String, onResult: (ArrayList<OlaLatLng>) -> Unit) {
//        // Using GlobalScope to launch the coroutine
//        GlobalScope.launch {
//            val result = withContext(Dispatchers.IO) {
//                try {
//                    decodePolylineOlaMapSdk(encoded) // Decoding polyline in the background
//                }catch (err: Exception){
//                    Log.d(TAG, "decodePolylineInBackground: error on create path => ${err}")
//                    ArrayList<OlaLatLng>()
//                }
//
//            }
//
//            // Call onResult with the result, this will run in the main thread
//            onResult(result)
//        }
//    }

    override fun onMapReady() {
        Toast.makeText(this, "Map is ready to use", Toast.LENGTH_SHORT).show()


        val markerOptionsSource = OlaMarkerOptions.Builder()
            .setMarkerId("source")
            .setPosition(OlaLatLng(sourceLatLng.latitude, sourceLatLng.longitude))
            .setIconIntRes(R.drawable.ic_location_pin)
            .setIconSize(0.1F)
            .setIsIconClickable(true)
            .setIconRotation(0f)
            .setIsAnimationEnable(true)
            .setIsInfoWindowDismissOnClick(true)
            .build()

        olaMapView.addMarkerView(markerOptionsSource)

        val markerOptionsDestination = OlaMarkerOptions.Builder()
            .setMarkerId("destination")
            .setPosition(OlaLatLng(destinationLatLng.latitude, destinationLatLng.longitude))
            .setIconIntRes(R.drawable.ic_location_pin)
            .setIconSize(0.1F)
            .setIsIconClickable(true)
            .setIconRotation(0f)
            .setIsAnimationEnable(true)
            .setIsInfoWindowDismissOnClick(true)
            .build()

        olaMapView.addMarkerView(markerOptionsDestination)


    }

    override fun onMapLoadFailed(p0: String?) {
        Log.d(TAG, "onMapLoadFailed: errro on map load => ${p0}")
        Toast.makeText(this, "error on map load $p0", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        olaMapView?.onDestroy()
        loader.dismiss()
        cabViewModel.clearRideFareDetailRes()
        super.onDestroy()

    }

    override fun onResume() {
        super.onResume()
        bindObservers()
    }

    private fun updateMotors(motorsAry: ArrayList<Driver>, motorIcon: Int) {
//        CoroutineScope(Dispatchers.IO).launch {
//            // Map for new markers
//            val updatedMarkers = mutableMapOf<String, Marker>()
//
//            // Fade out existing markers
//            withContext(Dispatchers.Main) {
//                driverMarkers.values.forEach { marker ->
//                    fadeOutMarker(marker) {
//                        marker.remove() // Remove marker after fade-out
//                    }
//                }
//            }
//
//            // If motorsAry is not empty, add or update markers
//            if (motorsAry.isNotEmpty()) {
//                motorsAry.forEach { driver ->
//                    val position = LatLng(driver.latitude.toDouble(), driver.longitude.toDouble())
//
//                    withContext(Dispatchers.Main) {
//                        val newMarker = addCustomMarker(position, motorIcon)
//                        updatedMarkers[driver.riderId.toString()] = newMarker
//                        fadeInMarker(newMarker, motorIcon)
//                    }
//                }
//            }
//
//            // Update the markers map on the main thread
//            withContext(Dispatchers.Main) {
//                driverMarkers.clear()
//                driverMarkers.putAll(updatedMarkers)
//            }
//        }
    }

//    private fun addCustomMarker(position: LatLng, motorIcon: Int): Marker {
//        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(MapUtils.getCarBitmap(this, motorIcon))
//        return map.addMarker(
//            MarkerOptions()
//                .position(position)
//                .icon(bitmapDescriptor)
//                .alpha(0f) // Start with transparent for fade-in animation
//        )!!
//    }

    private fun fadeOutMarker(marker: Marker, onComplete: () -> Unit) {
        val animator = ValueAnimator.ofFloat(1f, 0f)
        animator.duration = 500 // Duration for fade-out
        animator.addUpdateListener { valueAnimator ->
            marker.alpha = valueAnimator.animatedValue as Float
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                marker.remove() // Remove the marker after fade-out
                onComplete()
            }
        })
        animator.start()
    }

    private fun fadeInMarker(marker: Marker, motorIcon: Int) {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 500 // Duration for fade-in
        animator.addUpdateListener { valueAnimator ->
            marker.alpha = valueAnimator.animatedValue as Float
        }
        animator.start()
    }

    private fun bindObservers() {
        cabViewModel.rideFareDetailResponseLiveData.observe(this, Observer {
            Log.d(TAG, "bindObservers: response received  => ${it}")
            when (it) {
                is NetworkResult.Success -> {
//                    hideLoader(this, loader)
                    Log.d(TAG, "bindObservers: response received => ${it}")
                    val responseData = it.data

//                    cabAry.clear()
//                    autoAry.clear()
//                    bikeAry.clear()
//                    bikeLiteAry.clear()

                    if(responseData?.status == true){
                        val polylineData = responseData.drivePath ?: ""

                        Log.d(TAG, "bindObservers: polyLine Data => $${responseData.message}")
                        Log.d(TAG, "bindObservers: polyLine Data => $polylineData")

//                        val motordata = responseData.nearbyRiders


                        if(polylineData.isNotEmpty() && polylineData != "null"){

//                            drawPolyline(polylineData)

                            var polyData = decodePolylineOlaMapSdk(polylineData)


                            val polylineOptions = OlaPolylineOptions.Builder()
                                .setPolylineId("pid1")
                                .setPoints(polyData)
                                .setWidth(2f)
                                .setColor("#0000FF")
                                .build()
//
                            olaMapView.addPolyline(polylineOptions)

//                            polylineOptions.points.addAll(polyData)
                            Log.d(TAG, "bindObservers: route array length => Outer ${polyData.size} and ${::olaMapView.isInitialized}")
                            println(polyData)
                            if(::olaMapView.isInitialized) {
                                Log.d(TAG, "bindObservers: route array length => Inner ${polyData.size} and ${polylineOptions.points}")
                                olaMapView.addPolyline(polylineOptions)
                            }


//  ********************************************************************************************************************************************************
//                            if(::olaMapView.isInitialized) {
//                                val polylineOptions = OlaPolylineOptions.Builder()
//                                    .setPolylineId("pid1")
//                                    .setWidth(2f)
//                                    .setColor("#0000FF")
//                                    .build()
//
//                                val polyline = olaMapView.addPolyline(polylineOptions)
//
//                                val animator = ValueAnimator.ofInt(0, polyData.size - 1)
//                                animator.duration = 2000 // Animation duration
//                                animator.addUpdateListener { animation ->
//                                    val subList = ArrayList<OlaLatLng>()
//                                    val index = animation.animatedValue as Int
//                                    subList.addAll(polyData.subList(0, index + 1))
//                                    polyline?.setPoints(subList)
//                                }
//                                animator.start()
//                            }
//  *********************************************************************************************************************************************************


//                            GlobalScope.launch {
//                                val result = withContext(Dispatchers.IO) {
//                                    try {
//                                         decodePolylineOlaMapSdk(polylineData)
//                                    }catch (err: Exception){
//                                        Log.d(TAG, "decodePolylineInBackground: error on create path => ${err}")
//                                        ArrayList<OlaLatLng>()
//                                    }
//
//                                }
//
//                               runOnUiThread {
//
//                                   val polylineOptions = OlaPolylineOptions.Builder()
//                                       .setPolylineId("pid1")
//                                       .setPoints(result)
//                                       .setWidth(2f)
//                                       .setColor("#0000FF")
//                                       .build()
//                                   Log.d(TAG, "bindObservers: route array length => Outer ${result.size} and ${::olaMap.isInitialized}")
//                                   if(::olaMapView.isInitialized) {
//                                       Log.d(TAG, "bindObservers: route array length => Inner ${result.size} and ${polylineOptions.points}")
//                                       olaMapView.addPolyline(polylineOptions)
//                                   }
//                               }
//                            }
//
//
//
//
//                            // Decode polyline
//                            decodedPoints = decodePolyline(polylineData)
//
//                            // Initialize empty polyline
//                            polyline = olaMapView.addPolyline(PolylineOptions().color(Color.GRAY).width(10f))
//
//                            // Animate polyline
//                            animatePolyline(decodedPoints)

                        }




//                        motordata.nearbyCabs.riders.let { it1 -> cabAry.addAll(it1) }
//                        motordata.nearbyAutos.riders.let { it1 -> autoAry.addAll(it1) }
//                        motordata.nearbyBikes.riders.let { it1 -> bikeAry.addAll(it1) }
//                        motordata.nearbyBikeLite.riders.let { it1 -> bikeLiteAry.addAll(it1) }

                        Log.d(TAG, "bindObservers: ")
                        
                        cabViewModel.updateVehicleType(sharedPreference.getEnum(Constants.VEHICLE_TYPE, SelectVehicle::class.java ,SelectVehicle.BIKE))
                        cabViewModel.updateRideBookResponse(responseData)

//                        cabViewModel.clearRideFareDetailRes()
                    }else{
                        alertDialogService.alertDialogAnim(
                            this,
                            responseData?.message.toString(),
                            R.raw.failed,
                            true
                        )
                    }

                }
                is NetworkResult.Error -> {
                    hideLoader(this, loader)

                    alertDialogService.alertDialogAnim(
                        this,
                        it.message.toString(),
                        R.raw.failed
                    )
                    Log.d(TAG, "bindObservers: response received => Error = ${it.message}")
                }
                is NetworkResult.Loading ->{
//                    showLoader(this, loader)
                }
                is NetworkResult.Empty -> {
                    hideLoader(this, loader)
                }
            }
        })
    }

    fun decodePolylineOlaMapSdk(encoded: String): ArrayList<OlaLatLng> {
        val poly = ArrayList<OlaLatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        Log.d(TAG, "decodePolylineOlaMapSdk: internal function check => ${index} < $len")
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

//            poly.add(LatLng(lat / 1E5, lng / 1E5))
            poly.add(OlaLatLng(lat / 1E5, lng / 1E5))
        }
        return poly
    }

    
    fun decodePolyline(encoded: String): ArrayList<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

//            poly.add(LatLng(lat / 1E5, lng / 1E5))
            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }
        return poly
    }

    //call onStop() of SDK before removing the OlaMaps
    override fun onStop() {
        super.onStop()
        olaMapView?.onStop()
    }


//    private fun animatePolyline(points: List<LatLng>) {
//        val animator = ValueAnimator.ofInt(0, points.size - 1)
//        animator.duration = 1000 // 5 seconds
//        animator.interpolator = LinearInterpolator()
//
//        animator.addUpdateListener { animation ->
//            val endIndex = animation.animatedValue as Int
//            polyline.points = points.subList(0, endIndex + 1)
//
//            // Adjust camera dynamically to follow the polyline
//            val boundsBuilder = LatLngBounds.Builder()
//            for (i in 0..endIndex) {
//                boundsBuilder.include(points[i])
//            }
//            map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 200))
//
//            // Shift the camera position upwards to keep the polyline in the top half
//            map.setOnMapLoadedCallback {
//                val target = map.cameraPosition.target
//                val zoom = map.cameraPosition.zoom
//
//                // Adjust the target latitude by a small offset to bring it towards the top
//                val latOffset = 0 //-0.01 // Adjust based on preference, this moves the view up
//                val adjustedTarget = LatLng(target.latitude + latOffset, target.longitude)
//
//                // Update camera with the adjusted target position and current zoom level
//                map.animateCamera(CameraUpdateFactory.newLatLngZoom(adjustedTarget, zoom))
//            }
//        }
//
//        animator.start()
//    }

    @SuppressLint("CommitTransaction")
    private fun replaceFragment(currentFragment: Fragment, newFragment: Fragment){
        // Check if the current fragment exists and is attached
        val removeFragment = supportFragmentManager.findFragmentByTag(currentFragment.javaClass.simpleName) as? DialogFragment
        removeFragment?.dismissAllowingStateLoss()
//        val bottomSheetFragment = AssignDriverFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_bookride, newFragment, newFragment.javaClass.simpleName)
            .addToBackStack(null)
            .commitAllowingStateLoss()
    }

    override fun performOperation(status: BookingStatus) {
        when(status){
            BookingStatus.ACCEPTED -> {
                showToast("Ride Accepted")

//                replaceFragment(LookingForRiderFragment(), AssignDriverFragment())



//                val currentFragment = supportFragmentManager.findFragmentByTag(LookingForRiderFragment.TAG) as? LookingForRiderFragment
//                currentFragment?.dismiss()
//                val bottomSheetFragment = AssignDriverFragment()
//                supportFragmentManager.beginTransaction()
//                    .replace(R.id.fragment_container_bookride, bottomSheetFragment)
//                    .addToBackStack(null)
//                    .commitAllowingStateLoss()

                


            }
            BookingStatus.ARRIVEING -> {
                showToast("Driver arriving soon")
            }
            BookingStatus.ARRIVED -> {
                showToast("Driver arrived")
            }
            else -> {
                showToast("nothing happened")
            }
        }
    }


}