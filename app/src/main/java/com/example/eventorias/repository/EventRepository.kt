package com.example.eventorias.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class EventRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // Uploads an image to Firebase Storage and returns the download URL
    suspend fun uploadImageToFirebase(uri: Uri): String {
        val storageRef = storage.reference
        val imageRef = storageRef.child("event_images/${UUID.randomUUID()}.jpg")

        val uploadTask = imageRef.putFile(uri).await()
        return imageRef.downloadUrl.await().toString()
    }

    // Saves the event data to Firestore
    suspend fun saveEvent(
        title: String,
        date: String,
        time: String,
        address: String,
        description: String,
        latitude: Double,
        longitude: Double,
        imageUrl: String
    ) {
        val event = hashMapOf(
            "title" to title,
            "date" to date,
            "time" to time,
            "address" to address,
            "description" to description,
            "imageUrl" to imageUrl,
            "latitude" to latitude,
            "longitude" to longitude
        )

        firestore.collection("events").add(event).await()
    }
}