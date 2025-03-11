package com.my.raido.models.response.help

import com.google.gson.annotations.SerializedName

class HelpModelResponse(@SerializedName("status") val status: Boolean = false, @SerializedName("message") val message: String = "", @SerializedName("topics") val topics: ArrayList<HelpTopics> )

class HelpTopics(@SerializedName("id") val id: Int = 0, @SerializedName("topic_name") val topicName: String = "")