package com.my.raido.models.login

import com.google.gson.annotations.SerializedName
import com.my.raido.models.profile.UserDataModel


data class OTPVerifyResponse(
    @SerializedName("status") val status: Boolean = false,
    @SerializedName("access_token") val authToken: String = "",
    @SerializedName("user") val userData: UserDataModel,
)

