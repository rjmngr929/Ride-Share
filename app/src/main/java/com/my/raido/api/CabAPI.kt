package com.my.raido.api

import com.my.raido.models.RecentRideDetailResponse
import com.my.raido.models.RecentRidesModel
import com.my.raido.models.cab.NearByRidersModelResponse
import com.my.raido.models.cab.RideBookRequest
import com.my.raido.models.cab.RideBookResponse
import com.my.raido.models.response.DriverDetailModel
import com.my.raido.models.response.ResponseModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CabAPI {

    @GET("auth/user/riders/nearby")
    suspend fun fetchDashboardDetail() : Response<NearByRidersModelResponse>

    @GET("auth/ride-history")
    suspend fun getRecentRides() : Response<RecentRidesModel>

    @GET("auth/ride-history/{rideId}")
    suspend fun getRecentRideDetail(@Path("rideId") rideId: String) : Response<RecentRideDetailResponse>

    @POST("auth/user/rides/book")
    suspend fun getAvailableRiders(@Body rideBookRequest: RideBookRequest) : Response<RideBookResponse>

    @POST("auth/user/create-ride")
    suspend fun bookRide(@Body rideBookRequest: RideBookRequest) : Response<RideBookResponse>

    @FormUrlEncoded
    @POST("auth/user/district-services")
    suspend fun updateNonServiceDistrict(@Field("district") district: String) : Response<ResponseModel>

    @FormUrlEncoded
    @POST("auth/user/rider-details")
    suspend fun getDriverDetail(@Field("booking_id") riderId: String) : Response<DriverDetailModel>

    @FormUrlEncoded
    @POST("auth/user/update-ride-rating")
    suspend fun rateDriver(@Field("ride_id") riderId: String, @Field("rating") rating: String) : Response<ResponseModel>

}