package com.my.raido.repository

import com.my.raido.models.Database.Dao.UserDao
import com.my.raido.models.Database.DataModel.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRoomDataRepository @Inject constructor(
    private val userDao: UserDao
) {
    suspend fun insertUser(userData: User) : Unit = userDao.insertUsers(users = userData)

    suspend fun updateUser(userData: User): Unit = userDao.updateUsers(users = userData)

    suspend fun deleteUser(userData: User): Unit = userDao.deleteUsers(users = userData)

    suspend fun getUserById(id: Int): User = userDao.loadUserById(id = id)

    suspend fun nukeTable(): Unit = userDao.nukeTable()

    fun getAllUser(): Flow<List<User>> = userDao.loadUser()
}