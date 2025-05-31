package com.my.raido.models.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class DashboardDataModel(
    @SerializedName("status") val status: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("wallet") val wallet: String = "",
    @SerializedName("ride_otp") val rideOtp: String = "",
    @SerializedName("base_url") val imgUrl: String = "",
    @SerializedName("rideHistories") val rideHistories: RideHistories? = null
) : Serializable

data class RideHistories(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("ride_id") val rideId: String = "",
    @SerializedName("user_id") val userId: Int = 0,

    @SerializedName("pickup_location") val pickupLocation: String = "",
    @SerializedName("drop_location") val dropLocation: String = "",

    @SerializedName("fare") val fare: String = "",
    @SerializedName("payment_method") val paymentMethod: String = "",
    @SerializedName("service_categories_id") val serviceCategoriesId: Int = 0,
    @SerializedName("completed") val completed: String = "",
    @SerializedName("canceled_by") val canceledBy: String? = null,
    @SerializedName("ride_charge") val rideCharge: String = "",
    @SerializedName("temp_bookings_id") val tempBookingsId: String = "",
    @SerializedName("distance") val distance: String = "",
    @SerializedName("duration") val duration: String = "",
    @SerializedName("booking_fees") val bookingFees: String = "",
    @SerializedName("discount") val discount: String = "",
    @SerializedName("base_fare") val baseFare: String = "",
    @SerializedName("distance_fare") val distanceFare: String = "",
    @SerializedName("time_fare") val timeFare: String = "",
    @SerializedName("platform_fee") val platformFee: String = "",
    @SerializedName("final_fare_before_discount") val finalFareBeforeDiscount: String = "",
    @SerializedName("final_fare_after_discount") val finalFareAfterDiscount: String = "",
    @SerializedName("night_fare") val nightFare: String = "",
    @SerializedName("pickupLat") val pickupLat: String = "",
    @SerializedName("pickupLng") val pickupLng: String = "",
    @SerializedName("dropLat") val dropLat: String = "",
    @SerializedName("dropLng") val dropLng: String = "",
    @SerializedName("cancellation_reason") val cancellationReason: String = "",

    @SerializedName("rider_id") val driverId: Int = 0,
    @SerializedName("rider_name") val driverName: String = "",
    @SerializedName("rider_image") val driverImage: String = "",
    @SerializedName("rider_phone") val driverMobile: String = "",
    @SerializedName("ride_date") val rideDate: String = "",
    @SerializedName("rating") val rating: Double = 0.0,

    @SerializedName("vehicle_type") val vehicle: String = "",
    @SerializedName("vehicle_name") val vehicleName: String = "",
    @SerializedName("vehicle_number") val vehicleNumber: String = "",

) : Serializable

