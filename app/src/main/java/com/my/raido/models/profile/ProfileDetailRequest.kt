package com.my.raido.models.profile

import okhttp3.RequestBody
import retrofit2.http.Part

data class ProfileDetailRequest(
    @Part("name") val userName: RequestBody,
    @Part("email") val userEmail: RequestBody,
    @Part("gender") val userGender: RequestBody,
    @Part("date_of_birth") val userDob: RequestBody
)
