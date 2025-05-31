package com.my.raido.repository

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.my.raido.Helper.NetworkHelper
import com.my.raido.Utils.NetworkResult
import com.my.raido.api.UserAPI
import com.my.raido.di.exception.NetworkExceptionHandler
import com.my.raido.models.CreateOrderResponse
import com.my.raido.models.NotificationModel
import com.my.raido.models.contacts.ContactRequestData
import com.my.raido.models.contacts.ContactResponse
import com.my.raido.models.profile.ProfileDetailRequest
import com.my.raido.models.profile.ProfileDetailResponse
import com.my.raido.models.response.AddBalanceResModel
import com.my.raido.models.response.DashboardDataModel
import com.my.raido.models.response.PassbookResModel
import com.my.raido.models.response.ResponseModel
import okhttp3.MultipartBody
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject


class UserRepository @Inject constructor(private val userApi: UserAPI, private val networkHelper: NetworkHelper, private val exceptionHandler: NetworkExceptionHandler) {

//********************** Dashboard API ***********************************
    val _dashboardApiResponseLiveData = MutableLiveData<NetworkResult<DashboardDataModel>>()
    val dashboardApiResponseLiveData: LiveData<NetworkResult<DashboardDataModel>>
        get() = _dashboardApiResponseLiveData

    @SuppressLint("SuspiciousIndentation")
    suspend fun dashboardApi() {
        _dashboardApiResponseLiveData.postValue(NetworkResult.Loading())
        if(networkHelper.isNetworkConnected()) {
            try {
                val response =userApi.dashboardApi()
                Log.d("TAG", "dashboardApi: Dashboard data => ${response}")
                if (response.isSuccessful) {
                    Log.d("TAG", "dashboardApi: Dashboard data => ${response}")
                    handleDashboardResponse(response)
                } else {
                    try {
                        val message = response.errorBody()?.string()?.let { JSONObject(it) }?.getString("message")
    //                    Log.d("TAG", "dashboardApi: error create by ${message}")
                        _dashboardApiResponseLiveData.postValue(
                            NetworkResult.Error( message.toString() ))
                    }catch (e: Exception){
                        Log.d("TAG", "dashboardApi: Dashboard data => upper ${response.errorBody()}")
                        _dashboardApiResponseLiveData.postValue(
                            NetworkResult.Error(response.errorBody()?.string()
                                ?.let { JSONObject(it)?.getString("message") }.toString()))
                    }
                }
            }catch (e: Exception){
                Log.d("TAG", "dashboardApi: Dashboard data => lower ${e}")
                _dashboardApiResponseLiveData.postValue(NetworkResult.Error(exceptionHandler.handleException(e)))
            }
        }else{
            _dashboardApiResponseLiveData.postValue(
                NetworkResult.Error("No internet connection" )
            )
        }
    }

    private fun handleDashboardResponse(response: Response<DashboardDataModel>) {
        if (response.isSuccessful && response.body() != null) {
            _dashboardApiResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        }
        else if(response.errorBody()!=null){
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _dashboardApiResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("message")))
        }
        else{
            _dashboardApiResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
        }
    }

//********************** Dashboard API ***********************************

//********************** Create Payment Order ***********************************
    val _createPaymentResponseLiveData = MutableLiveData<NetworkResult<CreateOrderResponse>>()
    val createPaymentResponseLiveData: LiveData<NetworkResult<CreateOrderResponse>>
        get() = _createPaymentResponseLiveData

    @SuppressLint("SuspiciousIndentation")
    suspend fun createOrder(amount: String) {
        _createPaymentResponseLiveData.postValue(NetworkResult.Loading())
        if(networkHelper.isNetworkConnected()) {
            try {
                val response =userApi.createPaymentOrder(amount = amount)
                Log.d("TAG", "createOrder: Create Order data => ${response}")
                if (response.isSuccessful) {
                    Log.d("TAG", "createOrder: Create Order data => ${response}")
                    handleCreateOrderResponse(response)
                } else {
                    try {
                        Log.d("TAG", "createOrder: error create by ${response.errorBody()?.string()}")
                        _createPaymentResponseLiveData.postValue(
                            NetworkResult.Error(response.errorBody()?.string()
                                ?.let { JSONObject(it).getString("message") }))
                    }catch (e: Exception){
                        Log.d("TAG", "createOrder: Create Order data => upper ${e}")
                        _createPaymentResponseLiveData.postValue(
                            NetworkResult.Error(response.errorBody()?.string()
                                ?.let { JSONObject(it) }?.getString("message")))
                    }
                }
            }catch (e: Exception){
                Log.d("TAG", "createOrder: Create Order data => lower ${e}")
                _createPaymentResponseLiveData.postValue(NetworkResult.Error(exceptionHandler.handleException(e)))
            }
        }else{
            _dashboardApiResponseLiveData.postValue(
                NetworkResult.Error("No internet connection" )
            )
        }
    }

    private fun handleCreateOrderResponse(response: Response<CreateOrderResponse>) {
        if (response.isSuccessful && response.body() != null) {
            _createPaymentResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        }
        else if(response.errorBody()!=null){
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _createPaymentResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("message")))
        }
        else{
            _createPaymentResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
        }
    }

