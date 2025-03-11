package com.my.raido.models.Database.DataModel

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey val uid: String = "",
    var userId: Int?,
    var userName: String?,
    var userEmail: String?,
    var userMobile: String?,
    var userDob: String? = "select DOB",
    var gender: String? = "select gender",
    var userProfileImg: String?,
    var walletBalance: String?,
    var currency: String?,
    var memberSince: String?,
    var walletStatus: Boolean?,
    var totalCompleteRides: Int?,
    var totalRatingSum: Int?,
    var totalRatings: Int?


){
    fun copyWith(
        userId: Int? = null,
        userName: String? = null,
        userEmail: String? = null,
        userMobile: String? = null,
        userProfileImg: String? = null,
        walletBalance: String? = null,
        dateofBirth: String? = null,
        gender: String? = null,
        currency: String? =null,
        totalRatings: Int? = 0,
        walletStatus: Boolean? = false,
        memberSince: String? = null,
        totalCompleteRides: Int? = 0,
        totalRatingSum: Int? = 0,
    ) = User(
        userId = userId ?: this.userId,
        userName = userName ?: this.userName,
        userEmail = userEmail ?: this.userEmail,
        userMobile = userMobile ?: this.userMobile,
        userProfileImg = userProfileImg ?: this.userProfileImg,
        walletBalance = walletBalance ?: this.walletBalance,
        userDob = dateofBirth ?: this.userDob,
        gender = gender ?: this.gender,
        currency = currency ?: this.currency,
        totalRatings = totalRatings ?: this.totalRatings,
        walletStatus = walletStatus ?: this.walletStatus,
        memberSince = memberSince ?: this.memberSince,
        totalCompleteRides = totalCompleteRides ?: this.totalCompleteRides,
        totalRatingSum = totalRatingSum ?: this.totalRatingSum

    )
}
