package com.my.raido.models.book_ride

import com.google.gson.annotations.SerializedName


data class BookRideRequest(
    @SerializedName("booking_id") val bookingId: String,
    @SerializedName("pickupAddress") val pickupAddress: String,
    @SerializedName("dropAddress") val dropAddress: String,
    @SerializedName("service_categories_id") val serviceCategoriesId: String,
    @SerializedName("paymentMode") val paymentMode: Int,
    @SerializedName("distance") val distance: String
)

fun BookRideRequest.toMap(): Map<String, Any> {
    return mapOf(
        "booking_id" to this.bookingId,
        "pickupAddress" to this.pickupAddress,
        "dropAddress" to this.dropAddress,
        "service_categories_id" to this.serviceCategoriesId,
        "paymentMode" to this.paymentMode,
        "distance" to this.distance
    )
}