//********************** Create Payment Order ***********************************

//********************** Update Payment Order Status ***********************************
    val _updatePaymentStatusResponseLiveData = MutableLiveData<NetworkResult<AddBalanceResModel>>()
    val updatePaymentStatusResponseLiveData: LiveData<NetworkResult<AddBalanceResModel>>
        get() = _updatePaymentStatusResponseLiveData

    @SuppressLint("SuspiciousIndentation")
    suspend fun updateOrderStatus(orderId: String) {
        _updatePaymentStatusResponseLiveData.postValue(NetworkResult.Loading())
        if(networkHelper.isNetworkConnected()) {
            try {
                Log.d("TAG", "updateOrderStatus: update Status data => ${orderId}")
                val response =userApi.checkOrderStatus(orderId = orderId)
                Log.d("TAG", "updateOrderStatus: update Status data => ${response}")
                if (response.isSuccessful) {
                    Log.d("TAG", "updateOrderStatus: update Status data => ${response}")
                    handleUpdateOrderStatusResponse(response)
                } else {
                    try {
                        _updatePaymentStatusResponseLiveData.postValue(
                            NetworkResult.Error(response.errorBody()?.string()
                                ?.let { JSONObject(it).getString("message") }))
                    }catch (e: Exception){
                        Log.d("TAG", "updateOrderStatus: update Status data => upper ${e}")
                    }
                }
            }catch (e: Exception){
                Log.d("TAG", "updateOrderStatus: update Status data => lower ${e.message}")
                _updatePaymentStatusResponseLiveData.postValue(NetworkResult.Error(exceptionHandler.handleException(e)))
            }
        }else{
            _updatePaymentStatusResponseLiveData.postValue(
                NetworkResult.Error("No internet connection" )
            )
        }
    }

    private fun handleUpdateOrderStatusResponse(response: Response<AddBalanceResModel>) {
        if (response.isSuccessful && response.body() != null) {
            _updatePaymentStatusResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        }
        else if(response.errorBody()!=null){
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _updatePaymentStatusResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("message")))
        }
        else{
            _updatePaymentStatusResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
        }
    }

//********************** Update Payment Order Status ***********************************

//********************** Passbook History ***********************************
    val _passbookHistoryResponseLiveData = MutableLiveData<NetworkResult<PassbookResModel>>()
    val passbookHistoryResponseLiveData: LiveData<NetworkResult<PassbookResModel>>
        get() = _passbookHistoryResponseLiveData

    @SuppressLint("SuspiciousIndentation")
    suspend fun fetchPassbook() {
        _passbookHistoryResponseLiveData.postValue(NetworkResult.Loading())
        if(networkHelper.isNetworkConnected()) {
            try {
                val response =userApi.fetchPassbook()
                Log.d("TAG", "fetchPassbook: update Status data => ${response}")
                if (response.isSuccessful) {
                    Log.d("TAG", "fetchPassbook: update Status data => ${response}")
                    handlePassbookResponse(response)
                } else {
                    try {
                        _passbookHistoryResponseLiveData.postValue(
                            NetworkResult.Error(response.errorBody()?.string()
                                ?.let { JSONObject(it).getString("message") }))
                    }catch (e: Exception){
                        Log.d("TAG", "fetchPassbook: update Status data => upper ${e}")
                    }
                }
            }catch (e: Exception){
                Log.d("TAG", "fetchPassbook: update Status data => lower ${e.message}")
                _passbookHistoryResponseLiveData.postValue(NetworkResult.Error(exceptionHandler.handleException(e)))
            }
        }else{
            _passbookHistoryResponseLiveData.postValue(
                NetworkResult.Error("No internet connection" )
            )
        }
    }

    private fun handlePassbookResponse(response: Response<PassbookResModel>) {
        if (response.isSuccessful && response.body() != null) {
            _passbookHistoryResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        }
        else if(response.errorBody()!=null){
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _passbookHistoryResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("message")))
        }
        else{
            _passbookHistoryResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
        }
    }

