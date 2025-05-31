package com.my.raido.models.cab

import com.google.gson.annotations.SerializedName

data class NearByRidersModelResponse(
    @SerializedName("message") val message: String = "",
    @SerializedName("status") val status: Boolean,
    @SerializedName("serviceCategories") val serviceCategories: ArrayList<ServiceCategoriesModel>,
    @SerializedName("AboutUs") val aboutUsModel: AboutUsModel,
    @SerializedName("wallet_balance") val walletBalance: String
)

data class NearByRiderModel(
    @SerializedName("rider_id") val riderId: Int = 0,
    @SerializedName("distance") val distance: String = "",
    @SerializedName("duration") val duration: String = "",
    @SerializedName("latitude") val latitude: String = "",
    @SerializedName("longitude") val longitude: String = ""
)

data class ServiceCategoriesModel(
    @SerializedName("id") val vehicleId: Int = 0,
    @SerializedName("name") val vehicleType: String = ""
)

data class AboutUsModel(
    @SerializedName("privacy_policy") val privacyPolicy: String = "",
    @SerializedName("terms_condition") val termsCondition: String = "",
    @SerializedName("join_the_team") val joinTheTeam: String = "",
    @SerializedName("blog") val blog: String = ""
)
