package com.my.raido.models.response

import com.google.gson.annotations.SerializedName


data class ResponseModel(@SerializedName("status") val status: Boolean = false, @SerializedName("message") val message: String = "")