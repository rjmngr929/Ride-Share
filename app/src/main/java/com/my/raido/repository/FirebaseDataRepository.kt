package com.my.raido.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FirebaseDataRepository @Inject constructor(
    private val databaseReference: DatabaseReference
) {

    fun writeData(path: String, data: Any) {
        databaseReference.child(path).setValue(data)
    }

    fun readData(path: String, onDataChange: (DataSnapshot) -> Unit, onError: (DatabaseError) -> Unit) {
        databaseReference.child(path).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onDataChange(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error)
            }
        })
    }
}