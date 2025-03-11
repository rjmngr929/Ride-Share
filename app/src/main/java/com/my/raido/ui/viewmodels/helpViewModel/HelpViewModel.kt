package com.my.raido.ui.viewmodels.helpViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.raido.Utils.NetworkResult
import com.my.raido.models.response.help.HelpDetailModelResponse
import com.my.raido.models.response.help.HelpModelResponse
import com.my.raido.repository.HelpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HelpViewModel @Inject constructor(
    private val helpRepository: HelpRepository
) : ViewModel(){

//********************** Help Section ******************************************
    val helpQuotesResponseLiveData: LiveData<NetworkResult<HelpModelResponse>>
        get() = helpRepository.helpQueryResponseLiveData

    fun fetchHelpQuotes(){
        viewModelScope.launch {
            helpRepository.helpQueryApi()
        }
    }

    fun clearHelpQuotesRes(){
        helpRepository._helpQueryResponseLiveData.postValue(NetworkResult.Empty())
    }

//********************** Help Section ******************************************

//********************** Help Detail ******************************************
    val helpQuotesDetailResponseLiveData: LiveData<NetworkResult<HelpDetailModelResponse>>
        get() = helpRepository.helpQueryDetailResponseLiveData

    fun fetchHelpQuotesDetails(queryId: String){
        viewModelScope.launch {
            helpRepository.helpQueryDetailApi(queryId)
        }
    }

    fun clearHelpQuotesDetailRes(){
        helpRepository._helpQueryDetailResponseLiveData.postValue(NetworkResult.Empty())
    }

//********************** Help Detail ******************************************

}