package com.my.raido.api

import com.my.raido.models.response.AccessTokenDto
import com.ola.maps.navigation.v5.model.route.RouteInfoData
import com.ola.maps.sdk.model.reversegeocode.response.ReverseGeocodingResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface NavigationApi {

    @FormUrlEncoded
    @POST("token")
    suspend fun getAccessToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String = "client_credentials",
        @Field("scope") scope: String = "openid",
    ): Response<AccessTokenDto>

    @POST("routing/v1/directions")
    suspend fun fetchRoute(@Query("origin") origin: String, @Query("destination") destination: String, @Query("api_key") apiKey: String, ) : Response<RouteInfoData>

    @GET("places/v1/reverse-geocode")
    suspend fun fetchDistrict(@Query("latlng") latLng: String, @Query("api_key") apiKey: String, ) : Response<ReverseGeocodingResponse>

}