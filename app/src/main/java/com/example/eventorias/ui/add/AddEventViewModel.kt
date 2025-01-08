package com.example.eventorias.ui.add

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventorias.repository.EventRepository
import kotlinx.coroutines.launch

class AddEventViewModel : ViewModel() {

    private val repository = EventRepository()

    // Saves the event data after uploading the image
    fun saveEvent(
        title: String,
        date: String,
        time: String,
        address: String,
        description: String,
        latitude: Double,
        longitude: Double,
        imageUri: Uri?,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (imageUri == null) {
            onFailure("Please select an image")
            return
        }

        viewModelScope.launch {
            try {
                // Upload the image and get the download URL
                val imageUrl = repository.uploadImageToFirebase(imageUri)
                // Save the event data to Firestore
                repository.saveEvent(title, date, time, address, description, latitude, longitude, imageUrl)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Failed to save event")
            }
        }
    }
}