package com.my.raido.Modules

import android.app.Application
import com.my.raido.ui.viewmodels.MasterViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedViewModel(application: Application): MasterViewModel {
        return MasterViewModel(application)
    }
}