package com.my.raido.ui.viewmodels.userViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.my.raido.Utils.NetworkResult
import com.my.raido.models.CreateOrderResponse
import com.my.raido.models.Database.DataModel.User
import com.my.raido.models.NotificationModel
import com.my.raido.models.profile.ProfileDetailRequest
import com.my.raido.models.profile.ProfileDetailResponse
import com.my.raido.models.response.AddBalanceResModel
import com.my.raido.models.response.DashboardDataModel
import com.my.raido.models.response.PassbookResModel
import com.my.raido.repository.UserRepository
import com.my.raido.repository.UserRoomDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class UserDataViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userRoomDataRepository: UserRoomDataRepository
): ViewModel(){



//    val getAllUser = userRoomDataRepository.getAllUser().asLiveData()

    val allUsers: LiveData<List<User>> = userRoomDataRepository.getAllUser()
        .catch { exception ->
            // Handle any exceptions here
            Log.e("UserViewModel", "Error loading users: ${exception.message}")
        }
        .asLiveData()

    fun insertUser(userData: User){
        viewModelScope.launch (Dispatchers.IO){
            userRoomDataRepository.insertUser(userData = userData)
        }
    }

    fun updateUser(userData: User){
        viewModelScope.launch (Dispatchers.IO){
            userRoomDataRepository.updateUser(userData = userData)
        }
    }

    fun logoutUser(){
        viewModelScope.launch (Dispatchers.IO){
            userRoomDataRepository.nukeTable()
        }
    }

    fun deleteUser(userData: User){
        viewModelScope.launch (Dispatchers.IO){
            userRoomDataRepository.deleteUser(userData = userData)
        }
    }

//********************** Dashboard Data ******************************************
    val dashboardDataResponseLiveData: LiveData<NetworkResult<DashboardDataModel>>
        get() = userRepository.dashboardApiResponseLiveData

    fun dashboardApi(){
        viewModelScope.launch {
            userRepository.dashboardApi()
        }
    }

    fun clearDashboardRes(){
        userRepository._dashboardApiResponseLiveData.postValue(NetworkResult.Empty())
    }

//********************** Dashboard Data ******************************************

//********************** Create Order Data ******************************************
    val createOrderResponseLiveData: LiveData<NetworkResult<CreateOrderResponse>>
        get() = userRepository.createPaymentResponseLiveData

    fun createOrder(amount: String){
        viewModelScope.launch {
            userRepository.createOrder(amount = amount)
        }
    }

    fun clearOrderRes(){
        userRepository._createPaymentResponseLiveData.postValue(NetworkResult.Empty())
    }

//********************** Create Order Data ******************************************

//********************** Update Order Data ******************************************
    val updateOrderResponseLiveData: LiveData<NetworkResult<AddBalanceResModel>>
        get() = userRepository.updatePaymentStatusResponseLiveData

    fun updateOrderStatus(orderId: String){
        viewModelScope.launch {
            userRepository.updateOrderStatus(orderId = orderId)
        }
    }

    fun clearOrderStatusRes(){
        userRepository._updatePaymentStatusResponseLiveData.postValue(NetworkResult.Empty())
    }

//********************** Update Order Data ******************************************

//********************** Fetch Passbook ******************************************
    val fetchPassbookResponseLiveData: LiveData<NetworkResult<PassbookResModel>>
        get() = userRepository.passbookHistoryResponseLiveData

    fun fetchPassbook(){
        viewModelScope.launch {
            userRepository.fetchPassbook()
        }
    }

    fun clearPassbookRes(){
        userRepository._passbookHistoryResponseLiveData.postValue(NetworkResult.Empty())
    }

//********************** Fetch Passbook ******************************************

//********************** Profile Detail Update ******************************************
    val profileResponseLiveData: LiveData<NetworkResult<ProfileDetailResponse>>
        get() = userRepository.profileDetailResponseLiveData

    fun updateProfile(profileRequest: ProfileDetailRequest){
        viewModelScope.launch {
            userRepository.submitProfileDetail(profileRequest)
        }
    }

    fun clearProfileRes(){
        userRepository._profileDetailResponseLiveData.postValue(NetworkResult.Empty())
    }

//********************** Profile Detail Update ******************************************

//********************** Profile Photo Update ******************************************

    fun updateProfilePhoto(profileImg: MultipartBody.Part) {
        viewModelScope.launch {
            userRepository.submitProfilePhoto(profileImg)
        }
    }

//********************** Profile Photo Update ******************************************

//********************** Notification Data ******************************************
    val notificationResponseLiveData: LiveData<NetworkResult<NotificationModel>>
        get() = userRepository.notificationResponseLiveData

    fun fetchNotification(){
        viewModelScope.launch {
            userRepository.fetchNotification()
        }
    }

    fun clearNotificationRes(){
        userRepository._notificationResponseLiveData.postValue(NetworkResult.Empty())
    }

//********************** Notification Data ******************************************





}