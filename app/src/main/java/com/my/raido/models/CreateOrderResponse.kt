package com.my.raido.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CreateOrderResponse (
    @SerializedName("status") val status: Boolean = false,
    @SerializedName("gateway_name") val gatewayName: String = "",
    @SerializedName("clientId") val clientId: String = "",
    @SerializedName("order_id") val orderId: String = "",
    @SerializedName("cf_order_id") val cfOrderId: String = "",
    @SerializedName("payment_session_id") val paymentSessionId: String = "",
) : Serializable
