package com.my.raido.models.contacts

import com.google.gson.annotations.SerializedName


data class ContactResponse(@SerializedName("message") val message: String, @SerializedName("data") val ContactList: ArrayList<ContactList>, @SerializedName("status") val status: String)

data class ContactList(
    @SerializedName("name") val name: String,
    @SerializedName("number") val number: String,
    var isSelected: Boolean = false
    )
