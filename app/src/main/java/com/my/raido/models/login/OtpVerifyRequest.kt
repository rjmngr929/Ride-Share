package com.my.raido.models.login

import retrofit2.http.Field

data class OtpVerifyRequest (
    @Field("mobile_number") val mobileNumber: String,
    @Field("otp_code") val otpCode: String,
    @Field("fcm_token") val fcmToken: String
//    @Field("firebase_token") val fcmToken: String,
//    @Field("device_name") val deviceName: String,
//    @Field("device_model") val deviceModel: String,
//    @Field("device_id") val deviceId: String,
//    @Field("andriod_version") val androidVersion: String
)

fun OtpVerifyRequest.toMap(): Map<String, String> {
    return mapOf(
        "mobile_number" to this.mobileNumber,
        "otp_code" to this.otpCode,
        "fcm_token" to this.fcmToken
//        "firebase_token" to this.fcmToken,
//        "device_name" to this.deviceName,
//        "device_model" to this.deviceModel,
//        "device_id" to this.deviceId,
//        "andriod_version" to this.androidVersion
    )
}