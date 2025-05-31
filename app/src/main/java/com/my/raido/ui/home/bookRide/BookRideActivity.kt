package com.my.raido.ui.home.bookRide

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.PictureInPictureParams
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.Rational
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.kotlinpermissions.PermissionStatus
import com.kotlinpermissions.allGranted
import com.kotlinpermissions.anyPermanentlyDenied
import com.kotlinpermissions.anyShouldShowRationale
import com.kotlinpermissions.extension.permissionsBuilder
import com.kotlinpermissions.request.PermissionRequest
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.my.raido.Helper.SelectVehicle
import com.my.raido.R
import com.my.raido.Utils.AlertDialogUtility
import com.my.raido.Utils.AppUtils
import com.my.raido.Utils.NetworkResult
import com.my.raido.Utils.getLoadingDialog
import com.my.raido.Utils.gone
import com.my.raido.Utils.hideLoader
import com.my.raido.Utils.setOnSingleClickListener
import com.my.raido.Utils.showPermanentlyDeniedDialog
import com.my.raido.Utils.showRationaleDialog
import com.my.raido.Utils.showToast
import com.my.raido.Utils.visible
import com.my.raido.constants.Constants
import com.my.raido.constants.SocketConstants
import com.my.raido.data.prefs.SharedPrefManager
import com.my.raido.databinding.ActivityBookRideBinding
import com.my.raido.models.cab.RideBookRequest
import com.my.raido.services.BookingStatus
import com.my.raido.services.ManageBooking
import com.my.raido.socket.SocketManager
import com.my.raido.ui.home.HomeActivity
import com.my.raido.ui.home.bottomsheet_fragments.driver.AssignDriverFragment
import com.my.raido.ui.home.bottomsheet_fragments.ride_book.CancelRideFragment
import com.my.raido.ui.home.bottomsheet_fragments.ride_book.LookingForRiderFragment
import com.my.raido.ui.home.bottomsheet_fragments.ride_book.SelectVehicleTypeFragment
import com.my.raido.ui.home.bottomsheet_fragments.ride_book.cancel_ride.CancelLookingRideFragment
import com.my.raido.ui.viewmodels.MasterViewModel
import com.my.raido.ui.viewmodels.NavigationViewModel
import com.my.raido.ui.viewmodels.cabViewModel.CabViewModel
import com.ola.maps.mapslibrary.models.OlaLatLng
import com.ola.maps.mapslibrary.models.OlaMapsConfig
import com.ola.maps.mapslibrary.models.OlaMarkerOptions
import com.ola.maps.mapslibrary.utils.MapTileStyle
import com.ola.maps.navigation.ui.v5.MapStatusCallback
import com.ola.maps.navigation.v5.model.route.RouteInfoData
import com.ola.maps.navigation.v5.navigation.NavigationMapRoute
import com.ola.maps.navigation.v5.navigation.OlaMapView
import com.ola.maps.navigation.v5.navigation.direction.transform
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class BookRideActivity : AppCompatActivity() ,PermissionRequest.Listener, ManageBooking, MapStatusCallback
     { //OnMapReadyCallback  NavigationStatusCallback, RouteProgressListener

    companion object{
        private const val TAG = "Book Ride Activity"
    }

    private lateinit var binding: ActivityBookRideBinding

    private val cabViewModel: CabViewModel by viewModels()

    private val navigationViewModel: NavigationViewModel by viewModels()

    private var manageBookingOperations: ManageBooking? = null

    // initialize this variable after onMapReady callback
    private var navigationRoute: NavigationMapRoute? = null

    @Inject
    lateinit var sharedPreference: SharedPrefManager

    @Inject
    lateinit var alertDialogService: AlertDialogUtility

    @Inject
    lateinit var socketManager: SocketManager

    private var isRideStatus = BookingStatus.PENDING

     private val request by lazy {
         permissionsBuilder(Manifest.permission.ACCESS_FINE_LOCATION,
             Manifest.permission.ACCESS_COARSE_LOCATION).build()
     }

     private val GPSLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
         if(AppUtils.sharedInstance.isLocationEnabled(this)){
             request.send()
         }else{
             promptUserToEnableLocation()
         }
     }

    var accessToken = ""

    private val masterViewModel: MasterViewModel by viewModels()

    private lateinit var polylineData: String

    private lateinit var olaMapView: OlaMapView

    private var driverLatLng: OlaLatLng? = OlaLatLng()

    private lateinit var loader: AlertDialog

    private var sourceAddress = ""
    private var destinationAddress = ""

    private lateinit var sourceLatLng : LatLng
    private lateinit var destinationLatLng : LatLng

    private var vehicleId by Delegates.notNull<Int>()

    private var drawPath = false

    private var directionsRouteList  = arrayListOf<DirectionsRoute>() //  arrayListOf<DirectionsRoute>()

    private val markerViewOptions = OlaMarkerOptions.Builder()
        .setIconIntRes(R.drawable.ic_location)
        .setIconSize(0.05f)
        .build()

    private val driverMarkerViewOptions = OlaMarkerOptions.Builder()
        .setMarkerId("driver")
        .setIconIntRes(R.drawable.ic_bike_top)
        .build()

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

        request.addListener(this)

        drawPath = intent.getBooleanExtra("drawPath", false)

        manageBookingOperations = this

        masterViewModel.dashboardData.observe(this, Observer {
            Log.d(TAG, "onCreate: ${it}")

            if(it != null){

                val jsonObject = JSONObject()
                jsonObject.put("name", it.driverName)
                jsonObject.put("phone", it.driverMobile)
                jsonObject.put("dropaddress", it.dropLocation)
                jsonObject.put("pickupaddress", it.pickupLocation)
                jsonObject.put("fare", it.finalFareAfterDiscount)
                jsonObject.put("vehicle_name", it.vehicleName)
                jsonObject.put("vehicle_type", it.vehicle)
                jsonObject.put("vehicle_number", it.vehicleNumber)
                jsonObject.put("profile_picture", it.driverImage)
                jsonObject.put("ride_id", it.rideId)
                jsonObject.put("date", it.rideDate)
                jsonObject.put("newRideId", it.id)


                isRideStatus = sharedPreference.getEnum(Constants.RIDE_STATUS, BookingStatus::class.java, BookingStatus.ARRIVEING)


                cabViewModel.setRiderData(jsonObject.toString())

                val bottomSheetFragment = AssignDriverFragment()
//                bottomSheetFragment.arguments = Bundle().apply {
//                    putString("json_data", jsonObject.toString())
//                }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_bookride, bottomSheetFragment)
                    .addToBackStack(null)
                    .commit()

//                showBottomSheetSafely(bottomSheetFragment.tag.toString()){
//                    bottomSheetFragment
//                }

                when(isRideStatus) {
                    BookingStatus.RIDE_ACCEPTED -> {
                        olaMapView.removeAllMarkers()
                        showToast("Ride Accepted")
                        isRideStatus = BookingStatus.RIDE_ACCEPTED
                        sharedPreference.saveEnum(Constants.RIDE_STATUS, isRideStatus)

                    }
                    BookingStatus.ARRIVEING -> {
                        olaMapView.removeAllMarkers()
                        showToast("Driver arriving soon")
                        isRideStatus = BookingStatus.ARRIVEING
                        sharedPreference.saveEnum(Constants.RIDE_STATUS, isRideStatus)
                    }
                    BookingStatus.ARRIVED -> {
                        showToast("Driver arrived")
                        isRideStatus = BookingStatus.ARRIVED
                        sharedPreference.saveEnum(Constants.RIDE_STATUS, isRideStatus)
                    }
                    BookingStatus.RIDE_STARTED -> {
                        showToast("Ride Started")
                        isRideStatus = BookingStatus.RIDE_STARTED
                        sharedPreference.saveEnum(Constants.RIDE_STATUS, isRideStatus)
                    }
                    else -> {
                        Log.d(TAG, "onCreate: nothing can do on that")
                    }
                }
                masterViewModel.dashboardData.removeObservers(this)
            }

        })

        isRideStatus = sharedPreference.getEnum(Constants.RIDE_STATUS, BookingStatus::class.java, BookingStatus.ARRIVEING)
        manageBookingOperations?.performOperation(isRideStatus)

        if(AppUtils.sharedInstance.isLocationEnabled(this)){
            request.send()
        }else{
            promptUserToEnableLocation()
        }

        try {
            sourceAddress = sharedPreference.getString(Constants.PICKUP_ADDRESS)!!
            destinationAddress = sharedPreference.getString(Constants.DROP_ADDRESS)!!


            sourceLatLng = sharedPreference.getLatLng(Constants.PICKUP_LOCATION)!!
            destinationLatLng = sharedPreference.getLatLng(Constants.DROP_LOCATION)!!


        }catch (err: Exception){
            Log.d(TAG, "onCreate: error occur on that => ${err}")
        }



