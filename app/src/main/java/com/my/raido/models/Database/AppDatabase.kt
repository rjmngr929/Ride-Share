package com.my.raido.models.Database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.my.raido.models.Database.Dao.UserDao
import com.my.raido.models.Database.DataModel.User

@Database(entities = [User::class],  version = 1, exportSchema = false)
abstract class  AppDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
}