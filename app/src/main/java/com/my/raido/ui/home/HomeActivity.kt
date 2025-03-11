package com.my.raido.ui.home



//import com.ola.mapsdk.camera.MapControlSettings
//import com.ola.mapsdk.interfaces.OlaMapCallback
//import com.ola.mapsdk.model.OlaLatLng
//import com.ola.mapsdk.model.OlaMarkerOptions
//import com.ola.mapsdk.view.OlaMap
//import com.ola.mapsdk.view.OlaMapView
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.kotlinpermissions.PermissionStatus
import com.kotlinpermissions.allGranted
import com.kotlinpermissions.anyPermanentlyDenied
import com.kotlinpermissions.anyShouldShowRationale
import com.kotlinpermissions.extension.permissionsBuilder
import com.kotlinpermissions.request.PermissionRequest
import com.my.raido.Helper.SelectVehicle
import com.my.raido.R
import com.my.raido.Utils.AlertDialogUtility
import com.my.raido.Utils.AppUtils
import com.my.raido.Utils.TokenManager
import com.my.raido.Utils.getLoadingDialog
import com.my.raido.Utils.hideLoader
import com.my.raido.Utils.setOnSingleClickListener
import com.my.raido.Utils.showPermanentlyDeniedDialog
import com.my.raido.Utils.showRationaleDialog
import com.my.raido.Utils.showToast
import com.my.raido.constants.Constants
import com.my.raido.data.prefs.SharedPrefManager
import com.my.raido.databinding.ActivityHomeBinding
import com.my.raido.models.DriverDataModel
import com.my.raido.socket.SocketManager
import com.my.raido.ui.drivers.DriverLocationViewModel
import com.my.raido.ui.home.bottomsheet_fragments.DrawerFragment
import com.my.raido.ui.home.bottomsheet_fragments.PlanTripFragment
import com.my.raido.ui.home.bottomsheet_fragments.RecentRideHistoryFragment
import com.my.raido.ui.viewmodels.BookRideViewModel
import com.my.raido.ui.viewmodels.DummyDataViewModel
import com.my.raido.ui.viewmodels.cabViewModel.CabViewModel
import com.ola.maps.mapslibrary.models.OlaLatLng
import com.ola.maps.mapslibrary.models.OlaMapsConfig
import com.ola.maps.mapslibrary.utils.MapTileStyle
import com.ola.maps.navigation.ui.v5.MapStatusCallback
import com.ola.maps.navigation.v5.navigation.OlaMapView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class HomeActivity : AppCompatActivity(), PermissionRequest.Listener, MapStatusCallback { //OnMapReadyCallback

    companion object{
        private const val TAG = "Home Activity"
    }

    private lateinit var binding: ActivityHomeBinding

    @Inject
    lateinit var alertDialog: AlertDialogUtility

    @Inject
    lateinit var socketManager: SocketManager

    @Inject
    lateinit var sharedPrefManager: SharedPrefManager

    @Inject
    lateinit var tokenManager: TokenManager

    private val cabViewModel: CabViewModel by viewModels()

    private val bookRideViewModel: BookRideViewModel by viewModels()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val map_view_bundle_key = "My_Bundle"

    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView

//    private lateinit var olaMapView: OlaMapView
//
//    private lateinit var olaMap: OlaMap

    private lateinit var olaMapView: OlaMapView

    private var selectedVehicle = SelectVehicle.BIKE

    private var userId = 0

    private val request by lazy {
        permissionsBuilder(Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION).build()
    }

    private lateinit var loader: AlertDialog

    private lateinit var locationCallback: LocationCallback

    private var apiKey = ""

    private val GPSLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(AppUtils.sharedInstance.isLocationEnabled(this)){
            request.send()
        }else{
            promptUserToEnableLocation()
        }
    }

    private val viewModel: DriverLocationViewModel by viewModels()
//    private val driverMarkers = mutableMapOf<String, Marker>()
//    private val driverMarkers = mutableMapOf<String, com.ola.mapsdk.view.Marker>()

//    private lateinit var dataGenerator: DummyDataGenerator
        private val dummyDataViewModel: DummyDataViewModel by viewModels()

    private  var isMapLoad = false

