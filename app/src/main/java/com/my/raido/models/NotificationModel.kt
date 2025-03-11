package com.my.raido.models

import com.google.gson.annotations.SerializedName

//class NotificationModel( val icon: String = "", val title: String, val description: String): Serializable {
//}
data class NotificationModel(@SerializedName("message") val message: String, @SerializedName("data") val NotificationList: ArrayList<NotificationList>, @SerializedName("status") val status: String)

data class NotificationList(
        @SerializedName("type") val type: String,
        @SerializedName("message") val notificationMessage: String,
        @SerializedName("id") val notificationId: String,
        @SerializedName("is_read") val messageRead: Boolean,
    )

