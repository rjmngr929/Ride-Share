package com.my.raido.ui.home

import androidx.lifecycle.ViewModel
import com.my.raido.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: UserRepository
): ViewModel() {

//    val recentRidesList : MutableLiveData<Response<List<RecentRidesModel>>> = MutableLiveData()
//
//    fun getProductViewModel(){
//        viewModelScope.launch {
//            recentRidesList.value = ArrayList<RecentRidesModel>()
//        }
//    }


}