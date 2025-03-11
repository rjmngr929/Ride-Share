package com.my.raido.models.response.help

import com.google.gson.annotations.SerializedName


class HelpDetailModelResponse(@SerializedName("status") val status: Boolean = false, @SerializedName("message") val message: String = "", @SerializedName("faqs") val helpData: ArrayList<HelpDetail> )

class HelpDetail(@SerializedName("question") val question: String = "", @SerializedName("answer") val answer: String = "")