package com.my.raido.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
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
import com.my.raido.Utils.NetworkResult
import com.my.raido.Utils.TokenManager
import com.my.raido.Utils.getLoadingDialog
import com.my.raido.Utils.hideLoader
import com.my.raido.Utils.setOnSingleClickListener
import com.my.raido.Utils.showBottomSheetSafely
import com.my.raido.Utils.showLoader
import com.my.raido.Utils.showPermanentlyDeniedDialog
import com.my.raido.Utils.showRationaleDialog
import com.my.raido.Utils.showToast
import com.my.raido.constants.Constants
import com.my.raido.constants.SocketConstants
import com.my.raido.data.prefs.SharedPrefManager
import com.my.raido.databinding.ActivityHomeBinding
import com.my.raido.services.BookingStatus
import com.my.raido.socket.SocketManager
import com.my.raido.ui.home.bottomsheet_fragments.AddMoneyFragment
import com.my.raido.ui.home.bottomsheet_fragments.DrawerFragment
import com.my.raido.ui.home.bottomsheet_fragments.RecentRideHistoryFragment
import com.my.raido.ui.home.bottomsheet_fragments.ride_book.CancelRideFragment
import com.my.raido.ui.home.bottomsheet_fragments.ride_book.PlanTripFragment
import com.my.raido.ui.viewmodels.MasterViewModel
import com.my.raido.ui.viewmodels.NavigationViewModel
import com.my.raido.ui.viewmodels.cabViewModel.CabViewModel
import com.my.raido.ui.viewmodels.userViewModel.UserDataViewModel
import com.ola.maps.mapslibrary.models.OlaLatLng
import com.ola.maps.mapslibrary.models.OlaMapsConfig
import com.ola.maps.mapslibrary.models.OlaMarkerOptions
import com.ola.maps.navigation.ui.v5.MapStatusCallback
import com.ola.maps.navigation.v5.navigation.OlaMapView
import dagger.hilt.android.AndroidEntryPoint
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class HomeActivity : AppCompatActivity(), PermissionRequest.Listener, MapStatusCallback { //OnMapReadyCallback

    companion object{
        private const val TAG = "Home Activity"
        var instance: HomeActivity? = null
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

    private val navigationViewModel: NavigationViewModel by viewModels()

    private val userViewModel: UserDataViewModel by viewModels()

    private val masterViewModel: MasterViewModel by viewModels()

    private var currentDistrict = ""

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var olaMapView: OlaMapView

    private var selectedVehicle = SelectVehicle.BIKE

    private var userId = 0


    private var planTripStatus = true

    private var receiveNearByRiderListener: Emitter.Listener? = null

    private val request by lazy {
        permissionsBuilder(Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION).build()
    }

    private lateinit var loader: AlertDialog

    private lateinit var locationCallback: LocationCallback

    private val GPSLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(AppUtils.sharedInstance.isLocationEnabled(this)){
            request.send()
        }else{
            promptUserToEnableLocation()
        }
    }

    private val currentLocationMarkerViewOptions = OlaMarkerOptions.Builder()
        .setMarkerId("current_location")
        .setIconIntRes(R.drawable.ic_location)
        .setIconSize(0.05f)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        olaMapView = binding.mapView
        olaMapView.onCreate(savedInstanceState)

        instance = this

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        request.addListener(this)

        loader = getLoadingDialog(this)

        userId = sharedPrefManager.getInt(Constants.USER_ID)

        sharedPrefManager.putString(Constants.TEMP_BOOKING_ID, "")

        sharedPrefManager.saveEnum(Constants.RIDE_STATUS, BookingStatus.NO_RIDE)

        masterViewModel.walletBalanceData.observe(this) { balance ->
            binding.walletBalanceText.text = String.format("₹ %s", balance)
        }

        cabViewModel.fetchCabs()

        binding.addMoneyBtn.setOnSingleClickListener {
            val sheet = AddMoneyFragment()
//            val existingSheet = supportFragmentManager.findFragmentByTag(sheet.tag)
//            if (existingSheet == null) {
//                sheet.show(supportFragmentManager, sheet.tag)
//            }

            showBottomSheetSafely(sheet.tag.toString()) { sheet }

//            val bottomSheetFragment = AddMoneyFragment()
//            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

        if(AppUtils.sharedInstance.isLocationEnabled(this)){
            request.send()
        }else{
            promptUserToEnableLocation()
        }

        selectedVehicle = sharedPrefManager.getEnum(Constants.VEHICLE_TYPE, SelectVehicle::class.java ,SelectVehicle.BIKE)

        setActiveTab(selectedVehicle)

//  ************************* Local Data access ****************************************************
        userViewModel.allUsers.observe(this, Observer { userList ->
            if (userList.isNotEmpty()) {
                binding.helloNameText.text = String.format("Hello, %s!", userList[0].userName)

            }
        })
//  ************************* Local Data access ****************************************************

        binding.sectionOneRightDashboard.setOnSingleClickListener {
            planTrip()
        }

        binding.searchBarDashboard.setOnSingleClickListener {
            planTrip()
        }

        binding.settingBtnDashboard.setOnSingleClickListener {
            val sheet = DrawerFragment()
//            val existingSheet = supportFragmentManager.findFragmentByTag(sheet.tag)
//            if (existingSheet == null) {
//                sheet.show(supportFragmentManager, sheet.tag)
//            }

            showBottomSheetSafely(sheet.tag.toString()) { sheet }

//            val bottomSheetFragment = DrawerFragment()
//            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

        binding.sectionTwoRightDashboard.setOnSingleClickListener {
            val sheet = RecentRideHistoryFragment()
//            val existingSheet = supportFragmentManager.findFragmentByTag(sheet.tag)
//            if (existingSheet == null) {
//                sheet.show(supportFragmentManager, sheet.tag)
//            }

            showBottomSheetSafely(sheet.tag.toString()) { sheet }

//            val bottomSheetFragment = RecentRideHistoryFragment()
//            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

        binding.bikeBox.setOnSingleClickListener {
            setActiveTab(SelectVehicle.BIKE)
            planTrip()
        }

        binding.autoBox.setOnSingleClickListener {
            setActiveTab(SelectVehicle.AUTO)
            planTrip()
        }

        binding.cabBox.setOnSingleClickListener {
            setActiveTab(SelectVehicle.CABS)
            planTrip()
        }

        binding.parcelBox.setOnSingleClickListener {
            setActiveTab(SelectVehicle.PARCEL)
            planTrip()
        }

        binding.bikeLiteBox.setOnSingleClickListener {
            setActiveTab(SelectVehicle.BIKE_LITE)
            planTrip()
        }

        binding.sectionTwoLeftDashboard.setOnSingleClickListener {
            val sheet = CancelRideFragment()
            val existingSheet = supportFragmentManager.findFragmentByTag(sheet.tag)
            if (existingSheet == null) {
                sheet.show(supportFragmentManager, sheet.tag)
            }

//            val bottomSheetFragment = CancelRideFragment()
//            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
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




//  ************************* Socket Listener ***********************************************************
        socketManager.setOnSocketConnectedListener(object : SocketManager.OnSocketConnectedListener {
            override fun onConnected() {
                socketManager.listenToEvent(SocketConstants.RECEIVE_NEARBY_RIDERS) { data ->
                    val riders = data.getJSONArray("data")
                    ShowDrivers(riders)
                }

                socketManager.listenToEvent("welcomeMessage") { data ->
                    Log.d(TAG, "onCreate: test listen data on Raido $data")
                }
            }
        })
//  ************************* Socket Listener ***********************************************************

//  ************************* API Response Observer ************************************************
        cabViewModel.nearByCabsResponseLiveData.observe(this, Observer { response ->
            when (response) {
                is NetworkResult.Success -> {
                    hideLoader(this, loader)

//                    val driversAry = response.data?.nearbyRiders

                    val serviceCategoryAry = response.data?.serviceCategories ?: ArrayList()

                    val balance =  response.data?.walletBalance

                    masterViewModel.setWalletBalanceData(balance.toString())

                    binding.walletBalanceText.text = balance.toString()

                    val aboutUsData = response.data?.aboutUsModel

                    if(aboutUsData != null){
                        sharedPrefManager.putString(Constants.PRIVACY_POLICY, aboutUsData.privacyPolicy)
                        sharedPrefManager.putString(Constants.TERM_CONDITION, aboutUsData.termsCondition)
                        sharedPrefManager.putString(Constants.JOIN_TERM, aboutUsData.joinTheTeam)
                        sharedPrefManager.putString(Constants.BLOG, aboutUsData.blog)
                    }

                    Log.d(TAG, "onMapReady: $serviceCategoryAry")

                    if(serviceCategoryAry.isNotEmpty()){

                        for (vehicleDetail in serviceCategoryAry){
                            when(vehicleDetail.vehicleType){
                                "Bike" -> {
                                    sharedPrefManager.putInt(Constants.BIKE_ID, vehicleDetail.vehicleId )
                                }
                                "Auto" -> {
                                    sharedPrefManager.putInt(Constants.AUTO_ID, vehicleDetail.vehicleId )
                                }
                                "Cabs" -> {
                                    sharedPrefManager.putInt(Constants.CAB_ID, vehicleDetail.vehicleId )
                                }
                                "Bike Lite" -> {
                                    sharedPrefManager.putInt(Constants.BIKE_LITE_ID, vehicleDetail.vehicleId )
                                }
                                else -> {
                                    Log.d(TAG, "onCreate: bike id not found ")
                                }
                            }
                        }

//                        val bikeAry = serviceCategoryAry.filter { it.vehicleType == "Bike" }
//                        val autoAry = serviceCategoryAry.filter { it.vehicleType == "Auto" }
//                        val cabAry = serviceCategoryAry.filter { it.vehicleType == "Cabs" }
//                        val bikeLiteAry = serviceCategoryAry.filter { it.vehicleType == "Bike Lite" }
//
//                        if(bikeAry.isNotEmpty()){
//                            sharedPrefManager.putInt(Constants.BIKE_ID, bikeAry[0].vehicleId )
//                        }
//                        if(autoAry.isNotEmpty()) {
//                            sharedPrefManager.putInt( Constants.AUTO_ID, autoAry[0].vehicleId )
//                        }
//                        if(cabAry.isNotEmpty()) {
//                            sharedPrefManager.putInt( Constants.CAB_ID, cabAry[0].vehicleId )
//                        }
//                        if(bikeLiteAry.isNotEmpty()) {
//                            sharedPrefManager.putInt(Constants.BIKE_LITE_ID, bikeLiteAry[0].vehicleId)
//                        }
                    }

                }
                is NetworkResult.Error -> {
                    hideLoader(this, loader)
                    alertDialog.alertDialogAnim(
                        this,
                        response.message.toString(),
                        R.raw.failed
                    )
                    Log.d(TAG, "bindObservers: response received => Error = ${response.message}")
                }
                is NetworkResult.Loading ->{
                    showLoader(this, loader)
                }
                is NetworkResult.Empty -> {
                    hideLoader(this, loader)
                }
            }
        })

//        navigationViewModel.geoCodingData.observe(this) { geoCodingRes ->
//
//            if(geoCodingRes != null) {
//                val geoCodingData =
//                    geoCodingRes.results[0].addressComponents.filter { it.types.contains("locality") }
//
//                if (!geoCodingData.isNullOrEmpty()) {
//                    sharedPrefManager.putString(
//                        Constants.CURRENT_DISTRICT,
//                        geoCodingData[0].longName.toString()
//                    )
//                }else{
//                    sharedPrefManager.putString(
//                        Constants.CURRENT_DISTRICT,
//                        currentDistrict
//                    )
//                }
//            }else{
//                sharedPrefManager.putString(
//                    Constants.CURRENT_DISTRICT,
//                    currentDistrict
//                )
//            }
//
//        }

        navigationViewModel.fetchDistrictResponseLiveData.observe(this, Observer {
            Log.d(TAG, "bindObservers: response received  => ${it}")
            when (it) {
                is NetworkResult.Success -> {

                    hideLoader(this, loader)

                    val geoCodingRes = it.data

                    if(geoCodingRes != null) {
                        val geoCodingData =
                            geoCodingRes.results[0].addressComponents.filter { it.types.contains("locality") }

                        if (!geoCodingData.isNullOrEmpty()) {
                            sharedPrefManager.putString(
                                Constants.CURRENT_DISTRICT,
                                geoCodingData[0].longName.toString()
                            )
                        }else{
                            sharedPrefManager.putString(
                                Constants.CURRENT_DISTRICT,
                                currentDistrict
                            )
                        }
                    }else{
                        sharedPrefManager.putString(
                            Constants.CURRENT_DISTRICT,
                            currentDistrict
                        )
                    }

                }
                is NetworkResult.Error -> {
                    hideLoader(this, loader)

                    alertDialog.alertDialogAnim(
                        this,
                        it.message.toString(),
                        R.raw.failed
                    )
                    Log.d(TAG, "bindObservers: response received => Error = ${it.message}")
                }
                is NetworkResult.Loading ->{
                    showLoader(this, loader)
                }
                is NetworkResult.Empty -> {
                    hideLoader(this, loader)
                }
            }
        })

//  ************************* API Response Observer ************************************************




    }

    override fun onResume() {
        super.onResume()

        if(::olaMapView.isInitialized)
            olaMapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        socketManager.socketConnect()
    }


    override fun onPause() {

//        olaMapView.onPause()
        super.onPause()

    }

    override fun onLowMemory() {
        super.onLowMemory()
        olaMapView.onLowMemory()
    }

    private fun planTrip(){

       val vehicleId = when(sharedPrefManager.getEnum(Constants.VEHICLE_TYPE, SelectVehicle::class.java ,SelectVehicle.BIKE)){
            SelectVehicle.BIKE -> {
                sharedPrefManager.getInt(Constants.BIKE_ID, 0)
            }
            SelectVehicle.CABS -> {
                sharedPrefManager.getInt(Constants.CAB_ID, 0)
            }
            SelectVehicle.AUTO -> {
                sharedPrefManager.getInt(Constants.AUTO_ID, 0)
            }
            else -> {
                sharedPrefManager.getInt(Constants.BIKE_ID, 0)
            }
        }
        Log.d(TAG, "planTrip: selected vehicle id is ${selectedVehicle}")

        if(vehicleId != 0) {
            planTripStatus = true

            val sheet = PlanTripFragment()
//            val existingSheet = supportFragmentManager.findFragmentByTag(sheet.tag)
//            if (existingSheet == null) {
//                sheet.show(supportFragmentManager, sheet.tag)
//            }

            showBottomSheetSafely(sheet.tag.toString()){
                sheet
            }

//            val bottomSheetFragment = PlanTripFragment()
//            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }else{
            planTripStatus = false
            val currentLatLng = sharedPrefManager.getLatLng(Constants.CURRENT_LATLNG)
            if(currentLatLng != null){
                cabViewModel.fetchCabs()
            }
        }
    }

    //call onStop() of SDK before removing the OlaMaps
    override fun onStop() {
//        olaMapView?.onStop()
        super.onStop()

    }

    override fun onDestroy() {
        olaMapView?.onDestroy()
        super.onDestroy()

        instance = null

        if(::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
//        socketManager.socketDisconnect()

        masterViewModel.walletBalanceData.removeObservers(this)

        receiveNearByRiderListener?.let {
            socketManager.removeListener(SocketConstants.RECEIVE_NEARBY_RIDERS, it)
        }
        receiveNearByRiderListener = null

    }

    override fun onMapReady() {

        olaMapView.hideCurrentLocationMarker()
        olaMapView.toggleLocationComponent(false)

    }

    override fun onMapLoadFailed(p0: String?) {
        Log.d(TAG, "onMapLoadFailed: error on map load => ${p0}")
    }

    private fun setActiveTab(keyVal: SelectVehicle){

        sharedPrefManager.saveEnum(Constants.VEHICLE_TYPE, keyVal)

        val activeColor = ContextCompat.getColor(this, R.color.bg_dark)
        val inactiveColor = ContextCompat.getColor(this, R.color.white)

        val currentLatLng = sharedPrefManager.getLatLng(Constants.CURRENT_LATLNG)

        if(currentLatLng != null){
            val json = JSONObject().apply {
                put("latitude", currentLatLng.latitude)
                put("longitude", currentLatLng.longitude)
                put("vehicle_type", keyVal.name)
                put("socketId", socketManager.getSocketId())
            }
            socketManager.sendData(SocketConstants.GET_NEAR_BY_RIDERS, json){status ->

            }

        }

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

                try {

                    //call initialize function of OlaMapView with custom configuration
                    olaMapView.initialize(
                        mapStatusCallback = this,
                        olaMapsConfig = OlaMapsConfig.Builder()
                            .setApplicationContext(applicationContext) //pass the application context here, it is mandatory
                            .setMapBaseUrl("https://api.olamaps.io") // pass the Base URL of Ola-Maps here (Stage/Prod URL), it is mandatory
                            .setApiKey("B99cOWJwLYnmj1ewxa8RoZYQTqXQbfYfJlRZFrKb")
                            .setProjectId("d9485b50-6b6c-4f42-8a7a-14bd33a758f7") //Pass the Origination ID here, it is mandatory
                           // .setMapTileStyle(MapTileStyle.DEFAULT_DARK_LITE) //pass the MapTileStyle here, it is Optional.
                            .setMinZoomLevel(8.0)
                            .setMaxZoomLevel(20.0)
                            .setZoomLevel(15.0)
                            .showCurrentLocation(false)
                            .build()
                    )


//                    olaMapView.registerCurrentLocationUpdate( object :
//                        OnOlaMapLocationUpdateCallback {
//                        override fun onLocationUpdated(location: Location) {
//                            Log.d(TAG, "onLocationUpdated: OlaMap Update data is ${location.latitude}, ${location.longitude}")
//                        }
//
//                    })

                    if(::olaMapView.isInitialized) {

//                        olaMapView.toggleLocationComponent(false)

//                        olaMapView.moveToCurrentLocation()


//                        val navigationOption = NavigationViewOptions.builder()
//                            .shouldSimulateRoute(true)
//                            .progressChangeListener { location, routeProgress ->  }
//                            .build()
//
//                        olaMapView.startNavigation(navigationOption)
                        
                        

                        

//                 Handle button click
                        getCurrentLocation { latitude, longitude ->
                            val userLatLng = OlaLatLng(latitude, longitude)
                            olaMapView.moveCameraToLatLong(
                                userLatLng,
                                zoomLevel = 15.0,
                                1000
                            ) // Move camera to current location with zoom level

//                            olaMapView.addMarkerView(
//                                OlaMarkerOptions.Builder()
//                                    .setMarkerId(currentLocationMarkerViewOptions.markerId)
//                                    .setPosition(
//                                        userLatLng
//                                    )
//                                    .setIconIntRes(currentLocationMarkerViewOptions.iconIntRes!!)
//                                    .setIconSize(currentLocationMarkerViewOptions.iconSize)
//                                    .build()
//                            )

                            Handler(Looper.myLooper()!!).postDelayed({
                                val json = JSONObject().apply {
                                    put("latitude", latitude)
                                    put("longitude", longitude)
                                    put("vehicle_type", selectedVehicle.name)
                                    put("socketId", socketManager.getSocketId())
                                }
                                socketManager.sendData(SocketConstants.GET_NEAR_BY_RIDERS, json){status ->

                                }
                            },4000)

//                            olaMapView.registerCurrentLocationUpdate(object: OnOlaMapLocationUpdateCallback{
//                                override fun onLocationUpdated(location: Location) {
//                                    Log.d(TAG, "onLocationUpdated: location data is ${location}")
//                                }
//                            })


                        }
                    }else{
                        showToast("Something went wrong with olaMapView")
                    }

                }catch (err: Exception){
                    Log.d(TAG, "onPermissionsResult: error occor on that ${err}")
                }



            }
        }
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

                    getStateAndCityFromLatLng(latLngData.latitude, latLngData.longitude)

                    hideLoader(this@HomeActivity, loader)
                } ?: run {
                    // Handle location not available
                }
            }
        } else {
            request.send()
        }
    }

    private fun getStateAndCityFromLatLng(latitude: Double, longitude: Double) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            Log.d(TAG, "getStateAndCityFromLatLng: update current district => $addresses")
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val city = address.locality ?: address.subAdminArea
                val state = address.adminArea

                currentDistrict = city.toString()
                sharedPrefManager.putString(
                    Constants.CURRENT_DISTRICT,
                    currentDistrict.toString()
                )
            }else{
                navigationViewModel.fetchDistrictAPI(OlaLatLng(latitude, longitude))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            navigationViewModel.fetchDistrictAPI(OlaLatLng(latitude, longitude))
        }
    }



}