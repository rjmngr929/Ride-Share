package com.my.raido.models.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class DriverDetailModel(
    @SerializedName("status") val status: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val driverDetail: DriverDetail = DriverDetail()
) : Serializable

data class DriverDetail(
    @SerializedName("rider_id") val driverId: Int = 0,
    @SerializedName("name") val driverName: String = "",
    @SerializedName("rating") val rating: Double = 0.0,
    @SerializedName("vehicle") val vehicle: String = "",
    @SerializedName("vehicle_name") val vehicleName: String = "",
    @SerializedName("vehicle_number") val vehicleNumber: String = "",
    @SerializedName("otp") val otp: String = "",
    @SerializedName("profile_image") val profileImage: String = "",
) : Serializable

