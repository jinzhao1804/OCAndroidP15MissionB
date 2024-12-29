package com.example.eventorias.ui.detail

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.eventorias.api.ApiKey
import com.example.eventorias.data.Event
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class EventDetailActivity : ComponentActivity() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EventDetailScreen(eventId = intent.getStringExtra("EVENT_ID") ?: "")
        }
    }
}

@Composable
fun EventDetailScreen(eventId: String) {
    val firestore = FirebaseFirestore.getInstance()
    val event = remember { mutableStateOf<Event?>(null) }

    // Fetch event details
    LaunchedEffect(eventId) {
        fetchEventDetails(firestore, eventId, event)
    }

    // Render event details
    event.value?.let {
        EventDetailUI(event = it)
    }
}

private fun fetchEventDetails(
    firestore: FirebaseFirestore,
    eventId: String,
    event: MutableState<Event?>
) {
    val eventDocRef = firestore.collection("events").document(eventId)

    eventDocRef.get().addOnSuccessListener { document ->
        if (document != null && document.exists()) {
            val eventObj = document.toObject(Event::class.java)
            event.value = eventObj
        } else {
            Log.d("EventDetail", "No such document")
        }
    }.addOnFailureListener { e ->
        Log.e("EventDetail", "Error fetching document", e)
    }
}

@Composable
fun EventDetailUI(event: Event) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        event.imageUrl?.let {
            Image(
                painter = rememberImagePainter(it),
                contentDescription = "Event Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(bottom = 16.dp)
            )
        }

        BasicText(text = event.date, style = MaterialTheme.typography.bodyMedium)
        BasicText(text = event.time, style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(8.dp))

        BasicText(
            text = event.description,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        BasicText(text = event.address, style = MaterialTheme.typography.bodyMedium)

        // Load map based on address
        event.address?.let { address ->
            val coordinates = getCoordinatesFromAddress(address)
            coordinates?.let { (latitude, longitude) ->
                loadMapImage(latitude, longitude)
            }
        }
    }
}

@Composable
fun getCoordinatesFromAddress(address: String): Pair<Double, Double>? {
    val context = LocalContext.current
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

@Composable
fun loadMapImage(latitude: Double, longitude: Double) {
    //val apiKey = System.getenv("API_KEY")
    val apiKey = ApiKey.API_KEY

    val mapImageUrl = "https://maps.googleapis.com/maps/api/staticmap?center=$latitude,$longitude&zoom=14&size=400x400&key=$apiKey"

    Image(
        painter = rememberImagePainter(mapImageUrl),
        contentDescription = "Event Location Map",
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(top = 16.dp)
    )
}
