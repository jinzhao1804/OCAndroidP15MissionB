package com.example.eventorias.ui.detail

import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.eventorias.BuildConfig
import com.example.eventorias.R
import com.example.eventorias.api.ApiKey
import com.example.eventorias.data.Event
import com.example.eventorias.ui.theme.app_white
import com.example.eventorias.ui.theme.dark
import com.google.firebase.firestore.FirebaseFirestore
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class EventDetailActivity : ComponentActivity() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the event ID from the intent
        val eventId = intent.getStringExtra("EVENT_ID") ?: ""

        // Set the content of the activity
        setContent {
            // Create a NavController for navigation
            val navController = rememberNavController()

            // Pass Firestore and NavController to the EventDetailScreen
            EventDetailScreen(
                eventId = eventId,
                navController = navController
            )
        }
    }
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


@Composable
fun EventDetailScreen(eventId: String, navController: NavController) {
    val firestore = FirebaseFirestore.getInstance()
    val event = remember { mutableStateOf<Event?>(null) }

    // Fetch event details
    LaunchedEffect(eventId) {
        fetchEventDetails(firestore, eventId, event)
    }

    // Render event details
    event.value?.let { eventData ->
        EventDetailUI(
            event = eventData,
            onBackPressed = { navController.popBackStack() } // Handle back navigation
        )
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
fun EventDetailUI(event: Event, onBackPressed: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = dark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally // Center content horizontally

        ) {
            // Add a spacer to make space for the title and back arrow
            Spacer(modifier = Modifier.height(56.dp))

            event.imageUrl?.let {
                Image(
                    painter = rememberImagePainter(it),
                    contentDescription = "Event Image",
                    modifier = Modifier
                        .width(364.dp)
                        .height(354.dp)
                        .padding(bottom = 48.dp)
                        .align(Alignment.CenterHorizontally) // Align image horizontally center

                )
            }

            Column(
                modifier = Modifier.width(364.dp).fillMaxHeight()
                    .padding(40.dp)
            ) {
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween, // Add space between children

                ){
                    Column {
                        Row (
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Icon(
                                painter = painterResource(id = R.drawable.calendar), // Load drawable using painterResource
                                contentDescription = "Calendar Icon",
                                tint = app_white

                            )

                            Spacer(modifier = Modifier.padding(8.dp))

                            BasicText(
                                text = formatDate(event.date), // Use the formatted date
                                style = MaterialTheme.typography.bodyMedium.copy(color = app_white)
                            )
                        }

                        Row  (
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Icon(
                                painter = painterResource(id = R.drawable.time), // Load drawable using painterResource
                                contentDescription = "Time Icon",
                                tint = app_white

                            )

                            Spacer(modifier = Modifier.padding(8.dp))

                            BasicText(
                                text = formatTime(event.time),
                                style = MaterialTheme.typography.bodyMedium.copy(color = app_white)
                            )
                        }
                    }

                    event.imageUrl?.let {
                        RoundedImage(
                            painter = rememberImagePainter(it),
                            contentDescription = "Event Image"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                BasicText(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium.copy(color = app_white),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row (
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween, // Add space between children

                ){
                    BasicText(
                        text = event.address,
                        style = MaterialTheme.typography.bodyMedium.copy(color = app_white)
                    )
                    // Load map based on address
                    event.address?.let { address ->
                        val coordinates = getCoordinatesFromAddress(address)
                        coordinates?.let { (latitude, longitude) ->
                            loadMapImage(latitude, longitude)
                        }
                    }
                }
            }

        }
        // Back arrow and title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = colorResource(id = R.color.app_white) // Use colorResource to fetch the color
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
                color = app_white
            )
        }
    }
}

@Composable
fun RoundedImage(painter: Painter, contentDescription: String) {
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = Modifier
            .size(60.dp) // Set height and width to 80.dp
            .clip(RoundedCornerShape(50.dp)) // Crop with rounded corners (adjust radius as needed)
    )
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

    val apiKey = ApiKey.API_KEY

    val mapImageUrl = "https://maps.googleapis.com/maps/api/staticmap?center=$latitude,$longitude&zoom=14&size=400x400&key=$apiKey"

    Image(
        painter = rememberImagePainter(mapImageUrl),
        contentDescription = "Event Location Map",
        modifier = Modifier
            .width(180.dp)
            .height(100.dp)
            .padding(top = 16.dp)
            .clip(MaterialTheme.shapes.medium), // Crop with rounded corners (optional)
        contentScale = ContentScale.Crop
    )
}
