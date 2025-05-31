package com.my.raido.models.profile

import com.google.gson.annotations.SerializedName

data class ProfileDetailResponse(
    @SerializedName("message") val message: String = "",
    @SerializedName("user") val userData: UserDataModel?
)

data class UserDataModel(
    @SerializedName("id") val userId: Int = 0,
    @SerializedName("name") val userName: String = "",
    @SerializedName("email") val userEmail: String = "",
    @SerializedName("created_at") val memberSince: String = "",
    @SerializedName("mobile_number") val mobileNumber: String = "",
    @SerializedName("profile_picture") val profilePic: String = "",
    @SerializedName("gender") val gender: String = "",
    @SerializedName("date_of_birth") val dob: String = "",
    @SerializedName("wallet_balance") val walletBalance: String = "",
    @SerializedName("currency") val currency: String = "",
    @SerializedName("is_wallet_active") val isWalletActive: String = "false",
    @SerializedName("total_completed_rides") val totalCompleteRides: Int = 0,
    @SerializedName("total_rating_sum") val totalRatingSum: Int = 0,
    @SerializedName("total_ratings_count") val totalRatingsCount: Int = 0
)




