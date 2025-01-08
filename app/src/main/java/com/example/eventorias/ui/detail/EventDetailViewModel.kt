package com.example.eventorias.ui.detail

import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventorias.data.Event
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.location.Geocoder
import com.example.eventorias.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventDetailViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    // State to hold the event data
    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> get() = _event

    // State to hold the map image URL
    private val _mapImageUrl = MutableStateFlow<String?>(null)
    val mapImageUrl: StateFlow<String?> get() = _mapImageUrl

    // Function to fetch event details
    fun fetchEventDetails(eventId: String, context: Context) {
        viewModelScope.launch {
            val eventDocRef = firestore.collection("events").document(eventId)
            eventDocRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val eventObj = document.toObject(Event::class.java)
                    _event.value = eventObj

                    // Generate map URL if address is available
                    eventObj?.address?.let { address ->
                        val coordinates = getCoordinatesFromAddress(address, context)
                        coordinates?.let { (latitude, longitude) ->
                            _mapImageUrl.value = generateMapImageUrl(latitude, longitude)
                        }
                    }
                } else {
                    Log.d("EventDetail", "No such document")
                }
            }.addOnFailureListener { e ->
                Log.e("EventDetail", "Error fetching document", e)
            }
        }
    }

    // Function to get coordinates from an address
    fun getCoordinatesFromAddress(address: String, context: Context): Pair<Double, Double>? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addressList = geocoder.getFromLocationName(address, 1)
            if (addressList.isNullOrEmpty()) {
                null
            } else {
                val location = addressList[0]
                Pair(location.latitude, location.longitude)
            }
        } catch (e: Exception) {
            Log.e("EventDetail", "Geocoding error: ${e.message}")
            null
        }
    }

    // Function to generate a map image URL
    fun generateMapImageUrl(latitude: Double, longitude: Double): String {
        val apiKey = BuildConfig.MY_API_KEY
        return "https://maps.googleapis.com/maps/api/staticmap?center=$latitude,$longitude&zoom=14&size=400x400&key=$apiKey"
    }

    fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""

        // Define the input and output date formats
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Match the input format
        val outputFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

        return try {
            val date: Date = inputFormat.parse(dateString) ?: return ""
            outputFormat.format(date)
        } catch (e: Exception) {
            Log.e("EventDetail", "Error formatting date: ${e.message}")
            ""
        }
    }

    fun formatTime(timeString: String?): String {
        if (timeString.isNullOrEmpty()) return ""

        // Define the input and output time formats
        val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault()) // Adjust input format as needed
        val outputFormat = SimpleDateFormat("hh:mm  a", Locale.getDefault())

        return try {
            val time: Date = inputFormat.parse(timeString) ?: return ""
            outputFormat.format(time)
        } catch (e: Exception) {
            Log.e("EventDetail", "Error formatting time: ${e.message}")
            ""
        }
    }

}