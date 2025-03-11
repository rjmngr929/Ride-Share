package com.my.raido.models.cab

import com.google.gson.annotations.SerializedName


data class RideBookRequest(
    @SerializedName("pickup_latitude") val pickuplat: Double,
    @SerializedName("pickup_longitude") val pickupLng: Double,
    @SerializedName("dropoff_latitude") val dropLat: Double,
    @SerializedName("dropoff_longitude") val dropLng: Double,
    @SerializedName("categorie_id") val vehicleId: Int,
    @SerializedName("district") val district: String
)

fun RideBookRequest.toMap(): Map<String, Any> {
    return mapOf(
        "pickup_latitude" to this.pickuplat,
        "pickup_longitude" to this.pickupLng,
        "dropoff_latitude" to this.dropLat,
        "dropoff_longitude" to this.dropLng,
        "categorie_id" to this.vehicleId,
        "district" to this.district
    )
}
