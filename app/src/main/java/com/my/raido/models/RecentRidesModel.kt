package com.my.raido.models

import com.google.gson.annotations.SerializedName

//class RecentRidesModel {
//}

data class RecentRidesModel(@SerializedName("message") val message: String, @SerializedName("data") val rideList: ArrayList<RidesData>, @SerializedName("status") val status: String)

data class RidesData(@SerializedName("pickup_location") val pickupLocation: String, @SerializedName("drop_location") val dropLocation: String, @SerializedName("ride_date") val rideDate: String, @SerializedName("id") val rideId: String, @SerializedName("fare") val price: String)