//    val mapControlSettings = MapControlSettings.Builder()
//        .setRotateGesturesEnabled(true)
//        .setScrollGesturesEnabled(true)
//        .setZoomGesturesEnabled(true)
//        .setCompassEnabled(false)
//        .setTiltGesturesEnabled(true)
//        .setDoubleTapGesturesEnabled(true)
//        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        olaMapView = binding.mapView
        olaMapView.onCreate(savedInstanceState)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        request.addListener(this)

        loader = getLoadingDialog(this)

//        mapView = binding.googleMap

        userId = sharedPrefManager.getInt(Constants.USER_ID)


//        var mapViewBundle: Bundle? = null
//        if (savedInstanceState != null) {
//            mapViewBundle = savedInstanceState.getBundle(map_view_bundle_key)
//        }

//        mapView.onCreate(mapViewBundle)
//        mapView.getMapAsync(this)

        if(AppUtils.sharedInstance.isLocationEnabled(this)){
            request.send()
        }else{
            promptUserToEnableLocation()
        }

        selectedVehicle = sharedPrefManager.getEnum(Constants.VEHICLE_TYPE, SelectVehicle::class.java ,SelectVehicle.BIKE)

        setActiveTab(selectedVehicle)

        binding.sectionOneRightDashboard.setOnSingleClickListener {
            val bottomSheetFragment = PlanTripFragment()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

        binding.searchBarDashboard.setOnSingleClickListener {
            val bottomSheetFragment = PlanTripFragment()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

        binding.settingBtnDashboard.setOnSingleClickListener {
            val bottomSheetFragment = DrawerFragment()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

        binding.sectionTwoRightDashboard.setOnSingleClickListener {
            val bottomSheetFragment = RecentRideHistoryFragment()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

        binding.bikeBox.setOnSingleClickListener {
            setActiveTab(SelectVehicle.BIKE)
            val bottomSheetFragment = PlanTripFragment()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

        binding.autoBox.setOnSingleClickListener {
            setActiveTab(SelectVehicle.AUTO)
            val bottomSheetFragment = PlanTripFragment()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

        binding.cabBox.setOnSingleClickListener {
            setActiveTab(SelectVehicle.CABS)
            val bottomSheetFragment = PlanTripFragment()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

        binding.parcelBox.setOnSingleClickListener {
            setActiveTab(SelectVehicle.PARCEL)
            val bottomSheetFragment = PlanTripFragment()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

        binding.bikeLiteBox.setOnSingleClickListener {
            setActiveTab(SelectVehicle.BIKE_LITE)
            val bottomSheetFragment = PlanTripFragment()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

//        binding.sectionTwoLeftDashboard.setOnSingleClickListener {
//
//
//
////            ****************************************************************************************
////            val intent = Intent(
////                Intent.ACTION_VIEW,
////                Uri.parse("google.navigation:q=$destination&mode=d")
////            )
////            intent.setPackage("com.google.android.apps.maps")
////            startActivity(intent)
//
////            ***********************************************************************************************
////            val intent = Intent(
////                Intent.ACTION_VIEW,
////                Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$source&destination=$destination&travelmode=driving")
////            )
////            intent.setPackage("com.google.android.apps.maps")
////            startActivity(intent)
//
//        }

        // Fetching API_KEY which we wrapped
//        val ai: ApplicationInfo = applicationContext.packageManager
//            .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
//        val value = ai.metaData["com.google.android.geo.API_KEY"]
//        apiKey = value.toString()

        binding.gpsLocationBtn.setOnSingleClickListener {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
//                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
//                    if (location != null) {
//                        val latLng = LatLng(location.latitude, location.longitude)
////                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
//
//                    } else {
//                        showToast("Unable to fetch location")
//                    }
//                }

                if(::olaMapView.isInitialized) {
//                 Handle button click
                    getCurrentLocation { latitude, longitude ->
                        val userLatLng = OlaLatLng(latitude, longitude)
                        olaMapView.moveCameraToLatLong(
                            userLatLng,
                            zoomLevel = 15.0,
                            1000
                        ) // Move camera to current location with zoom level
                    }
                }else{

                }
            } else {
                request.send()
            }
        }

//  ***********************************************************************************
//            tokenManager.removeToken()
//            sharedPrefManager.removeAll()
//            startActivity(Intent(this, AuthActivity::class.java))
//            finish()
//  ***********************************************************************************

//  ******************************************* Simulator ******************************************
//        dataGenerator = DummyDataGenerator()

        // Generate 10 dummy drivers and 20 dummy passengers
//        dataGenerator.generateDummyDrivers(80)
//        dataGenerator.generateDummyPassengers(20)

        // Listen for driver location updates
//        val driverData = dataGenerator.listenForDriverUpdates()

        // Simulate movement for a specific driver
//        dataGenerator.simulateDriverMovement("driver_1")





        // Generate dummy drivers
//        dummyDataViewModel.generateDrivers(80, )

        // Generate dummy passengers
//        dummyDataViewModel.generatePassengers(5)

        // Listen for driver updates
//        dummyDataViewModel.listenForDriverUpdates { driver ->
//            Log.d("Driver Update", "Driver: ${driver.riderId}, Location: ${driver.latitude}, ${driver.longitude}")
//        }

        // Simulate driver movement for a specific driver
//        dummyDataViewModel.startDriverMovementSimulation("driver_1")
//  ******************************************* Simulator ******************************************


        binding.sectionTwoLeftDashboard.setOnSingleClickListener {
//            val pickUpLatLng = PassOlaLatLng(26.220756, 73.040516)
//            val dropLatLng = PassOlaLatLng(26.269024, 73.018435)
////            bookRideViewModel.sendRideRequest(riderId = userId.toString(), pickupLat = pickUpLatLng.latitude, pickupLng = pickUpLatLng.longitude, dropLat = dropLatLng.latitude, dropLng = dropLatLng.longitude)
//
//            val intent = Intent(this, BookRideActivity::class.java)
//            intent.putExtra("sourceText", pickUpLatLng)
//            intent.putExtra("destText", dropLatLng)
//            startActivity(intent)

//            ******************************************************************
            showToast("open map app..")
            val source = "26.228985, 73.039767"  // Jhalamand
            val destination = "26.284224, 73.016299"  // Jalori Gate


//            val intent = Intent(
//                Intent.ACTION_VIEW,
//                Uri.parse("google.navigation:q=$destination&mode=d") // "d" = driving mode
//            )
//            intent.setPackage("com.google.android.apps.maps") // Ensure only Google Maps opens
//
//            if (intent.resolveActivity(packageManager) != null) {
//                startActivity(intent) // Start navigation
//            } else {
//                Toast.makeText(this, "Google Maps is not installed!", Toast.LENGTH_LONG).show()
//            }


        }


//        *********************** OlaMapSdk **********************************************************


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

        // Initialize the OlaMapView
//        olaMapView.onCreate(savedInstanceState)

//        olaMapView.getMap(apiKey = "1mthwdRIjnbT77e4xJUcLserilOY5BhRtn5sQa4S",
//            olaMapCallback = object : OlaMapCallback {
//                override fun onMapReady(olaMap: OlaMap) {
//
//                    this@HomeActivity.olaMap = olaMap
//
//                    if(!isMapLoad){
//                        isMapLoad = true
//
//
//
//
//                        // Fetch location manually using FusedLocationProviderClient
//                        getCurrentLocation { latitude, longitude ->
//                            val currentLocation = OlaLatLng(latitude, longitude)
//
//                            // Animate camera to the user's current location
//                            olaMap.moveCameraToLatLong(currentLocation, 15.0, 1000)
//
//                            olaMap.showCurrentLocation()
//
//                           return@getCurrentLocation
//                        }
//
//
//                    }
//
//
//                }
//
//                override fun onMapError(error: String) {
//                    // Handle map error
//                }
//            },mapControlSettings
//        )
//        *********************** OlaMapSdk **********************************************************

        cabViewModel.nearByCabsResponseLiveData.observe(this){ response ->
            val driversAry = response.data?.nearbyRiders
            val serviceCategoryAry = response.data?.serviceCategories ?: ArrayList()
            driversAry?.forEach { driver ->
                Log.d(TAG, "onMapReady: drivers data => ${driver}")
//                updateDriverMarker(driver, olaMap) //mMap
            }

            binding.walletBalanceText.text = response.data?.walletBalance

            val aboutUsData = response.data?.aboutUsModel

            if(aboutUsData != null){
                sharedPrefManager.putString(Constants.PRIVACY_POLICY, aboutUsData.privacyPolicy)
                sharedPrefManager.putString(Constants.TERM_CONDITION, aboutUsData.termsCondition)
                sharedPrefManager.putString(Constants.JOIN_TERM, aboutUsData.joinTheTeam)
                sharedPrefManager.putString(Constants.BLOG, aboutUsData.blog)
            }

            Log.d(TAG, "onMapReady: $serviceCategoryAry")

            if(serviceCategoryAry.isNotEmpty()){
                val bikeAry = serviceCategoryAry.filter { it.vehicleType == "Bike" }
                val autoAry = serviceCategoryAry.filter { it.vehicleType == "Auto" }
                val cabAry = serviceCategoryAry.filter { it.vehicleType == "Cabs" }
                val bikeLiteAry = serviceCategoryAry.filter { it.vehicleType == "Bike Light" }

                if(bikeAry.isNotEmpty()){
                    sharedPrefManager.putInt(Constants.BIKE_ID, bikeAry[0].vehicleId )
                }
                if(autoAry.isNotEmpty()) {
                    sharedPrefManager.putInt( Constants.AUTO_ID, autoAry[0].vehicleId )
                }
                if(cabAry.isNotEmpty()) {
                    sharedPrefManager.putInt( Constants.CAB_ID, cabAry[0].vehicleId )
                }
                if(bikeLiteAry.isNotEmpty()) {
                    sharedPrefManager.putInt(Constants.BIKE_LITE_ID, bikeLiteAry[0].vehicleId)
                }
            }



        }



//  ************************* Socket Work ***********************************************************

//        val json = JSONObject().apply {
//            put("message", "hello Mahendra")
//        }
//        socketManager.sendData("HelloData", json)
//
//

        socketManager.listenToEvent("testEvent"){data ->
            Log.d(TAG, "SocketManager: hello socket data => ${data.getString("message")}")
        }



//  ************************* Socket Work ***********************************************************


    }

    override fun onResume() {
        super.onResume()
        if(::olaMapView.isInitialized.not())
            olaMapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        socketManager.socketConnect()
    }


    override fun onPause() {
        super.onPause()
        olaMapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        olaMapView.onLowMemory()
    }

//    private fun updateDriverMarker(driver: NearByRiderModel, olaMap: OlaMap) { //googleMap: GoogleMap
////        val position = LatLng(driver.latitude.toDouble(), driver.longitude.toDouble())
//        val position = OlaLatLng(driver.latitude.toDouble(), driver.longitude.toDouble())
//
//        if (driverMarkers.containsKey(driver.riderId.toString())) {
//            // Move existing marker
////            driverMarkers[driver.riderId.toString()]?.position = position
//            driverMarkers[driver.riderId.toString()]?.setPosition(position)
//        } else {
////            val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(MapUtils.getCarBitmap(this, R.drawable.ic_car))
//
//            val markerOptions1 = OlaMarkerOptions.Builder()
//                .setMarkerId("Driver ${driver.riderId}")
//                .setPosition(position)
//                .setIconSize(0.6f)
//                .setIconBitmap(MapUtils.getCarBitmap(this, R.drawable.ic_car))
//                .setIsAnimationEnable(true)
//                .build()
//            olaMap.addMarker(markerOptions1)
//
//
//        }
//    }

    //call onStop() of SDK before removing the OlaMaps
    override fun onStop() {
        super.onStop()
        olaMapView?.onStop()
    }

    override fun onDestroy() {
        olaMapView?.onDestroy()
        super.onDestroy()

        socketManager.socketDisconnect()
    }

    override fun onMapReady() {
//        Toast.makeText(this, "Map is ready to use", Toast.LENGTH_SHORT).show()

        // 🔹 Observe LiveData (UI will update automatically)
        cabViewModel.nearbyDrivers.observe(this) { drivers ->
            updateUIWithDrivers(drivers)
        }

    }

    override fun onMapLoadFailed(p0: String?) {
        Log.d(TAG, "onMapLoadFailed: errro on map load => ${p0}")
        Toast.makeText(this, "error on map load $p0", Toast.LENGTH_SHORT).show()
    }

    private fun updateUIWithDrivers(drivers: List<DriverDataModel>) {
        olaMapView.removeAllMarkers()
        for (driver in drivers) {
            Log.d(TAG, "updateUIWithDrivers: selected vehicle => ${driver.vehicleType.uppercase() == SelectVehicle.BIKE.name}")
            when(driver.vehicleType.uppercase()){
                SelectVehicle.BIKE.name -> {
                    olaMapView.addMarker(point = com.mapbox.mapboxsdk.geometry.LatLng(driver.latitude, driver.longitude), icon = R.drawable.ic_bike_top, isAnimRequired = true, iconImage = "ic_bike_top")
                }
                SelectVehicle.CABS.name -> {
                    olaMapView.addMarker(point = com.mapbox.mapboxsdk.geometry.LatLng(driver.latitude, driver.longitude), icon = R.drawable.ic_cab_top, isAnimRequired = true, iconImage = "ic_cab_top")
                }
                SelectVehicle.AUTO.name -> {
                    olaMapView.addMarker(point = com.mapbox.mapboxsdk.geometry.LatLng(driver.latitude, driver.longitude), icon = R.drawable.ic_auto, isAnimRequired = true, iconImage = "ic_auto")
                }
                SelectVehicle.BIKE_LITE.name -> {
                    olaMapView.addMarker(point = com.mapbox.mapboxsdk.geometry.LatLng(driver.latitude, driver.longitude), icon = R.drawable.ic_car, isAnimRequired = true, iconImage = "ic_car")
                }
            }
        }
    }

    private fun setActiveTab(keyVal: SelectVehicle){

        sharedPrefManager.saveEnum(Constants.VEHICLE_TYPE, keyVal)

        val activeColor = ContextCompat.getColor(this, R.color.bg_dark)
        val inactiveColor = ContextCompat.getColor(this, R.color.white)

        when(keyVal){
            SelectVehicle.BIKE -> {
                binding.selectBikeBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.bikeIc.backgroundTintList = ColorStateList.valueOf(activeColor)

                binding.selectAutoBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_light))
                binding.autoIc.backgroundTintList = ColorStateList.valueOf(inactiveColor)

                binding.selectCabBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_light))
                binding.cabIc.backgroundTintList = ColorStateList.valueOf(inactiveColor)

                binding.selectParcelBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_light))
                binding.parcelIc.backgroundTintList = ColorStateList.valueOf(inactiveColor)

                binding.selectBikeliteBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_light))
                binding.bikeliteIc.backgroundTintList = ColorStateList.valueOf(inactiveColor)
            }
            SelectVehicle.AUTO -> {
                binding.selectBikeBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_light))
                binding.bikeIc.backgroundTintList = ColorStateList.valueOf(inactiveColor)

                binding.selectAutoBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.autoIc.backgroundTintList = ColorStateList.valueOf(activeColor)

                binding.selectCabBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_light))
                binding.cabIc.backgroundTintList = ColorStateList.valueOf(inactiveColor)

                binding.selectParcelBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_light))
                binding.parcelIc.backgroundTintList = ColorStateList.valueOf(inactiveColor)

                binding.selectBikeliteBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_light))
                binding.bikeliteIc.backgroundTintList = ColorStateList.valueOf(inactiveColor)
            }
            SelectVehicle.CABS -> {
                binding.selectBikeBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_light))
                binding.bikeIc.backgroundTintList = ColorStateList.valueOf(inactiveColor)

                binding.selectAutoBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_light))
                binding.autoIc.backgroundTintList = ColorStateList.valueOf(inactiveColor)

                binding.selectCabBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.cabIc.backgroundTintList = ColorStateList.valueOf(activeColor)

                binding.selectParcelBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_light))
                binding.parcelIc.backgroundTintList = ColorStateList.valueOf(inactiveColor)

                binding.selectBikeliteBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_light))
                binding.bikeliteIc.backgroundTintList = ColorStateList.valueOf(inactiveColor)

            }
            SelectVehicle.PARCEL -> {
                binding.selectBikeBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_light))
                binding.bikeIc.backgroundTintList = ColorStateList.valueOf(inactiveColor)

                binding.selectAutoBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_light))
                binding.autoIc.backgroundTintList = ColorStateList.valueOf(inactiveColor)

                binding.selectCabBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_light))
                binding.cabIc.backgroundTintList = ColorStateList.valueOf(inactiveColor)

                binding.selectParcelBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.parcelIc.backgroundTintList = ColorStateList.valueOf(activeColor)

                binding.selectBikeliteBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_light))
                binding.bikeliteIc.backgroundTintList = ColorStateList.valueOf(inactiveColor)

            }
            SelectVehicle.BIKE_LITE -> {
                binding.selectBikeBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_light))
                binding.bikeIc.backgroundTintList = ColorStateList.valueOf(inactiveColor)

                binding.selectAutoBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_light))
                binding.autoIc.backgroundTintList = ColorStateList.valueOf(inactiveColor)

                binding.selectCabBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_light))
                binding.cabIc.backgroundTintList = ColorStateList.valueOf(inactiveColor)

                binding.selectParcelBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bg_light))
                binding.parcelIc.backgroundTintList = ColorStateList.valueOf(inactiveColor)

                binding.selectBikeliteBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.bikeliteIc.backgroundTintList = ColorStateList.valueOf(activeColor)
            }
        }

    }

    private fun promptUserToEnableLocation() {
        alertDialog.customAlertDialogAnim(this,"Location Services Disabled", "Location services are required for this app to function correctly. Please enable them.", R.raw.failed){
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            GPSLauncher.launch(intent)
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
//                showToast("permission granted")
//
//                getCurrentLocation()
                // Fetch location manually using FusedLocationProviderClient
//                showLoader(this, loader)
//                getCurrentLocation { latitude, longitude ->
//                    val currentLocation = OlaLatLng(latitude, longitude)
//
//                    if(::olaMap.isInitialized) {
//                        // Animate camera to the user's current location
//                        olaMap.moveCameraToLatLong(currentLocation, 15.0, 1000)
//
//                        olaMap.showCurrentLocation()
//
//                        return@getCurrentLocation
//                    }
//                }


//                getCurrentLocation()

                if(::olaMapView.isInitialized) {
//                 Handle button click
                    getCurrentLocation { latitude, longitude ->
                        val userLatLng = OlaLatLng(latitude, longitude)
                        olaMapView.moveCameraToLatLong(
                            userLatLng,
                            zoomLevel = 15.0,
                            1000
                        ) // Move camera to current location with zoom level
                    }
                }else{
                    showToast("Something went wrong with olaMapView")
                }




            }
        }
    }