//********************** Passbook History ***********************************

//********************** profile detail Opertaion ******************************************
    val _profileDetailResponseLiveData = MutableLiveData<NetworkResult<ProfileDetailResponse>>()
    val profileDetailResponseLiveData: LiveData<NetworkResult<ProfileDetailResponse>>
        get() = _profileDetailResponseLiveData

    suspend fun submitProfileDetail(profileRequest: ProfileDetailRequest) {
        _profileDetailResponseLiveData.postValue(NetworkResult.Loading())
        if(networkHelper.isNetworkConnected()) {
            try {
    //            Log.d("TAG", "submitProfileDetail: response data => ${profileDetailRequest.toMap()} ")
                val response =userApi.updateProfile(
                    userName = profileRequest.userName,
                    userEmail = profileRequest.userEmail,
                    userGender = profileRequest.userGender,
                    userDob = profileRequest.userDob
                )
                Log.d("TAG", "submitProfileDetail: response data => $response ")
                if (response.isSuccessful) {
                    handleProfileDataResponse(response)
                } else {
                    Log.d("TAG", "submitProfileDetail: error on response unsuccessfull => ${response.errorBody()?.string().let {
                        if (it != null) {
                            JSONObject(it)
                        }
                    }}")
                    try {
                        _profileDetailResponseLiveData.postValue(
                            NetworkResult.Error(response.errorBody()?.string()
                            ?.let { JSONObject(it).getString("message") }))
                    }catch (e: Exception){
                        Log.d("TAG", "submitProfileDetail: error on catch part ${e}")
                    }
                }
            }catch (e: Exception){
                Log.d("TAG", "submitProfileDetail: error on catch part network => ${e}")
                _profileDetailResponseLiveData.postValue(NetworkResult.Error(exceptionHandler.handleException(e)))
            }
        }else{
            _profileDetailResponseLiveData.postValue(
                NetworkResult.Error("No internet connection" )
            )
        }
    }

    private fun handleProfileDataResponse(response: Response<ProfileDetailResponse>) {
        if (response.isSuccessful && response.body() != null) {
            _profileDetailResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        }
        else if(response.errorBody()!=null){
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _profileDetailResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("message")))
        }
        else{
            _profileDetailResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
        }
    }
//********************** profile detail Opertaion ******************************************

//********************** profile Photo Opertaion ******************************************
    suspend fun submitProfilePhoto(profileImg: MultipartBody.Part) {
    _profileDetailResponseLiveData.postValue(NetworkResult.Loading())
        if(networkHelper.isNetworkConnected()) {
            try {
    //            Log.d("TAG", "submitProfileDetail: response data => ${profileDetailRequest.toMap()} ")
                val response =userApi.updateProfile(
                   profileImg = profileImg
                )
                Log.d("TAG", "submitProfilePhoto: response data => $response ")
                if (response.isSuccessful) {
                    handleProfileDataResponse(response)
                } else {
                    Log.d("TAG", "submitProfilePhoto: error on response unsuccessfull => ${response.errorBody()?.string().let {
                        if (it != null) {
                            JSONObject(it)
                        }
                    }}")
                    try {
                        _profileDetailResponseLiveData.postValue(
                            NetworkResult.Error(response.errorBody()?.string()
                                ?.let { JSONObject(it).getString("message") }))
                    }catch (e: Exception){
                        Log.d("TAG", "submitProfilePhoto: error on catch part ${e}")
                    }
                }
            }catch (e: Exception){
                Log.d("TAG", "submitProfilePhoto: error on catch part network => $e")
                _profileDetailResponseLiveData.postValue(NetworkResult.Error(exceptionHandler.handleException(e)))
            }
        }else{
            _profileDetailResponseLiveData.postValue(
                NetworkResult.Error("No internet connection" )
            )
        }
    }
//********************** profile Photo Opertaion ******************************************

