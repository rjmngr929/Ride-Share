package com.my.raido.models.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class PassbookResModel(
        @SerializedName("status") val status: Boolean = false,
        @SerializedName("message") val message: String = "",
        @SerializedName("data") val trxnAry: ArrayList<TransactionModel> = ArrayList()
) : Serializable

data class TransactionModel(
        @SerializedName("id") val trxnId: Int = 0,
        @SerializedName("entry_type") val entryType: String = "",
        @SerializedName("amount") val amount: String = "",
        @SerializedName("status") val status: String = "",
        @SerializedName("payment_status") val paymentStatus: String = "",
        @SerializedName("payment_mode") val paymentMode: String = "",
        @SerializedName("entry_date") val entryDate: String = "",
        @SerializedName("transaction_id") val transactionId: String = "",
) : Serializable