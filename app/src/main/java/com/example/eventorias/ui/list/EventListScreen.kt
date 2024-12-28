package com.example.eventorias.ui.list

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.eventorias.data.Event
import com.example.eventorias.ui.add.CreateEventActivity
import com.example.eventorias.ui.detail.EventDetailActivity
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

                    Event(id, title, description, imageUrl, latitude, longitude, date, time, address)
                } ?: emptyList()

                events = newEvents
                filteredEvents = filterAndSortEvents(events, searchText.text, isSortedDescending)
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Search Bar
        SearchBar(searchText) { text ->
            searchText = text
            filteredEvents = filterAndSortEvents(events, text.text, isSortedDescending)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sort Button
        SortButton(isSortedDescending) {
            isSortedDescending = !isSortedDescending
            filteredEvents = filterAndSortEvents(events, searchText.text, isSortedDescending)
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        // FAB
        FloatingActionButton(
            onClick = {
                val context = context
                val intent = Intent(context, CreateEventActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Event")
        }
    }
}

@Composable
fun SearchBar(searchText: TextFieldValue, onSearchTextChange: (TextFieldValue) -> Unit) {
    TextField(
        value = searchText,
        onValueChange = onSearchTextChange,
        modifier = Modifier.fillMaxWidth().padding(16.dp),

    )
}

@Composable
fun SortButton(isSortedDescending: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier) {
        Text(text = if (isSortedDescending) "Sort Ascending" else "Sort Descending")
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
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp).clickable { onEventClick(event) }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.title, style = MaterialTheme.typography.titleSmall)
            Text(text = event.description, style = MaterialTheme.typography.bodyMedium)
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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    EventListScreen()
}