//********************** Notification Data ******************************************
    val _notificationResponseLiveData = MutableLiveData<NetworkResult<NotificationModel>>()
    val notificationResponseLiveData: LiveData<NetworkResult<NotificationModel>>
        get() = _notificationResponseLiveData

    suspend fun fetchNotification() {
        _notificationResponseLiveData.postValue(NetworkResult.Loading())
        if(networkHelper.isNetworkConnected()) {
            try {
                val response =userApi.getNotifications()
                Log.d("TAG", "fetchNotification: notification data => ${response}")
                if (response.isSuccessful) {
                    Log.d("TAG", "fetchNotification: notification data => ${response}")
                    handleNotificationResponse(response)
                } else {
                    try {
                        _notificationResponseLiveData.postValue(
                            NetworkResult.Error(response.errorBody()?.string()
                                ?.let { JSONObject(it).getString("message") }))
                    }catch (e: Exception){
                        Log.d("TAG", "fetchNotification: recent Rides data => upper ${e}")
                    }
                }
            }catch (e: Exception){
                Log.d("TAG", "fetchNotification: recent Rides data => lower ${e}")
                _notificationResponseLiveData.postValue(NetworkResult.Error(exceptionHandler.handleException(e)))
            }
        }else{
            _notificationResponseLiveData.postValue(
                NetworkResult.Error("No internet connection" )
            )
        }
    }

    private fun handleNotificationResponse(response: Response<NotificationModel>) {
        if (response.isSuccessful && response.body() != null) {
            _notificationResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        }
        else if(response.errorBody()!=null){
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _notificationResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("message")))
        }
        else{
            _notificationResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
        }
    }
//********************** Notification Data ***********************************


//********************** Fetch Contact Data ***********************************
    val _contactListResponseLiveData = MutableLiveData<NetworkResult<ContactResponse>>()
    val contactListResponseLiveData: LiveData<NetworkResult<ContactResponse>>
        get() = _contactListResponseLiveData

    suspend fun fetchContactList() {
        _contactListResponseLiveData.postValue(NetworkResult.Loading())
        if(networkHelper.isNetworkConnected()) {
            try {
                val response =userApi.getContactList()
                Log.d("TAG", "fetchContactList: Contact list data => ${response}")
                if (response.isSuccessful) {
                    Log.d("TAG", "fetchContactList: Contact list data => ${response}")
                    handleContactListResponse(response)
                } else {
                    try {
                        _contactListResponseLiveData.postValue(
                            NetworkResult.Error(response.errorBody()?.string()
                                ?.let { JSONObject(it).getString("message") }))
                    }catch (e: Exception){
                        Log.d("TAG", "fetchContactList: Contact list data => upper ${e}")
                    }
                }
            }catch (e: Exception){
                Log.d("TAG", "fetchContactList: Contact list data => lower ${e}")
                _contactListResponseLiveData.postValue(NetworkResult.Error(exceptionHandler.handleException(e)))
            }
        }else{
            _contactListResponseLiveData.postValue(
                NetworkResult.Error("No internet connection" )
            )
        }
    }

    private fun handleContactListResponse(response: Response<ContactResponse>) {
        if (response.isSuccessful && response.body() != null) {
            _contactListResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        }
        else if(response.errorBody()!=null){
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _contactListResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("message")))
        }
        else{
            _contactListResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
        }
    }

//********************** Fetch Contact Data ***********************************

//********************** Update Contact Data ***********************************
    val _updateContactListResponseLiveData = MutableLiveData<NetworkResult<ResponseModel>>()
    val updateContactListResponseLiveData: LiveData<NetworkResult<ResponseModel>>
        get() = _updateContactListResponseLiveData

    @SuppressLint("SuspiciousIndentation")
    suspend fun updateContactList(contactList: ContactRequestData) {
        _updateContactListResponseLiveData.postValue(NetworkResult.Loading())
        if(networkHelper.isNetworkConnected()) {
            try {
                    Log.d("TAG", "updateContactList: update contact list data => ${contactList}")
                val response =userApi.submitContactList(contactlist = contactList)
                    Log.d("TAG", "updateContactList: Contact list data => ${response}")
                if (response.isSuccessful) {
                    Log.d("TAG", "updateContactList: Contact list data => ${response}")
                    handleUpdateContactListResponse(response)
                } else {
                    try {
                        _updateContactListResponseLiveData.postValue(
                            NetworkResult.Error(response.errorBody()?.string()
                                ?.let { JSONObject(it).getString("message") }))
                    }catch (e: Exception){
                        Log.d("TAG", "updateContactList: Contact list data => upper ${e}")
                    }
                }
            }catch (e: Exception){
                Log.d("TAG", "updateContactList: Contact list data => lower ${e}")
                _updateContactListResponseLiveData.postValue(NetworkResult.Error(exceptionHandler.handleException(e)))
            }
        }else{
            _updateContactListResponseLiveData.postValue(
                NetworkResult.Error("No internet connection" )
            )
        }
    }

    private fun handleUpdateContactListResponse(response: Response<ResponseModel>) {
        if (response.isSuccessful && response.body() != null) {
            _updateContactListResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        }
        else if(response.errorBody()!=null){
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _updateContactListResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("message")))
        }
        else{
            _updateContactListResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
        }
    }

//********************** Update Contact Data ***********************************





}