package com.my.raido.models.book_ride

import com.google.gson.annotations.SerializedName


data class BookRideResponse(
    @SerializedName("message") val message: String = "",
    @SerializedName("status") val status: Boolean,
    @SerializedName("booking_id") val bookingId: String = ""
)
