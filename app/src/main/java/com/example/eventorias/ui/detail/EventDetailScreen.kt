package com.example.eventorias.ui.detail

import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.eventorias.R
import com.example.eventorias.data.Event
import com.example.eventorias.ui.theme.app_white
import com.example.eventorias.ui.theme.dark
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class EventDetailActivity : ComponentActivity() {

    private val viewModel: EventDetailViewModel by viewModels {
        EventDetailViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the event ID from the intent
        val eventId = intent.getStringExtra("EVENT_ID") ?: ""

        // Set the content of the activity
        setContent {
            // Create a NavController for navigation
            val navController = rememberNavController()

            // Pass eventId and NavController to the EventDetailScreen
            EventDetailScreen(
                eventId = eventId,
                navController = navController
            )
        }
    }
}

@Composable
fun EventDetailScreen(eventId: String, navController: NavController) {

    val context = LocalContext.current
    val viewModel: EventDetailViewModel = viewModel(
        factory = EventDetailViewModelFactory(context)
    )
    val event by viewModel.event.collectAsState()
    val mapImageUrl by viewModel.mapImageUrl.collectAsState()

    // Fetch event details when the screen is launched
    LaunchedEffect(eventId) {
        viewModel.fetchEventDetails(eventId, context)
    }

    // Render event details
    event?.let { eventData ->
        EventDetailUI(
            event = eventData,
            mapImageUrl = mapImageUrl,
            onBackPressed = { navController.popBackStack() },
            viewModel = viewModel
        )
    }
}

@Composable
fun EventDetailUI(event: Event, mapImageUrl: String?, onBackPressed: () -> Unit, viewModel: EventDetailViewModel) {
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
                        .align(Alignment.CenterHorizontally), // Align image horizontally center
                        contentScale = ContentScale.Crop // Crop the image to fill the bounds

                )
            }

            Column(
                modifier = Modifier.width(364.dp).fillMaxHeight()
                    .padding(40.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween // Add space between children
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.calendar), // Load drawable using painterResource
                                contentDescription = "Calendar Icon",
                                tint = app_white
                            )

                            Spacer(modifier = Modifier.padding(8.dp))

                            BasicText(
                                text = viewModel.formatDate(event.date), // Use the formatted date
                                style = MaterialTheme.typography.bodyMedium.copy(color = app_white)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.time), // Load drawable using painterResource
                                contentDescription = "Time Icon",
                                tint = app_white
                            )

                            Spacer(modifier = Modifier.padding(8.dp))

                            BasicText(
                                text = viewModel.formatTime(event.time),
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween // Add space between children
                ) {
                    BasicText(
                        text = event.address,
                        style = MaterialTheme.typography.bodyMedium.copy(color = app_white)
                    )
                    // Load map image if URL is available
                    mapImageUrl?.let { url ->
                        Image(
                            painter = rememberImagePainter(url),
                            contentDescription = "Event Location Map",
                            modifier = Modifier
                                .width(180.dp)
                                .height(100.dp)
                                .padding(top = 16.dp)
                                .clip(MaterialTheme.shapes.medium), // Crop with rounded corners (optional)
                            contentScale = ContentScale.Crop
                        )
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
            .clip(RoundedCornerShape(50.dp)), // Crop with rounded corners (adjust radius as needed)
            contentScale = ContentScale.Crop // Crop the image to fill the bounds

    )
}
