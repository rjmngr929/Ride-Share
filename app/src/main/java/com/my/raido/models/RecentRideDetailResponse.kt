package com.my.raido.models

import com.google.gson.annotations.SerializedName

data class RecentRideDetailResponse(@SerializedName("message") val message: String, @SerializedName("data") val recentRideDetail: RecentRideDetail, @SerializedName("status") val status: String)

data class RecentRideDetail(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("rider_id") val riderId: String,
    @SerializedName("pickup_location") val pickupLocation: String,
    @SerializedName("drop_location") val dropLocation: String,
    @SerializedName("ride_date") val rideDate: String,
    @SerializedName("fare") val farePrice: String,
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("completed") val completedRide: Boolean,
    @SerializedName("driver_name") val driverName: String,
    @SerializedName("rating") val ratting: Float,
    @SerializedName("ride_id") val rideId: String,
    @SerializedName("duration") val duration: String,
    @SerializedName("distance") val distance: String,
    @SerializedName("ride_charge") val rideCharge: String,
    @SerializedName("booking_fees") val bookingFees: String,
    @SerializedName("discount") val discount: String
)
