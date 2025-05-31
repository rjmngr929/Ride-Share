package com.my.raido.models.response

import com.google.gson.annotations.SerializedName

data class AddBalanceResModel (@SerializedName("status") val status: Boolean = false, @SerializedName("message") val message: String = "",  @SerializedName("wallet_balance") val walletBalance: String = "")