package com.my.raido.di

import com.my.raido.api.NavigationApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NavigationModule {

//    @Provides
//    @Singleton
//    fun provideFusedLocationProviderClient(
//        @ApplicationContext context: Context
//    ): FusedLocationProviderClient {
//        return LocationServices.getFusedLocationProviderClient(context)
//    }

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.olamaps.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideDirectionsAPI(retrofit: Retrofit): NavigationApi {
        return retrofit.create(NavigationApi::class.java)
    }
}