//        try {
//            //call initialize function of OlaMapView with custom configuration
//            olaMapView.initialize(
//                mapStatusCallback = this,
//                olaMapsConfig = OlaMapsConfig.Builder()
//                    .setApplicationContext(applicationContext) //pass the application context here, it is mandatory
//                    .setMapBaseUrl("https://api.olamaps.io") // pass the Base URL of Ola-Maps here (Stage/Prod URL), it is mandatory
//                    .setApiKey("B99cOWJwLYnmj1ewxa8RoZYQTqXQbfYfJlRZFrKb")
//                    .setProjectId("d9485b50-6b6c-4f42-8a7a-14bd33a758f7") //Pass the Origination ID here, it is mandatory
//                    .setMapTileStyle(MapTileStyle.DEFAULT_LIGHT_STANDARD) //pass the MapTileStyle here, it is Optional.
//                    .setMinZoomLevel(3.0)
//                    .setMaxZoomLevel(21.0)
//                    .setZoomLevel(14.0)
//                    .showCurrentLocation(false)
//                    .build()
//            )
//        }catch (err: Exception){
//
//        }


//        *********************** OlaMapSdk **********************************************************

        if(masterViewModel.dashboardData.value == null) {
            val bottomSheetFragment = SelectVehicleTypeFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_bookride, bottomSheetFragment)
                .addToBackStack(null)
                .commit()

