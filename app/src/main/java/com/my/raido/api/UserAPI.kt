package com.my.raido.api

import com.my.raido.models.CreateOrderResponse
import com.my.raido.models.NotificationModel
import com.my.raido.models.contacts.ContactRequestData
import com.my.raido.models.contacts.ContactResponse
import com.my.raido.models.profile.ProfileDetailResponse
import com.my.raido.models.response.AddBalanceResModel
import com.my.raido.models.response.DashboardDataModel
import com.my.raido.models.response.PassbookResModel
import com.my.raido.models.response.ResponseModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface UserAPI {

//    @FormUrlEncoded
//    @POST("secure/kyc-verification/basic-details")
//    suspend fun submitProfileData(@FieldMap fields: Map<String, String>) : Response<ProfileDetailResponse>
//
//
//    @Multipart
//    @POST("secure/kyc-verification/pan-card-details")
//    suspend fun submitPanData(@Part("pan_card") panNumber: RequestBody, @Part panImg: MultipartBody.Part) : Response<ProfileDetailResponse>

    @Multipart
    @POST("auth/user/profile")
    suspend fun updateProfile(
        @Part("name") userName: RequestBody?,
        @Part("email") userEmail: RequestBody?,
        @Part("gender") userGender: RequestBody?,
        @Part("date_of_birth") userDob: RequestBody?
    ): Response<ProfileDetailResponse>

    @Multipart
    @POST("auth/user/profile")
    suspend fun updateProfile(
        @Part profileImg: MultipartBody.Part?,
    ): Response<ProfileDetailResponse>

    @GET("auth/user/notifications")
    suspend fun getNotifications() : Response<NotificationModel>

    @GET("auth/user/safety-toolkits")
    suspend fun getContactList() : Response<ContactResponse>

    @POST("auth/user/safety-toolkits-store")
    suspend fun submitContactList(@Body contactlist: ContactRequestData) : Response<ResponseModel>

    @FormUrlEncoded
    @POST("auth/user/wallet/create-payment")
    suspend fun createPaymentOrder(@Field("amount") amount: String) : Response<CreateOrderResponse>

    @GET("auth/user/wallet/get-payment-detailsby/{orderId}")
    suspend fun checkOrderStatus( @Path("orderId") orderId: String) : Response<AddBalanceResModel>

    @GET("auth/user/wallet/passbook")
    suspend fun fetchPassbook() : Response<PassbookResModel>

    @GET("auth/user/dashboard")
    suspend fun dashboardApi() : Response<DashboardDataModel>

}