//    override fun onMapReady(googleMap: GoogleMap) {
//
//
//
//        mapView.onResume()
//
//        mMap = googleMap
//
//        mMap.uiSettings.isMyLocationButtonEnabled = false
//        mMap.uiSettings.isCompassEnabled = false
//
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            request.send()
//            return
//        }
//
//        // Enable the blue dot (My Location Layer)
//        mMap.isMyLocationEnabled = true
//
//        // Set map type to normal
//        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
//
//        // Load the custom map style
//        try {
//            val success = mMap.setMapStyle(
//                MapStyleOptions.loadRawResourceStyle(applicationContext, R.raw.custom_map_style)
//            )
//            if (!success) {
//                Log.e("MapStyle", "Style parsing failed.")
//            }
//        } catch (e: Resources.NotFoundException) {
//            Log.e("MapStyle", "Can't find style. Error: ", e)
//        }
//
//
////        viewModel.availableDrivers.observe(this) { drivers ->
////            drivers.forEach { driver ->
////                updateDriverMarker(driver, mMap)
////            }
////        }
//
//        cabViewModel.nearByCabsResponseLiveData.observe(this){ response ->
//            val driversAry = response.data?.nearbyRiders
//            val serviceCategoryAry = response.data?.serviceCategories ?: ArrayList()
//            driversAry?.forEach { driver ->
//                Log.d(TAG, "onMapReady: drivers data => ${driver}")
//                updateDriverMarker(driver, olaMap) //mMap
//            }
//
//            val aboutUsData = response.data?.aboutUsModel
//
//            if(aboutUsData != null){
//                sharedPrefManager.putString(Constants.PRIVACY_POLICY, aboutUsData.privacyPolicy)
//                sharedPrefManager.putString(Constants.TERM_CONDITION, aboutUsData.termsCondition)
//                sharedPrefManager.putString(Constants.JOIN_TERM, aboutUsData.joinTheTeam)
//                sharedPrefManager.putString(Constants.BLOG, aboutUsData.blog)
//            }
//
//            Log.d(TAG, "onMapReady: $serviceCategoryAry")
//
//            if(serviceCategoryAry.isNotEmpty()){
//                val bikeAry = serviceCategoryAry.filter { it.vehicleType == "Bike" }
//                val autoAry = serviceCategoryAry.filter { it.vehicleType == "Auto" }
//                val cabAry = serviceCategoryAry.filter { it.vehicleType == "Cabs" }
//                val bikeLiteAry = serviceCategoryAry.filter { it.vehicleType == "Bike Light" }
//
//                if(bikeAry.isNotEmpty()){
//                    sharedPrefManager.putInt(Constants.BIKE_ID, bikeAry[0].vehicleId )
//                }
//                if(autoAry.isNotEmpty()) {
//                    sharedPrefManager.putInt( Constants.AUTO_ID, autoAry[0].vehicleId )
//                }
//                if(cabAry.isNotEmpty()) {
//                    sharedPrefManager.putInt( Constants.CAB_ID, cabAry[0].vehicleId )
//                }
//                if(bikeLiteAry.isNotEmpty()) {
//                    sharedPrefManager.putInt(Constants.BIKE_LITE_ID, bikeLiteAry[0].vehicleId)
//                }
//            }
//
//
//
//        }
//
//
//    }

    // Get the current location
    private fun getCurrentLocation() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            request.send()
            return
        }


        val locationRequest = LocationRequest.Builder( // API level 30 and higher uses LocationRequest.Builder
            Priority.PRIORITY_HIGH_ACCURACY, // High accuracy for real-time location
            1000 // Request every 1000ms (1 second)
        ).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude

