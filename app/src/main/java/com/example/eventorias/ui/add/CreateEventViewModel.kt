package com.example.eventorias.ui.add

import android.content.Context
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.*
import kotlin.coroutines.cancellation.CancellationException

class CreateEventViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CreateEventState())
    val uiState: StateFlow<CreateEventState> = _uiState

    private val _eventSaved = MutableStateFlow(false)
    val eventSaved: StateFlow<Boolean> get() = _eventSaved

    private val _imageUri = MutableLiveData<Uri?>()
    val imageUri: LiveData<Uri?> get() = _imageUri

    fun handleImageUri(uri: Uri) {
        _imageUri.value = uri
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateDate(date: String) {
        _uiState.value = _uiState.value.copy(date = date)
    }

    fun updateTime(time: String) {
        _uiState.value = _uiState.value.copy(time = time)
    }

    fun updateAddress(address: String) {
        _uiState.value = _uiState.value.copy(address = address)
    }

    fun onSaveEvent(context: Context) {
        viewModelScope.launch {
            Log.d("CreateEventViewModel", "Coroutine started")
            try {
                val coordinates = getCoordinatesFromAddress(context, _uiState.value.address)
                if (coordinates != null) {
                    val (latitude, longitude) = coordinates
                    val imageUrl = _imageUri.value?.let { uri ->
                        uploadImageToFirebase(uri).await()
                    } ?: run {
                        Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    saveEvent(
                        _uiState.value.title, _uiState.value.date, _uiState.value.time,
                        _uiState.value.address, _uiState.value.description,
                        latitude, longitude, imageUrl.toString()
                    )
                    _eventSaved.value = true // Notify UI of success

                    sendMessageToTopic(_uiState.value.title, "all")
                    Toast.makeText(context, "Event created successfully", Toast.LENGTH_SHORT).show()

                    Log.d("CreateEventViewModel", "Event saved successfully")
                } else {
                    Toast.makeText(
                        context,
                        "Could not get coordinates for the address",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: CancellationException) {
                Log.e("CreateEventViewModel", "Coroutine was cancelled: ${e.message}", e)
            } catch (e: Exception) {
                Log.e("CreateEventViewModel", "Error saving event: ${e.message}", e)
                Toast.makeText(context, "Error saving event: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private suspend fun getCoordinatesFromAddress(
        context: Context,
        address: String
    ): Pair<Double, Double>? {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addressList = geocoder.getFromLocationName(address, 1)
            if (addressList?.isNotEmpty() == true) {
                val location = addressList[0]
                Pair(location.latitude, location.longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("CreateEventViewModel", "Geocoding error: ${e.message}", e)
            null
        }
    }

    private fun uploadImageToFirebase(uri: Uri) = FirebaseStorage.getInstance().reference
        .child("event_images/${UUID.randomUUID()}.jpg")
        .putFile(uri)
        .continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception ?: Exception("Image upload failed")
            }
            task.result?.storage?.downloadUrl
        }

    private fun saveEvent(
        title: String, date: String, time: String, address: String, description: String,
        latitude: Double, longitude: Double, imageUrl: String
    ) {
        val firestore = FirebaseFirestore.getInstance()
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

        firestore.collection("events").add(event)
            .addOnSuccessListener {
                Log.d("CreateEventViewModel", "Event created successfully")
            }
            .addOnFailureListener { e ->
                Log.e("CreateEventViewModel", "Error creating event: ${e.message}", e)
            }
    }

    private fun sendMessageToTopic(message: String, topic: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val jsonObject = JSONObject().apply {
                    put("message", message)
                    put("topic", topic)
                }

                Log.d("remoteMessage", "Sending message to topic: $topic")

                val requestBody =
                    jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("https://us-central1-p15eventorias.cloudfunctions.net/sendNotificationToTopic")
                    // .addHeader("Authorization", "Bearer $identityToken")  // Adding the Authorization header
                    .post(requestBody)
                    .build()

                Log.d("remoteMessage", "Request: $request")
                val response = client.newCall(request).execute()
                Log.d("remoteMessage", "Response: ${response.message}")

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        Log.d("remoteMessage", "Message sent successfully: $responseBody")
                    } else {
                        val errorBody = response.body?.string()
                        Log.e("remoteMessage", "Message: $message, Topic: $topic")
                        Log.e(
                            "remoteMessage",
                            "Failed to send message: ${response.message}, Error body: $errorBody"
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("remoteMessage", "Exception occurred: ${e.message}")
                }
            }
        }
    }

}
data class CreateEventState(
    val title: String = "",
    val date: String = "",
    val time: String = "",
    val address: String = "",
    val description: String = "",
    val imageUri: Uri? = null
)