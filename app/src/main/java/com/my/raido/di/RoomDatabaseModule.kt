package com.my.raido.di

import android.content.Context
import androidx.room.Room
import com.my.raido.R
import com.my.raido.models.Database.AppDatabase
import com.my.raido.models.Database.Dao.UserDao
import com.my.raido.repository.UserRoomDataRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class RoomDatabaseModule {

    @Singleton
    @Provides
    fun provideLocalDataBase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            context.getString(R.string.database_name)
            ).build()
    }

    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    @Singleton
    fun provideUserRepository(user_dao: UserDao): UserRoomDataRepository = UserRoomDataRepository(userDao = user_dao)

}