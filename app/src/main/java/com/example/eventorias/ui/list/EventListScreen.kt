package com.example.eventorias.ui.list

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.eventorias.R
import com.example.eventorias.R.color
import com.example.eventorias.data.Event
import com.example.eventorias.data.User
import com.example.eventorias.ui.add.CreateEventActivity
import com.example.eventorias.ui.detail.EventDetailActivity
import com.example.eventorias.ui.theme.dark
import com.example.eventorias.ui.theme.grey
import com.google.firebase.firestore.FirebaseFirestore
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EventListScreen() {
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var filteredEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    var isSortedDescending by remember { mutableStateOf(true) }

    val context = LocalContext.current

    val firestore = FirebaseFirestore.getInstance()

    // Listen to Firestore for updates
    LaunchedEffect(Unit) {
        firestore.collection("events")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(context, "Failed to fetch events: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val newEvents = snapshot?.documents?.map { document ->
                    val id = document.id
                    val title = document.getString("title") ?: ""
                    val description = document.getString("description") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val latitude = document.getDouble("latitude") ?: 0.0
                    val longitude = document.getDouble("longitude") ?: 0.0
                    val date = document.getString("date") ?: ""
                    val time = document.getString("time") ?: ""
                    val address = document.getString("address") ?: ""
                    // Assuming 'user' is a DocumentReference field in Firestore
                    val userRef = document.getDocumentReference("user")

                    // Fetch the user data from Firestore asynchronously
                    val userSnapshot = userRef?.get()?.result

                    val user = userSnapshot?.toObject(User::class.java) ?: User()

                    Event(id, title, description, imageUrl, latitude, longitude, date, time, address, user)
                } ?: emptyList()

                events = newEvents
                filteredEvents = filterAndSortEvents(events, searchText.text, isSortedDescending)
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(dark)
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, bottom = 88.dp)
            .background(dark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp)
                        ,
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ){

                Text(
                    text = "Event list",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White // Set text color to white
                )


                // Search Bar
                SearchBar(searchText) { text ->
                    searchText = text
                    filteredEvents = filterAndSortEvents(events, text.text, isSortedDescending)
                }

                // Sort Button
                SortButton(isSortedDescending) {
                    isSortedDescending = !isSortedDescending
                    filteredEvents = filterAndSortEvents(events, searchText.text, isSortedDescending)
                }
            }

            //Spacer(modifier = Modifier.height(16.dp))

            // Event List
            EventList(events = filteredEvents) { event ->
                val context = context
                val intent = Intent(context, EventDetailActivity::class.java).apply {
                    putExtra("EVENT_ID", event.id)
                    putExtra("EVENT_TITLE", event.title)
                    putExtra("EVENT_DESCRIPTION", event.description)
                    putExtra("EVENT_IMAGE_URL", event.imageUrl)
                    putExtra("EVENT_LATITUDE", event.latitude)
                    putExtra("EVENT_LONGITUDE", event.longitude)
                }
                context.startActivity(intent)
            }

            Spacer(modifier = Modifier.height(16.dp))


        }
    }
}


@Composable
fun SearchBar(
    searchText: TextFieldValue,
    onSearchTextChange: (TextFieldValue) -> Unit
) {
    val isSearchActive = remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
            TextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                modifier = Modifier
                    .width(220.dp) // Limiting the width of the search bar
                    .padding(16.dp),
                placeholder = { if (isSearchActive.value) Text("Search")},
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = if (isSearchActive.value) grey else dark,
                    unfocusedContainerColor = if (isSearchActive.value) dark else dark,
                    disabledContainerColor = dark,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        Button(
            onClick = {
                isSearchActive.value = !isSearchActive.value // Toggle the state
            },
            modifier = Modifier.size(48.dp), // Adjust size of the button if needed
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp), // Remove padding to only show the icon
            shape = RectangleShape // Make sure it has no border or default rounded corners
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        }
    }
}


@Composable
fun SortButton(isSortedDescending: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick,
        modifier = Modifier.size(48.dp), // Adjust size of the button if needed
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp), // Remove padding to only show the icon
        shape = RectangleShape
    ) {
        if (isSortedDescending) {
            Image(
                painter = painterResource(id = R.drawable.sort), // Use painterResource for drawable
                contentDescription = "Sort Icon"
            )
        } else {
            Icon(
                imageVector = Icons.Default.ArrowDropDown, // Use imageVector for Material icons
                contentDescription = "Arrow Drop Down",
                tint = colorResource(id = R.color.app_white)
            )
        }

    }
}

@Composable
fun EventList(events: List<Event>, onEventClick: (Event) -> Unit) {
    LazyColumn {
        items(events) { event ->
            EventItem(event, onEventClick)
        }
    }
}

@Composable
fun EventItem(event: Event, onEventClick: (Event) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = grey,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .padding(8.dp)
            .clickable { onEventClick(event) }) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween, // This ensures the last image is placed at the end
            verticalAlignment = Alignment.CenterVertically // Aligns all items vertically in the center
        ) {
            
            Spacer(modifier = Modifier.padding(8.dp))
            // User profile image
            Image(
                painter = rememberImagePainter(event.imageUrl),
                contentDescription = "User profile picture",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop // Scale the image to fill the space and crop

            )

            val whiteTextStyle = TextStyle(color = colorResource(id = R.color.app_white)) // Use colorResource to fetch the color

            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleSmall.merge(whiteTextStyle)
                )
                Text(
                    text = event.date,
                    style = MaterialTheme.typography.bodyMedium.merge(whiteTextStyle)
                )
            }
            // Event image at the end of the row
            Image(
                painter = rememberImagePainter(event.imageUrl),
                contentDescription = "Event image",
                modifier = Modifier
                    .fillMaxHeight()
                    .width(180.dp)
                    .aspectRatio(1f)

            )
        }
    }
}


fun filterAndSortEvents(events: List<Event>, query: String, isDescending: Boolean): List<Event> {
    val filteredEvents = events.filter { it.title.contains(query, ignoreCase = true) }
    val sortedEvents = filteredEvents.sortedByDescending { parseDate(it.date) }.toMutableList()
    if (!isDescending) {
        sortedEvents.reverse()
    }
    return sortedEvents
}

fun parseDate(dateString: String): Date? {
    val dateFormats = arrayOf(
        SimpleDateFormat("M/d/yyyy", Locale.getDefault()),
        SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    )

    for (format in dateFormats) {
        try {
            return format.parse(dateString)
        } catch (e: ParseException) {
            // Continue to try other formats
        }
    }
    return null
}
