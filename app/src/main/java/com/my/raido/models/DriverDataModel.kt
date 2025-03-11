package com.my.raido.models

//data class DriverDataModel(
//    var driverId: Int = 0,
//    var latitude: Double = 0.0,
//    var longitude: Double = 0.0,
//    var onDuty: Boolean = false,
//    var status: String = "unavailable",
//    var fcmToken: String = "",
//    var vehicleType: String = "",
//    var rideDetail : RideRequest?
//) {
//    // 🔹 Firebase requires an empty constructor
//    constructor() : this(0, "", 0.0, 0.0, false, "", "")
//}

data class DriverDataModel(
    val driverId: Int = 0,
    val fcmToken: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val onDuty: Boolean = false,
    val status: String = "",
    val vehicleType: String = ""
) {
    // 🔹 Default constructor (No-arg constructor for Firebase)
    constructor() : this(0, "", 0.0, 0.0, false, "", "")
}
