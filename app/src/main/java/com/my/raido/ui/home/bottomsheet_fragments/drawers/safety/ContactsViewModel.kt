package com.my.raido.ui.home.bottomsheet_fragments.drawers.safety

import android.app.Application
import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.my.raido.Utils.NetworkResult
import com.my.raido.models.contacts.ContactList
import com.my.raido.models.contacts.ContactRequestData
import com.my.raido.models.contacts.ContactResponse
import com.my.raido.models.response.ResponseModel
import com.my.raido.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

// Data class to hold contact information
//data class Contact(val name: String, val phoneNumber: String?, var isSelected: Boolean = false )

@HiltViewModel
class ContactsViewModel @Inject constructor(
    application: Application,
    private val userRepository: UserRepository
) : AndroidViewModel(application) {

    private val _contacts = MutableLiveData<List<ContactList>>()
    val contacts: LiveData<List<ContactList>> = _contacts

    fun fetchContacts() {
        viewModelScope.launch(Dispatchers.IO) { // Run in background thread
            val contactList = getContactsFromPhone()
            _contacts.postValue(contactList) // Post result to LiveData
        }
    }

    private fun getContactsFromPhone(): List<ContactList> {
        val contentResolver: ContentResolver = getApplication<Application>().contentResolver
        val contactList = mutableListOf<ContactList>()
        val phoneNumbersSet = mutableSetOf<String>() // To track unique phone numbers

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                val phoneNumber = it.getString(phoneIndex).toString().replace("[ -]".toRegex(), "")

                // Check if the phone number is already added
                if (phoneNumber !in phoneNumbersSet) {
                    contactList.add(ContactList(name, phoneNumber))
                    phoneNumbersSet.add(phoneNumber) // Add to the set to track duplicates
                }
            }
        }
        return contactList
    }


//********************** Fetch Contact List ******************************************
    val contactListResponseLiveData: LiveData<NetworkResult<ContactResponse>>
        get() = userRepository.contactListResponseLiveData

    fun fetchContactList(){
        viewModelScope.launch {
            userRepository.fetchContactList()
        }
    }

    fun clearContactListRes(){
        userRepository._contactListResponseLiveData.postValue(NetworkResult.Empty())
    }

//********************** Fetch Contact List ******************************************

//********************** Update Contact List ******************************************
    val updateContactListResponseLiveData: LiveData<NetworkResult<ResponseModel>>
        get() = userRepository.updateContactListResponseLiveData

    fun updateContactList(contactList: ContactRequestData){
        viewModelScope.launch {
            userRepository.updateContactList(contactList = contactList)
        }
    }

    fun clearUpdateContactRes(){
        userRepository._updateContactListResponseLiveData.postValue(NetworkResult.Empty())
    }

//********************** Update Contact List ******************************************

}