package com.my.raido.models.RideDataModels

import java.io.Serializable


data class RideRequest(val userId: String, val pickupLat: Double, val pickupLng: Double, val pickupAddress: String, val dropLat: Double, val dropLng: Double, val dropAddress: String, val paymentMode: String, val farePrice: String, val fcmToken: String, val distance: String, val serviceCategory: Int, val rideDuration : String, val selectedVehicleType: String, val bookingId: String, val status: String = "pending" ): Serializable