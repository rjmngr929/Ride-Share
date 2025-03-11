package com.my.raido.api

import com.my.raido.models.login.OTPVerifyResponse
import com.my.raido.models.response.ResponseModel
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthAPI {

    @FormUrlEncoded
    @POST("auth/sendOtp")
    suspend fun sendOtp(@Field("mobile_number") mobileNumber: String) : Response<ResponseModel>

    @FormUrlEncoded
    @POST("auth/verifyOtp")
    suspend fun verifyOtp(@FieldMap fields: Map<String, String>) : Response<OTPVerifyResponse>


}