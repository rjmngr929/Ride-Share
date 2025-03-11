package com.my.raido.ui.map

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.my.raido.R
import com.my.raido.Utils.AnimationUtils
import com.my.raido.Utils.AppUtils
import com.my.raido.Utils.MapUtils
import com.my.raido.Utils.gone
import com.my.raido.Utils.visible
import com.my.raido.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), MapsView, OnMapReadyCallback {

    companion object{
        private const val TAG = "Maps Activity"

        private const val REQUEST_LOCATION_PERMISSION_REQUEST_CODE = 120
        private const val PICKUP_REQUEST_CODE = 142
        private const val DROP_REQUEST_CODE = 210
    }

    private lateinit var binding: ActivityMapsBinding

    private lateinit var presenter: MapsPresenter
    private lateinit var googleMap: GoogleMap

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var locationCallback: LocationCallback
    private var currentLatLng: LatLng? = null
    private var pickUpLatLng: LatLng? = null
    private var dropLatLng: LatLng? = null
    private var greyPolyLine: Polyline? = null
    private var blackPolyLine: Polyline? = null
    private val nearByCarMarketList = arrayListOf<Marker>()
    private var originMarker: Marker? = null
    private var destinationMarker: Marker? = null
    private var movingCabMarker: Marker? = null
    private var previousLatLngFromServer: LatLng? = null
    private var currentLatLngFromServer: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        AppUtils.sharedInstance.enableTransparentStatusBar(window)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        presenter = MapsPresenter(NetworkService())
        presenter.onAttach(this)
        setOnClickListener()

    }

    private fun setOnClickListener() {
        binding.pickUpTextView.setOnClickListener {
            launchLocationAutoCompleteActivity(PICKUP_REQUEST_CODE)
        }

        binding.dropTextView.setOnClickListener {
            launchLocationAutoCompleteActivity(DROP_REQUEST_CODE)
        }

        binding.requestCabButton.setOnClickListener {
            binding.statusTextView.visible()
            binding.statusTextView.text = getString(R.string.requesting_your_cab)
            binding.requestCabButton.isEnabled = false
            binding.pickUpTextView.isEnabled = false
            binding.dropTextView.isEnabled = false
            presenter.requestCab(pickUpLatLng!!, dropLatLng!!)
        }

        binding.nextRideButton.setOnClickListener {reset()}
    }

    private fun launchLocationAutoCompleteActivity(requestCode: Int) {
        try {
            val fields: List<Place.Field> =
                listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
            val intent =
                Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this)
            startActivityForResult(intent, requestCode)
        }catch (err: Exception){
            Log.d(TAG, "launchLocationAutoCompleteActivity: error => $err")
        }
      
    }

    private fun moveCamera(latLng: LatLng) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    private fun animateCamera(latLng: LatLng) {
        val cameraPosition = CameraPosition.Builder().target(latLng).zoom(15.5f).build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun addCarMarketAndGet(latLng: LatLng): Marker? {
        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(MapUtils.getCarBitmap(this, R.drawable.ic_car))
        return googleMap.addMarker(MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor))

//        val bitmap = MapUtils.getCarBitmap(this, R.drawable.ic_car)
//        if (bitmap != null) {
//            val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
//           return googleMap.addMarker(MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor))
//        } else {
//            // Handle the error case, e.g., log an error or use a default bitmap
//            Log.e("BitmapError", "Bitmap is null")
//            return null
//        }

    }

    private fun enableMyLocationMap() {
        googleMap.setPadding(0, 48, 0, 0)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        googleMap.isMyLocationEnabled = true
    }

    private fun addOriginDestinationMarkerAndGet(latLng: LatLng): Marker? {
        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(MapUtils.getDestinationBitmap())
        return googleMap.addMarker(MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor))
    }

    private fun setCurrentLocationAsPickup() {
        pickUpLatLng = currentLatLng
        binding.pickUpTextView.text = getString(R.string.current_location)
    }

    private fun setupLocationListener() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest().setInterval(2000).setFastestInterval(2000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        //get location updates
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationRequest: LocationResult) {
                super.onLocationResult(locationRequest)
                //taking current location
                if (currentLatLng == null) {
                    for (location in locationRequest.locations) {
                        if (currentLatLng == null) {
                            currentLatLng = LatLng(location.latitude, location.longitude)
                            setCurrentLocationAsPickup()
                            enableMyLocationMap()
                            moveCamera(currentLatLng!!)
                            animateCamera(currentLatLng!!)

                            presenter.requestNearbyCabs(currentLatLng!!)
                        }
                    }
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )

    }

    private fun checkAndShowRequestButton() {
        if (pickUpLatLng != null && dropLatLng != null) {
            binding.requestCabButton.visible()
            binding.requestCabButton.isEnabled = true
        }
    }

    private fun reset(){
        binding.statusTextView.gone()
        binding.nextRideButton.gone()
        nearByCarMarketList.forEach {
            it.remove()
        }
        nearByCarMarketList.clear()
        currentLatLngFromServer = null
        previousLatLngFromServer = null
        if(currentLatLng != null){
            moveCamera(currentLatLng!!)
            animateCamera(currentLatLng!!)
            setCurrentLocationAsPickup()
            presenter.requestNearbyCabs(currentLatLng!!)
        }else{
            binding.pickUpTextView.text = ""
        }

        binding.pickUpTextView.isEnabled = true
        binding.dropTextView.isEnabled = true
        binding.dropTextView.text = ""
        movingCabMarker?.remove()
        greyPolyLine?.remove()
        blackPolyLine?.remove()
        originMarker?.remove()
        destinationMarker?.remove()
        dropLatLng = null
        greyPolyLine = null
        blackPolyLine = null
        originMarker = null
        destinationMarker = null
        movingCabMarker = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
    }

    override fun onStart() {
        super.onStart()

        setupLocationListener()

//        if (PermissionUtils.isFineLocationGranted(this)) {
//            if (PermissionUtils.isLocationEnabled(this)) {
//                //get location
//                setupLocationListener()
//            } else {
//                PermissionUtils.showGPSNotEnabled(this)
//            }
//        } else {
//            PermissionUtils.requestAccessFineLocationPermission(
//                this,
//                REQUEST_LOCATION_PERMISSION_REQUEST_CODE
//            )
//        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (AppUtils.sharedInstance.isLocationEnabled(this)) {
                        //get location
                        setupLocationListener()
                    } else {
                        AppUtils.sharedInstance.showGPSNotEnabled(this)
                    }
                } else {
                    Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICKUP_REQUEST_CODE || requestCode == DROP_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    when (requestCode) {
                        PICKUP_REQUEST_CODE -> {
                            binding.pickUpTextView.text = place.name
                            pickUpLatLng = place.latLng
                        }

                        DROP_REQUEST_CODE -> {
                            binding.dropTextView.text = place.name
                            dropLatLng = place.latLng
                            checkAndShowRequestButton()
                        }
                    }
                }

                AutocompleteActivity.RESULT_ERROR -> {
                    //ping for error
                    val status: Status = Autocomplete.getStatusFromIntent(data!!)
                    Log.i(TAG, "Error: " + status.statusMessage)
                }

                AutocompleteActivity.RESULT_CANCELED -> {
                    //logging
                }
            }
        }
    }

    override fun onDestroy() {
        presenter.onDetach()
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }

    override fun showNearByCabs(latLngList: List<LatLng>) {
        nearByCarMarketList.clear()
        for (latLng in latLngList) {
            val nearbyCarMarker = addCarMarketAndGet(latLng)
            if (nearbyCarMarker != null) {
                nearByCarMarketList.add(nearbyCarMarker)
            }
        }
    }

    override fun informCabBooked() {
        nearByCarMarketList.forEach {
            it.remove()
        }

        nearByCarMarketList.clear()
        binding.requestCabButton.gone()
        binding.statusTextView.text = getString(R.string.your_cab_is_booked)
    }

    override fun showPath(latLngList: List<LatLng>) {
        val builder = LatLngBounds.Builder()
        for (latLng in latLngList) {
            builder.include(latLng)
        }
        val bounds = builder.build()
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 2))

        val polylineOptions = PolylineOptions()
        polylineOptions.color(Color.GRAY).width(5f).addAll(latLngList)
        greyPolyLine = googleMap.addPolyline(polylineOptions)

        val blackPolylineOptions = PolylineOptions()
        polylineOptions.color(Color.BLACK).width(5f)
        blackPolyLine = googleMap.addPolyline(blackPolylineOptions)

        originMarker = addOriginDestinationMarkerAndGet(latLngList[0])
        originMarker?.setAnchor(0.5f, 0.5f)

        destinationMarker = addOriginDestinationMarkerAndGet(latLngList[latLngList.size - 1])
        originMarker?.setAnchor(0.5f, 0.5f)

        val polyLineAnimator = AnimationUtils.polyLineAnimator()
        polyLineAnimator.addUpdateListener { valueAnimator ->
            val percentValue = (valueAnimator.animatedValue as Int)
            val index = (greyPolyLine?.points!!.size) * (percentValue / 100.0f).toInt()
            blackPolyLine?.points = greyPolyLine?.points!!.subList(0, index)
        }

        polyLineAnimator.start()
    }

    override fun updateCabLocation(latLng: LatLng) {
        if (movingCabMarker == null) {
            movingCabMarker = addCarMarketAndGet(latLng)
        }

        if (previousLatLngFromServer == null) {
            currentLatLngFromServer = latLng
            previousLatLngFromServer = currentLatLngFromServer
            movingCabMarker?.position = currentLatLngFromServer as LatLng
            movingCabMarker?.setAnchor(0.5f, 0.5f)
            animateCamera(currentLatLngFromServer as LatLng)
        } else {
            previousLatLngFromServer = currentLatLngFromServer
            currentLatLngFromServer = latLng

            val valueAnimator = AnimationUtils.cabAnimator()
            valueAnimator.addUpdateListener { va ->
                if (currentLatLngFromServer != null && previousLatLngFromServer != null){
                    val multiplier = va.animatedFraction
                    val nextLocation = LatLng(
                        multiplier * currentLatLngFromServer!!.latitude + (1-multiplier) * currentLatLngFromServer!!.latitude,
                        multiplier * currentLatLngFromServer!!.longitude + (1-multiplier) * currentLatLngFromServer!!.longitude
                    )

                    movingCabMarker?.position = nextLocation
                    val rotation = MapUtils.getRotation(previousLatLngFromServer!!, nextLocation)
                    if (!rotation.isNaN()) {
                        movingCabMarker?.rotation = rotation
                    }
                    movingCabMarker?.setAnchor(0.5f, 0.5f)
                    animateCamera(nextLocation)
                }
            }
            valueAnimator.start()
        }
    }

    override fun informCabIsArriving() {
        binding.statusTextView.text = getString(R.string.your_cab_is_arriving)
    }

    override fun informCabArrived() {
        binding.statusTextView.text = getString(R.string.your_cab_has_arrived)
        blackPolyLine?.remove()
        greyPolyLine?.remove()
        originMarker?.remove()
        destinationMarker?.remove()
    }

    override fun informTripStart() {
        binding.statusTextView.text = getString(R.string.you_are_on_a_trip)
        previousLatLngFromServer = null
    }

    override fun informTripEnd() {
        binding.statusTextView.text = getString(R.string.trip_end)
        binding.nextRideButton.visible()
        blackPolyLine?.remove()
        greyPolyLine?.remove()
        originMarker?.remove()
        destinationMarker?.remove()
    }

    override fun showRoutesNoteAvailableError() {
        val error = getString(R.string.route_not_available_choose_different_locations)
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        reset()
    }

    override fun showDirectionApiFailedError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        reset()
    }
}