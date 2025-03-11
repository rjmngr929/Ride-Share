package com.my.raido.services

//import com.ola.mapsdk.camera.MapControlSettings
//import com.ola.mapsdk.interfaces.OlaMapCallback
//import com.ola.mapsdk.view.OlaMap
//import com.ola.mapsdk.view.OlaMapView

object OlaMapManager {
//    @SuppressLint("StaticFieldLeak")
//    var mapInstance: OlaMap? = null // Replace OlaMap with your map type, e.g., GoogleMap if you're using Google Maps SDK
//
//    val mapControlSettings = MapControlSettings.Builder()
//        .setRotateGesturesEnabled(false)
//        .setScrollGesturesEnabled(true)
//        .setZoomGesturesEnabled(true)
//        .setCompassEnabled(false)
//        .setTiltGesturesEnabled(false)
//        .setDoubleTapGesturesEnabled(false)
//        .build()
//
//
//
//    fun initializeMap(mapView: OlaMapView, onMapReady: (OlaMap) -> Unit) {
//
//        if (mapInstance == null) {
//
//            mapView.getMap(apiKey = "1mthwdRIjnbT77e4xJUcLserilOY5BhRtn5sQa4S",
//                olaMapCallback = object : OlaMapCallback {
//                    override fun onMapError(error: String) {
//                        Log.d("TAG", "onMapError: Something went wrong on map load")
//                    }
//
//                    override fun onMapReady(olaMap: OlaMap) {
//                        mapInstance = olaMap
//                        onMapReady(olaMap)
//                    }
//
//                }, mapControlSettings)
//        } else {
//            onMapReady(mapInstance!!)
//        }
//    }
}