//            val bottomSheet = SelectVehicleTypeFragment()
//            bottomSheet.show(supportFragmentManager, SelectVehicleTypeFragment.TAG)



        }

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

        val district = sharedPreference.getString(Constants.CURRENT_DISTRICT)

        if(masterViewModel.dashboardData.value == null) {
            if (!district.isNullOrEmpty()) {
                cabViewModel.fetchRideFareDetail(
                    RideBookRequest(
                        pickuplat = sourceLatLng.latitude,
                        pickupLng = sourceLatLng.longitude,
                        dropLat = destinationLatLng.latitude,
                        dropLng = destinationLatLng.longitude,
                        vehicleId = vehicleId,
                        district = district,
                        pickupAddress = sourceAddress,
                        dropAddress = destinationAddress
                    )
                )
            } else {
                getStateAndCityFromLatLng(sourceLatLng.latitude, sourceLatLng.longitude)
            }
        }

        binding.gpsLocationBtn.setOnSingleClickListener {

            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if(::olaMapView.isInitialized) {
//                    val currentLatLng = sharedPrefManager.getLatLng(Constants.CURRENT_LATLNG)
//
//                    if(currentLatLng != null){
//                        val userLatLng = OlaLatLng(currentLatLng.latitude, currentLatLng.longitude)
//                        olaMapView.moveCameraToLatLong(
//                            userLatLng,
//                            zoomLevel = 15.0,
//                            1000
//                        ) // Move camera to current location with zoom level
//
//
//
//                    }

                    olaMapView.moveToCurrentLocation()



                }
            } else {
                request.send()
            }
        }


//  ************************* Socket Listener ******************************************************

//        cabViewModel.driveLocationData.observe (this) {response ->
//            Log.d(TAG, "onCreate: driver location data is ${response}")
//            isRideStatus = sharedPreference.getEnum(
//                Constants.RIDE_STATUS,
//                BookingStatus::class.java,
//                BookingStatus.ARRIVEING
//            )
//            try {
//                driverLatLng = OlaLatLng(
//                    response.getDouble("latitude"),
//                    response.getDouble("longitude")
//                )
//
//                Log.d(TAG, "onCreate: driver location data is ${response}")
//
//                if (driverLatLng != null) {
////                    olaMapView.removeMarker("driver")
//
////                    olaMapView.addMarker(
////                        point = com.mapbox.mapboxsdk.geometry.LatLng(
////                            response.getDouble("latitude"),
////                            response.getDouble("longitude")
////                        ),
////                        icon = R.drawable.ic_bike_top,
////                        isAnimRequired = true,
////                        iconImage = "driver"
////                    )
//
//
//                    olaMapView.updateMarkerView(
//                        OlaMarkerOptions.Builder()
//                            .setMarkerId(driverMarkerViewOptions.markerId)
//                            .setPosition(
//                                OlaLatLng(
//                                    latitude = response.getDouble("latitude"),
//                                    longitude = response.getDouble("longitude")
//                                )
//                            )
//                            .setIconIntRes(driverMarkerViewOptions.iconIntRes!!)
//                            .setIconSize(driverMarkerViewOptions.iconSize)
//                            .build()
//                    )
//
//                    if (isRideStatus == BookingStatus.RIDE_ACCEPTED) {
//                        Log.d(
//                            TAG,
//                            "onCreate: rideStatus => ${isRideStatus.name} and origin = ${driverLatLng} and dest = ${
//                                OlaLatLng(
//                                    sourceLatLng.latitude,
//                                    sourceLatLng.longitude
//                                )
//                            }"
//                        )
////                        navigationViewModel.fetchRoute(
////                            driverLatLng!!,
////                            OlaLatLng(sourceLatLng.latitude, sourceLatLng.longitude)
////                        )
//
//                        setupRoute(
//                            LatLng(driverLatLng!!.latitude, driverLatLng!!.longitude),
//                            LatLng(sourceLatLng.latitude, sourceLatLng.longitude)
//                        )
////                        if(directionsRouteList.isEmpty()) { //directionsRouteList.isNullOrEmpty()
//////                            navigationViewModel.fetchRouteAPI(
//////                                driverLatLng!!,
//////                                OlaLatLng(sourceLatLng.latitude, sourceLatLng.longitude)
//////                            )
////                            setupRoute(
////                                LatLng(driverLatLng!!.latitude, driverLatLng!!.longitude),
////                                LatLng(sourceLatLng.latitude, sourceLatLng.longitude)
////                            )
////                        }
//
//                    }
//
//                    if (isRideStatus == BookingStatus.RIDE_STARTED) {
//                        Log.d(
//                            TAG,
//                            "onCreate: rideStatus => ${isRideStatus.name} and origin = ${driverLatLng} and dest = ${
//                                OlaLatLng(
//                                    sourceLatLng.latitude,
//                                    sourceLatLng.longitude
//                                )
//                            }"
//                        )
////                        navigationViewModel.fetchRoute(
////                            driverLatLng!!,
////                            OlaLatLng(destinationLatLng.latitude, destinationLatLng.longitude)
////                        )
//
//                        setupRoute(
//                            LatLng(driverLatLng!!.latitude, driverLatLng!!.longitude),
//                            LatLng(destinationLatLng.latitude, destinationLatLng.longitude)
//                        )
////                        if(directionsRouteList.isEmpty()) {
//////                            navigationViewModel.fetchRouteAPI(
//////                                driverLatLng!!,
//////                                OlaLatLng(destinationLatLng.latitude, destinationLatLng.longitude)
//////                            )
////                            setupRoute(
////                                LatLng(driverLatLng!!.latitude, driverLatLng!!.longitude),
////                                LatLng(destinationLatLng.latitude, destinationLatLng.longitude)
////                            )
////                        }
//                    }
//                }
//
//            } catch (err: Exception) {
//                Log.d(TAG, "onCreate: error occor on update driver location")
//            }
//        }

