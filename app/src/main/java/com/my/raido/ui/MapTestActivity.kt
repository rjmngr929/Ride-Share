package com.my.raido.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.my.raido.R
import com.my.raido.databinding.ActivityMapTestBinding

class MapTestActivity : AppCompatActivity() { //, MapStatusCallback, RouteProgressListener, NavigationStatusCallback

    companion object{
        private const val TAG = "Map Test Activity"
    }

    private lateinit var binding: ActivityMapTestBinding

//    private lateinit var navigationRoute: NavigationMapRoute

    var isMapReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMapTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


//        binding.olaMapView.onCreate(savedInstanceState)
//        //call initialize function of OlaMapView with custom configuration
//        binding.olaMapView.initialize(
//            mapStatusCallback = this,
//            olaMapsConfig = OlaMapsConfig.Builder()
//                .setApplicationContext(applicationContext) //pass the application context here, it is mandatory
//                .setMapBaseUrl("https://api.olamaps.io/") // pass the Base URL of Ola-Maps here (Stage/Prod URL), it is mandatory
//                .setApiKey("1mthwdRIjnbT77e4xJUcLserilOY5BhRtn5sQa4S")
//                .setProjectId("bd6f4c73-2798-4281-93cc-eaca0bd184b5") //Pass the Origination ID here, it is mandatory
//                .setMapTileStyle(MapTileStyle.DEFAULT_LIGHT_STANDARD) //pass the MapTileStyle here, it is Optional.
//                .setMinZoomLevel(3.0)
//                .setMaxZoomLevel(21.0)
//                .setZoomLevel(14.0)
//                .build()
//        )
//
//
//        // initialize this variable after onMapReady callback
//        val navigationRoute =  binding.olaMapView.getNavigationMapRoute()
//        val directionsRouteList = arrayListOf<DirectionsRoute>()
// For asynchronous call we can plan for coroutines or any other async call
//        lifecycleScope.launch(Dispatchers.IO) {
//            directionsRouteList.add(transform(routeInfoData))
//        }
//// Once we get the transformed response from SDK, we can call addRoutesForRoutePreview method to show the routes
//        navigationRoute?.addRoutesForRoutePreview(directionsRouteList)


// initialize this variable after onMapReady callback
//        navigationRoute =  binding.olaMapView.getNavigationMapRoute()!!
//        var directionsRoute : DirectionsRoute? = null
//// For asynchronous call we can plan for coroutines or any other async call
//        lifecycleScope.launch(Dispatchers.IO) {
//            directionsRoute = transform(routeInfoData)
//        }
//// Once we get the transformed response from SDK, we can get the NavigationViewOptions instance
//        val navigationViewOptions = getNavigationViewOptions(directionsRoute)
//// Register the listener and start the Navigation for Turn-by-Turn Mode
//        binding.olaMapView.addRouteProgressListener(this@MapTestActivity)
//        binding.olaMapView.registerNavigationStatusCallback(this)
//        binding.olaMapView.startNavigation(navigationViewOptions)


    }

//    //call onStop() of SDK before removing the OlaMaps
//    override fun onStop() {
//        super.onStop()
//        binding.olaMapView.onStop()
//
//        // Remove Route Preview
//        navigationRoute.removeRoute()
//        // Remove All Markers
//        binding.olaMapView.removeAllMarkers()
//    }
//
//    //call onDestroy() of SDK to remove the Maps
//    override fun onDestroy() {
//        binding.olaMapView.onDestroy()
//
//        // Remove Route Preview
//        navigationRoute.removeRoute()
//        // Remove All Markers
//        binding.olaMapView.removeAllMarkers()
//        super.onDestroy()
//    }
//
//    override fun onUpdatedWaypoints(location: List<OlaLatLng?>?) {
//        TODO("Not yet implemented")
//    }
//
//    override fun onRouteProgressChange(instructionModel: InstructionModel?) {
//        instructionModel?.let { it ->
//            val distance = it.currentInstructionModel.getDistance()
//            val imageType = it.currentInstructionModel.getImageType()
//            val updatedInstructionText = it.currentInstructionModel.getTextInstruction()
//        }
//    }
//
//    override fun onOffRoute(p0: Location?) {
//        //Need to call Route-API again for Re-Route state with current location  and destination as getting from onOffRoute
////        val routeInfoData: RouteInfoData = get from the response of Route-API
////        val directionsRoute = transform(routeInfoData) // transform should be called asynchronously
////        binding.olaMapView.updateNavigation(directionsRoute)
//    }
//
//    override fun onArrival() {
//        TODO("Not yet implemented")
//    }
//
//    override fun onMapReady() {
//        TODO("Not yet implemented")
//    }
//
//    override fun onMapLoadFailed(p0: String?) {
//        TODO("Not yet implemented")
//    }
//
//    override fun onNavigationReady() {
//        TODO("Not yet implemented")
//    }
//
//    override fun onNavigationInitError(p0: NavigationErrorInfo?) {
//        TODO("Not yet implemented")
//    }

}