//                        dummyDataViewModel.generateDrivers(500, latitude, longitude)

                        Log.d(TAG, "requestCurrentLocation: current location => $latitude, $longitude and ${::mMap.isInitialized}")
                        val latLng = LatLng(latitude, longitude)


                        sharedPrefManager.putLatLng(Constants.CURRENT_LATLNG, latLng)
                        sharedPrefManager.putLatLng(Constants.PICKUP_LOCATION, latLng)
//                        sharedViewModel.updateMarkerLocation(latLng)

                        // Update the map or use the latitude and longitude values
                        if (::mMap.isInitialized) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                            mMap.moveCamera(CameraUpdateFactory.zoomTo(16F))
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(16F))
//                            mMap.addMarker(MarkerOptions().position(latLng).title("I am here"))
                        }

//                        // Add markers
//                        val desination = LatLng(26.275913, 73.032676)
//
//
//                        mapView.getMapAsync {
//                            mMap = it
//                            mMap.addMarker(MarkerOptions().position(latLng))
//                            mMap.addMarker(MarkerOptions().position(desination))
//                            val urll = getDirectionURL(
//                                latLng,
//                                desination,
//                                apiKey
//                            )
//                            GetDirection(urll).execute()
//                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14F))
//                        }

//                        cabViewModel.fetchCabs(lng = longitude, lat = latitude)
                        cabViewModel.fetchDrivers(lng = longitude, lat = latitude)

                        hideLoader(this@HomeActivity, loader)
                        // Stop further location updates
                        fusedLocationClient.removeLocationUpdates(this)
                        break // No need to keep checking, we got a valid location
                    }else{
                        showToast("location is null")
                    }
                }
            }

            override fun onLocationAvailability(p0: LocationAvailability) {
                super.onLocationAvailability(p0)
                Log.d(TAG, "onLocationAvailability: enter on that loop ${p0}")
            }
        }

        // Request location updates
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)

    }

    private fun getCurrentLocation(onLocationFetched: (Double, Double) -> Unit) {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    onLocationFetched(it.latitude, it.longitude)
                    val latLngData = LatLng(it.latitude, it.longitude)
                    sharedPrefManager.putLatLng(Constants.CURRENT_LATLNG, latLngData)
                    sharedPrefManager.putLatLng(Constants.PICKUP_LOCATION, latLngData)
//                    cabViewModel.fetchCabs(lng = it.longitude, lat = it.latitude)
                    cabViewModel.fetchDrivers(lng = it.longitude, lat = it.latitude)
                    hideLoader(this@HomeActivity, loader)
                } ?: run {
                    // Handle location not available
                }
            }
        } else {
            request.send()
        }
    }

}