//        socketManager.setOnSocketConnectedListener(object : SocketManager.OnSocketConnectedListener {
//            override fun onConnected() {
//
//                Log.d(TAG, "onConnected: now all event triggered...!!!!")

                socketManager.listenToEvent(SocketConstants.RIDE_ACCEPT_RESPONSE) { response ->
                    Log.d(TAG, "onConnected: booking accept listen on activity $response")
                }
                
                socketManager.listenToEvent(SocketConstants.RECEIVE_NEARBY_RIDERS) { data ->
                    val riders = data.getJSONArray("data")
                    ShowDrivers(riders)
                }

                socketManager.listenToEvent(SocketConstants.RIDER_LOCATION_CHANGE) { response ->
                    Log.d(TAG, "onCreate: driver location data is ${response}")
                    isRideStatus = sharedPreference.getEnum(
                        Constants.RIDE_STATUS,
                        BookingStatus::class.java,
                        BookingStatus.ARRIVEING
                    )
                    try {
                        driverLatLng = OlaLatLng(
                            response.getDouble("latitude"),
                            response.getDouble("longitude")
                        )

                        Log.d(TAG, "onCreate: driver location data is ${response}")

                        if (driverLatLng != null) {
//                    olaMapView.removeMarker("driver")

//                    olaMapView.addMarker(
//                        point = com.mapbox.mapboxsdk.geometry.LatLng(
//                            response.getDouble("latitude"),
//                            response.getDouble("longitude")
//                        ),
//                        icon = R.drawable.ic_bike_top,
//                        isAnimRequired = true,
//                        iconImage = "driver"
//                    )


                            olaMapView.updateMarkerView(
                                OlaMarkerOptions.Builder()
                                    .setMarkerId(driverMarkerViewOptions.markerId)
                                    .setPosition(
                                        OlaLatLng(
                                            latitude = response.getDouble("latitude"),
                                            longitude = response.getDouble("longitude")
                                        )
                                    )
                                    .setIconIntRes(driverMarkerViewOptions.iconIntRes!!)
                                    .setIconSize(driverMarkerViewOptions.iconSize)
                                    .build()
                            )

                            if (isRideStatus == BookingStatus.RIDE_ACCEPTED) {
                                Log.d(
                                    TAG,
                                    "onCreate: rideStatus => ${isRideStatus.name} and origin = ${driverLatLng} and dest = ${
                                        OlaLatLng(
                                            sourceLatLng.latitude,
                                            sourceLatLng.longitude
                                        )
                                    }"
                                )
//                        navigationViewModel.fetchRoute(
//                            driverLatLng!!,
//                            OlaLatLng(sourceLatLng.latitude, sourceLatLng.longitude)
//                        )

                                setupRoute(
                                    LatLng(driverLatLng!!.latitude, driverLatLng!!.longitude),
                                    LatLng(sourceLatLng.latitude, sourceLatLng.longitude)
                                )
//                        if(directionsRouteList.isEmpty()) { //directionsRouteList.isNullOrEmpty()
////                            navigationViewModel.fetchRouteAPI(
////                                driverLatLng!!,
////                                OlaLatLng(sourceLatLng.latitude, sourceLatLng.longitude)
////                            )
//                            setupRoute(
//                                LatLng(driverLatLng!!.latitude, driverLatLng!!.longitude),
//                                LatLng(sourceLatLng.latitude, sourceLatLng.longitude)
//                            )
//                        }

                            }

                            if (isRideStatus == BookingStatus.RIDE_STARTED) {
                                Log.d(
                                    TAG,
                                    "onCreate: rideStatus => ${isRideStatus.name} and origin = ${driverLatLng} and dest = ${
                                        OlaLatLng(
                                            sourceLatLng.latitude,
                                            sourceLatLng.longitude
                                        )
                                    }"
                                )
//                        navigationViewModel.fetchRoute(
//                            driverLatLng!!,
//                            OlaLatLng(destinationLatLng.latitude, destinationLatLng.longitude)
//                        )

                                setupRoute(
                                    LatLng(driverLatLng!!.latitude, driverLatLng!!.longitude),
                                    LatLng(destinationLatLng.latitude, destinationLatLng.longitude)
                                )
//                        if(directionsRouteList.isEmpty()) {
////                            navigationViewModel.fetchRouteAPI(
////                                driverLatLng!!,
////                                OlaLatLng(destinationLatLng.latitude, destinationLatLng.longitude)
////                            )
//                            setupRoute(
//                                LatLng(driverLatLng!!.latitude, driverLatLng!!.longitude),
//                                LatLng(destinationLatLng.latitude, destinationLatLng.longitude)
//                            )
//                        }
                            }
                        }

                    } catch (err: Exception) {
                        Log.d(TAG, "onCreate: error occor on update driver location")
                    }


                }

                socketManager.listenToEvent(SocketConstants.RIDE_CANCELLED) {
                    startActivity(Intent(this@BookRideActivity, HomeActivity::class.java))
                    finish()
                }
//            }
//        })

//  ************************* Socket Listener ******************************************************

