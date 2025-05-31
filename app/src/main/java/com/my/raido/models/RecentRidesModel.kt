package com.my.raido.models

import com.google.gson.annotations.SerializedName

//class RecentRidesModel {
//}

data class RecentRidesModel(@SerializedName("message") val message: String, @SerializedName("data") val rideList: MutableList<RidesData>, @SerializedName("status") val status: String)

data class RidesData(@SerializedName("pickup_location") val pickupLocation: String, @SerializedName("drop_location") val dropLocation: String, @SerializedName("ride_date") val rideDate: String, @SerializedName("id") val rideId: String, @SerializedName("final_fare_after_discount") val price: String, @SerializedName("completed") val rideStatus: String  )
