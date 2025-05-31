package com.my.raido.models

import com.google.gson.annotations.SerializedName

data class RecentRideDetailResponse(@SerializedName("message") val message: String, @SerializedName("data") val recentRideDetail: RecentRideDetail, @SerializedName("status") val status: String, @SerializedName("base_url") val baseUrl: String)

data class RecentRideDetail(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("rider_id") val riderId: String,
    @SerializedName("pickup_location") val pickupLocation: String,
    @SerializedName("drop_location") val dropLocation: String,
    @SerializedName("ride_date") val rideDate: String,
    @SerializedName("fare") val farePrice: String,
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("completed") val completedRide: String,
    @SerializedName("service_categories_id") val serviceCategoriesId: Int,
    @SerializedName("rider_name") val driverName: String,
    @SerializedName("rider_image") val driverImg: String,
    @SerializedName("final_fare_after_discount") val finalFareAfterDiscount: String,
    @SerializedName("rating") val ratting: Float,
    @SerializedName("ride_id") val rideId: String,
    @SerializedName("duration") val duration: String,
    @SerializedName("distance") val distance: String,
    @SerializedName("ride_charge") val rideCharge: String,
    @SerializedName("booking_fees") val bookingFees: String,
    @SerializedName("night_fare") val nightFare: String,
    @SerializedName("discount") val discount: String,
    @SerializedName("waiting_time") val waitingTime: String,
    @SerializedName("waiting_time_fare") val waitingTimeFare: String,
    @SerializedName("base_fare") val baseFare: String,
    @SerializedName("distance_fare") val distanceFare: String,
    @SerializedName("time_fare") val timeFare: String
)