//        navigationViewModel.routeData.observe(this) { DirectionRoute ->
//            if(DirectionRoute != null) {
//                if (::navigationRoute.isInitialized) {
//                    navigationRoute.removeRoute()
//                    directionsRouteList.clear()
//
//                    directionsRouteList.add(transform(DirectionRoute))
//
////            try {
////                val angle = calculateAngle(driverLatLng!!, OlaLatLng(sourceLatLng.latitude, sourceLatLng.longitude) ) // (new, this side set previous latlng)
////
////                val markerOptions = OlaMarkerOptions.Builder()
////                    .setMarkerId("marker1")
////                    .setIconSize(0.6f)
////                    .setIconRotation(angle)
////                    .setIconBitmap(MapUtils.getCarBitmap(this, R.drawable.bike_map))
////                    .setIsAnimationEnable(true)
////                    .build()
////                olaMapView.moveMarkerBetweenPoints(srcOlaLatLng = driverLatLng!!, enOlaLatLng =  OlaLatLng(sourceLatLng.latitude, sourceLatLng.longitude), olaMarkerOptions = markerOptions, 1000 )
////            }catch (err: Exception){
////                Log.d(TAG, "onMapReady: error => $err")
////            }
//
//                    Log.d(
//                        TAG,
//                        "onMapReady: directionsRouteList data size => ${directionsRouteList.size}"
//                    )
//
//                    if (directionsRouteList.isNotEmpty()){
//                        navigationRoute?.addRoutesForRoutePreview(directionsRouteList)
//                    }
//                }
//            }
//        }

        navigationViewModel.fetchRouteResponseLiveData.observe(this, Observer {
            Log.d(TAG, "bindObservers: response received  => ${it}")
            when (it) {
                is NetworkResult.Success -> {

                    hideLoader(this, loader)

                    val directionRoute = it.data

                    if(directionRoute != null) {

                        showRoute(routeInfoData = directionRoute, destinationLatLng)

//                        if (::navigationRoute.isInitialized) {
//                            navigationRoute.removeRoute()
//                            directionsRouteList.clear()
//
//                            lifecycleScope.launch(Dispatchers.IO) {
//                                directionsRouteList.add(transform(directionRoute))
//                            }
//
//
//
////            try {
////                val angle = calculateAngle(driverLatLng!!, OlaLatLng(sourceLatLng.latitude, sourceLatLng.longitude) ) // (new, this side set previous latlng)
////
////                val markerOptions = OlaMarkerOptions.Builder()
////                    .setMarkerId("marker1")
////                    .setIconSize(0.6f)
////                    .setIconRotation(angle)
////                    .setIconBitmap(MapUtils.getCarBitmap(this, R.drawable.bike_map))
////                    .setIsAnimationEnable(true)
////                    .build()
////                olaMapView.moveMarkerBetweenPoints(srcOlaLatLng = driverLatLng!!, enOlaLatLng =  OlaLatLng(sourceLatLng.latitude, sourceLatLng.longitude), olaMarkerOptions = markerOptions, 1000 )
////            }catch (err: Exception){
////                Log.d(TAG, "onMapReady: error => $err")
////            }
//
//                            if (directionsRouteList.isNotEmpty()){
//                                navigationRoute?.addRoutesForRoutePreview(directionsRouteList)
//
//
//
//                            }
//                        }
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

        navigationViewModel.fetchAccessTokenResponseLiveData.observe(this, Observer {result ->
            accessToken = result.data?.accessToken.toString()

            navigationViewModel.clearFetchAccessTokenRes()
            navigationViewModel.fetchAccessTokenResponseLiveData.removeObservers(this)
        })

    }

    //    ******************** Handle PIP mode *********************************************
    override fun onBackPressed() {

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_bookride)

        Log.d(TAG, "onBackPressed: current fragment is ${currentFragment}")

        if(currentFragment !is SelectVehicleTypeFragment && currentFragment !is LookingForRiderFragment && currentFragment !is CancelLookingRideFragment && currentFragment !is CancelRideFragment){
            enterPiPMode()
        }else{
            super.onBackPressed()

        }

    }

    // Method to enter PiP mode
    private fun enterPiPMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val aspectRatio = Rational(1, 2) // Set the aspect ratio for PiP window
            val pipParams = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .build()
            enterPictureInPictureMode(pipParams)


        }
    }

//    override fun onPause() {
//        super.onPause()
//        enterPiPMode()
//    }

     override fun onUserLeaveHint() {
         super.onUserLeaveHint()
         val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_bookride)
         if(currentFragment !is SelectVehicleTypeFragment)
            enterPiPMode()
     }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        Log.d(TAG, "onPictureInPictureModeChanged: PIP mode status => $isInPictureInPictureMode")
        if (isInPictureInPictureMode) {
            // Simplify UI
            binding.fragmentContainerBookride.gone()
            binding.topDivider.gone()
        } else {
            // Restore full UI
            binding.fragmentContainerBookride.visible()
            binding.topDivider.visible()
            olaMapView.removeBezierCurve("bcurve1")
            olaMapView.removeMarkerViewWithId("pickUpHere")
        }

    }

