package com.my.raido.api

import com.my.raido.models.response.help.HelpDetailModelResponse
import com.my.raido.models.response.help.HelpModelResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface HelpAPI {

//************************************* Help Section ********************************************
    @GET("auth/user/help-topic")
    suspend fun helpQueryAPI() : Response<HelpModelResponse>

    @GET("auth/user/help-faqs/{queryId}")
    suspend fun helpQueryDetailAPI(@Path("queryId") queryId: String) : Response<HelpDetailModelResponse>
//************************************* Help Section ********************************************

}