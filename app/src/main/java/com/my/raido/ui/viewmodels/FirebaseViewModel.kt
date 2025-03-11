package com.my.raido.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.my.raido.repository.FirebaseDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class FirebaseViewModel @Inject constructor(
    private val repository: FirebaseDataRepository
) : ViewModel() {

    fun writeToFirebase() {
        repository.writeData("users/user1", mapOf("name" to "John Doe", "age" to 25))
    }

    fun readFromFirebase(onResult: (DataSnapshot) -> Unit) {
        repository.readData("users/user1", onResult, { error ->
            Log.e("FirebaseError", error.message)
        })
    }
}