//    ******************** Handle PIP mode *********************************************

     private fun promptUserToEnableLocation() {
         alertDialogService.customAlertDialogAnim(this,"Location Services Disabled", "Location services are required for this app to function correctly. Please enable them.", R.raw.failed){
             val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
             GPSLauncher.launch(intent)
         }
     }

    private fun olaMapsInit() {
        try {
            //call initialize function of OlaMapView with custom configuration
            olaMapView.initialize(
                mapStatusCallback = this,
                olaMapsConfig = OlaMapsConfig.Builder()
                    .setApplicationContext(applicationContext)
                    .setMapBaseUrl("https://api.olamaps.io") // pass the Base URL of Ola-Maps here (Stage/Prod URL), it is mandatory
//                    .setClientId("54ddd969-9c71-4e07-85db-e6cf89f2051c")
                    .setApiKey("B99cOWJwLYnmj1ewxa8RoZYQTqXQbfYfJlRZFrKb")
                    .setProjectId("d9485b50-6b6c-4f42-8a7a-14bd33a758f7") //Pass the Origination ID here, it is mandatory
                    .setMapTileStyle(MapTileStyle.DEFAULT_LIGHT_STANDARD) //pass the MapTileStyle here, it is Optional.
                    .setMinZoomLevel(3.0)
                    .setMaxZoomLevel(21.0)
                    .setZoomLevel(14.0)
                    .showCurrentLocation(false)
                    .setInterceptor ({chain ->
                        val originalRequest = chain.request()

                        val newRequest = originalRequest.newBuilder()
                            .addHeader(
                                "Authorization",
                                "Bearer $accessToken"
                            )
                            .build()

                        chain.proceed(newRequest)
                    })
                    .build()
            )

//            olaMapView.updateMarkerView(
//                OlaMarkerOptions.Builder()
//                    .setMarkerId("dest_marker")
//                    .setPosition(
//                        OlaLatLng(
//                            latitude = latLng.latitude,
//                            longitude = latLng.longitude
//                        )
//                    )
//                    .setIconIntRes(markerViewOptions.iconIntRes!!)
//                    .setIconSize(markerViewOptions.iconSize)
//                    .build()
//            )

        }catch (err: Exception){

        }
//        fusedLocationClient.lastLocation
//            .addOnSuccessListener { location: Location? ->
//                location?.let {
//                    currentLatLng = com.mapbox.mapboxsdk.geometry.LatLng(it.latitude, it.longitude)
//
//                    binding.olaMapView.addHuddleMarkerView(
//                        olaLatLng = OlaLatLng(
//                            latitude = currentLatLng.latitude,
//                            longitude = currentLatLng.longitude
//                        ),
//                        headerText = "Current Location",
//                        descriptionText = "This is your location"
//                    )
//
//                }
//            }
//            .addOnFailureListener {
//                Toast.makeText(
//                    this,
//                    "unable to fetch current latitude, longitude",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
    }

    private fun setupRoute(sourceLatLng: LatLng, destLatLng: LatLng) {
        Log.d(TAG, "showRoute: display route success setupRoute")

//        olaMapView.updateMarkerView(
//            OlaMarkerOptions.Builder()
//                .setMarkerId("dest_marker")
//                .setPosition(
//                    OlaLatLng(
//                        latitude = sourceLatLng.latitude,
//                        longitude = sourceLatLng.longitude
//                    )
//                )
//                .setIconIntRes(markerViewOptions.iconIntRes!!)
//                .setIconSize(markerViewOptions.iconSize)
//                .build()
//        )


        if(navigationRoute != null) {
            navigationRoute!!.removeRoute()
            navigationRoute!!.animateCamera(
                com.mapbox.mapboxsdk.geometry.LatLng(
                    sourceLatLng.latitude,
                    sourceLatLng.longitude
                ), 1.0
            )
            navigationViewModel.fetchRouteAPI(
                OlaLatLng(sourceLatLng.latitude, sourceLatLng.longitude),
                OlaLatLng(destLatLng.latitude, destLatLng.longitude)
            )
        }
    }

    private fun showRoute(routeInfoData: RouteInfoData, latLng: LatLng) {
        if(driverLatLng != null && navigationRoute != null) {
            navigationRoute?.removeRoute()
            directionsRouteList.clear()
            directionsRouteList.add(transform(routeInfoData))

            Log.d(TAG, "showRoute: display route success")
            
            navigationRoute!!.addRoutesForRoutePreview(directionsRouteList)

            olaMapView.animateCameraWithLatLngs(
                olaLatLngs = listOf(
                    OlaLatLng(driverLatLng!!.latitude, driverLatLng!!.longitude),
                    OlaLatLng(latLng.latitude, latLng.longitude)
                ),
                paddingLeft = 160,
                paddingBottom = 160,
                paddingRight = 160,
                paddingTop = 160,
                durationMs = 1000
            )

//            olaMapView.removeMarkerViewWithId("dest_marker")


//            // Once we get the transformed response from SDK, we can get the NavigationViewOptions instance
//            val navigationViewOptions = getNavigationViewOptions(transform(routeInfoData))
//// Register the listener and start the Navigation for Turn-by-Turn Mode
//            if (navigationViewOptions != null) {
//                olaMapView?.addRouteProgressListener(this@BookRideActivity)
//                olaMapView?.registerNavigationStatusCallback(this@BookRideActivity)
//                olaMapView?.startNavigation(navigationViewOptions)
//            }


        }
    }

    private fun ShowDrivers(drivers: JSONArray) {
        olaMapView.removeAllMarkers()
        for (i in 0 until drivers.length()) {
//            Log.d(TAG, "updateUIWithDrivers: selected vehicle => ${driver.vehicleType.uppercase() == SelectVehicle.BIKE.name}")
            Log.d(TAG, "updateUIWithDrivers: selected vehicle => ${drivers.getJSONObject(i)}")
            when(drivers.getJSONObject(i).getString("vehicle_type").uppercase()){
                SelectVehicle.BIKE.name -> {
                    olaMapView.addMarker(point = com.mapbox.mapboxsdk.geometry.LatLng(drivers.getJSONObject(i).getString("latitude").toDouble(), drivers.getJSONObject(i).getString("longitude").toDouble()), icon = R.drawable.ic_bike_top, isAnimRequired = true, iconImage = "ic_bike_top")
                }
                SelectVehicle.CABS.name -> {
                    olaMapView.addMarker(point = com.mapbox.mapboxsdk.geometry.LatLng(drivers.getJSONObject(i).getString("latitude").toDouble(), drivers.getJSONObject(i).getString("longitude").toDouble()), icon = R.drawable.ic_cab_top, isAnimRequired = true, iconImage = "ic_cab_top")
                }
                SelectVehicle.AUTO.name -> {
                    olaMapView.addMarker(point = com.mapbox.mapboxsdk.geometry.LatLng(drivers.getJSONObject(i).getString("latitude").toDouble(), drivers.getJSONObject(i).getString("longitude").toDouble()), icon = R.drawable.ic_cab_bottom, isAnimRequired = true, iconImage = "ic_auto")
                }
                SelectVehicle.BIKE_LITE.name -> {
                    olaMapView.addMarker(point = com.mapbox.mapboxsdk.geometry.LatLng(drivers.getJSONObject(i).getString("latitude").toDouble(), drivers.getJSONObject(i).getString("longitude").toDouble()), icon = R.drawable.ic_car, isAnimRequired = true, iconImage = "ic_car")
                }
            }
        }
    }

    private fun getStateAndCityFromLatLng(latitude: Double, longitude: Double) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val city = address.locality ?: address.subAdminArea
                val state = address.adminArea
                sharedPreference.putString(Constants.CURRENT_DISTRICT, city.toString())
                cabViewModel.fetchRideFareDetail(
                    RideBookRequest(
                        pickuplat = sourceLatLng.latitude,
                        pickupLng = sourceLatLng.longitude,
                        dropLat = destinationLatLng.latitude,
                        dropLng = destinationLatLng.longitude,
                        vehicleId = vehicleId,
                        district = city.toString(),
                        pickupAddress = sourceAddress,
                        dropAddress = destinationAddress
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun calculateAngle(from: OlaLatLng, to: OlaLatLng): Float {
        val lat1 = Math.toRadians(from.latitude)
        val lon1 = Math.toRadians(from.longitude)
        val lat2 = Math.toRadians(to.latitude)
        val lon2 = Math.toRadians(to.longitude)

        val deltaLon = lon2 - lon1
        val y = Math.sin(deltaLon) * Math.cos(lat2)
        val x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLon)

        val angle = Math.toDegrees(Math.atan2(y, x))

        // Apply 180-degree rotation
//        val rotatedAngle = (angle + 245) % 360
//        return rotatedAngle.toFloat()

        // Ensure the angle is between 0 and 360 degrees and cast to Float
        return ((angle + 360) % 360).toFloat()
    }

    override fun onMapReady() {

        navigationRoute = olaMapView.getNavigationMapRoute()!!

//        val markerOptionsSource = OlaMarkerOptions.Builder()
//            .setMarkerId("source")
//            .setPosition(OlaLatLng(sourceLatLng.latitude, sourceLatLng.longitude))
//            .setIconIntRes(R.drawable.ic_location)
//            .setIconSize(0.04F)
//            .setIsIconClickable(true)
//            .setIconRotation(0f)
//            .setIsAnimationEnable(true)
//            .setIsInfoWindowDismissOnClick(true)
//            .build()
//
//        olaMapView.addMarkerView(markerOptionsSource)

//        *************************************************************


//
//        val markerOptionsDestination = OlaMarkerOptions.Builder()
//            .setMarkerId("destination")
//            .setPosition(OlaLatLng(destinationLatLng.latitude, destinationLatLng.longitude))
//            .setIconIntRes(com.ola.maps.R.drawable.ic_user_destination)
////            .setIconSize(0.1F)
//            .setIsIconClickable(true)
//            .setIconRotation(0f)
//            .setIsAnimationEnable(true)
//            .setIsInfoWindowDismissOnClick(true)
//            .build()
//
//        olaMapView.addMarkerView(markerOptionsDestination)

        if(drawPath) {
            drawPath = false
//            navigationViewModel.fetchRouteAPI(
//                OlaLatLng(sourceLatLng.latitude, sourceLatLng.longitude),
//                OlaLatLng(destinationLatLng.latitude, destinationLatLng.longitude)
//            )
            setupRoute(LatLng(sourceLatLng.latitude, sourceLatLng.longitude), LatLng(destinationLatLng.latitude, destinationLatLng.longitude))
        }

    }

    override fun onMapLoadFailed(p0: String?) {
        Log.d(TAG, "onMapLoadFailed: errro on map load => ${p0}")

        p0?.let {
            val errorCode = p0.substring(it.lastIndex - 2)
            if (errorCode == "401") {
                navigationViewModel.fetchAccessTokenAPI(
                    clientId = "54ddd969-9c71-4e07-85db-e6cf89f2051c",
                    clientSecret = "PLo98S3rcnrg3oj9pV6nMsv3Lrssp8BQ"
                )
            }

        }

    }

    override fun onDestroy() {
        olaMapView?.onDestroy()
        loader.dismiss()
        cabViewModel.clearRideFareDetailRes()

        masterViewModel.setDashboardData(null)
        super.onDestroy()

//        if(::olaMapView.isInitialized){
//            olaMapView.removeRouteProgressListener()
//        }


    }



    override fun onResume() {
        super.onResume()
        bindObservers()

        Log.d(TAG, "onResume: onResume called!!..")

        var bookingId = sharedPreference.getString(Constants.TEMP_BOOKING_ID)

        val rideHistories = masterViewModel.dashboardData.value
        if(rideHistories != null){
            bookingId = rideHistories.tempBookingsId
            sharedPreference.putString(Constants.TEMP_BOOKING_ID, bookingId)
        }

//        if(!bookingId.isNullOrEmpty()) {
//            socketManager.sendData("user_update", JSONObject().apply {
//                put("temp_bookings_id", bookingId)
//                put("socket_id", socketManager.getSocketId())
//            }) {
//                runOnUiThread {
//                    showToast(
//                        "user connect emit success!!..."
//                    )
//                }
//            }
//        }


        if(::olaMapView.isInitialized)
            olaMapView?.onResume()


    }

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

                    if(responseData?.status == true){
                        polylineData = responseData.drivePath ?: ""

                    }else{
                        alertDialogService.alertDialogAnim(
                            this,
                            responseData?.message.toString(),
                            R.raw.failed,
                            true
                        )
                        val district = sharedPreference.getString(Constants.CURRENT_DISTRICT, "")
                        cabViewModel.updateNonServiceDistrict(district.toString())
                        cabViewModel.rideFareDetailResponseLiveData.removeObservers(this)
                        cabViewModel.clearRideFareDetailRes()
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


    //call onStop() of SDK before removing the OlaMaps
    override fun onStop() {
        super.onStop()
//        olaMapView?.onStop()
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


    override fun performOperation(status: BookingStatus) {
        when(status){
            BookingStatus.SELECT_VEHICLE -> {

            }
            BookingStatus.LOOKING_DRIVER -> {
                showToast("Looking drivers")
                isRideStatus = BookingStatus.LOOKING_DRIVER
                sharedPreference.saveEnum(Constants.RIDE_STATUS, isRideStatus)
            }
            BookingStatus.RIDE_CANCELLED -> {
                showToast("Ride Cancelled")
                isRideStatus = BookingStatus.RIDE_CANCELLED
                sharedPreference.saveEnum(Constants.RIDE_STATUS, isRideStatus)
            }
            BookingStatus.RIDE_ACCEPTED -> {
                olaMapView.removeAllMarkers()
                showToast("Ride Accepted")

                directionsRouteList.clear()
//                navigationViewModel.clearRoute()

                isRideStatus = BookingStatus.RIDE_ACCEPTED
                sharedPreference.saveEnum(Constants.RIDE_STATUS, isRideStatus)


            }
            BookingStatus.ARRIVEING -> {
                olaMapView.removeAllMarkers()

                directionsRouteList.clear()

                showToast("Driver arriving soon")
                isRideStatus = BookingStatus.ARRIVEING
                sharedPreference.saveEnum(Constants.RIDE_STATUS, isRideStatus)
            }
            BookingStatus.ARRIVED -> {
                showToast("Driver arrived")
                directionsRouteList.clear()


                isRideStatus = BookingStatus.ARRIVED
                sharedPreference.saveEnum(Constants.RIDE_STATUS, isRideStatus)
            }
            BookingStatus.RIDE_STARTED -> {
                showToast("Ride Started")
                directionsRouteList.clear()


//                navigationViewModel.clearRoute()
                isRideStatus = BookingStatus.RIDE_STARTED
                sharedPreference.saveEnum(Constants.RIDE_STATUS, isRideStatus)


//                if(::polylineData.isInitialized && polylineData.isNotEmpty()){
//
//                    var polyData = decodePolylineOlaMapSdk(polylineData)
//
//
//                    val polylineOptions = OlaPolylineOptions.Builder()
//                        .setPolylineId("pid1")
//                        .setPoints(polyData)
//                        .setWidth(2f)
//                        .setColor("#060606") //0000FF
//                        .build()
////
//                    olaMapView.addPolyline(polylineOptions)
//
////                            polylineOptions.points.addAll(polyData)
//                    Log.d(TAG, "bindObservers: route array length => Outer ${polyData.size} and ${::olaMapView.isInitialized}")
//                    println(polyData)
//                    if(::olaMapView.isInitialized) {
//                        Log.d(TAG, "bindObservers: route array length => Inner ${polyData.size} and ${polylineOptions.points}")
//                        olaMapView.addPolyline(polylineOptions)
//                    }

//                }

            }
            BookingStatus.RIDE_COMPLETED -> {
                showToast("Ride Completed")
                isRideStatus = BookingStatus.RIDE_COMPLETED
                sharedPreference.saveEnum(Constants.RIDE_STATUS, isRideStatus)
                sharedPreference.putString(Constants.TEMP_BOOKING_ID, "")

                directionsRouteList.clear()
            }
            else -> {
                Log.d(TAG, "performOperation: no status on Booking Ride Activity")
            }
        }



    }

     override fun onPermissionsResult(result: List<PermissionStatus>) {
             when {
                 result.anyPermanentlyDenied() -> showPermanentlyDeniedDialog(result, "Please allowed Permission"){
                     val intent = Intent().apply {
                         action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                         data = Uri.fromParts("package", packageName, null)
                     }
                     GPSLauncher.launch(intent)
                 }
                 result.anyShouldShowRationale() -> showRationaleDialog(result, request, "Please allowed Permission")
                 result.allGranted() -> {

                     olaMapsInit()


                 }
             }
         }


}