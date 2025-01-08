package com.example.eventorias.ui.list

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventorias.data.Event
import com.example.eventorias.data.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventListViewModel : ViewModel() {
    // Use a single MutableStateFlow to manage all state
    private val _state = MutableStateFlow(EventListState())
    val state: StateFlow<EventListState> = _state.asStateFlow()


    private val firestore = FirebaseFirestore.getInstance()

    init {
        fetchEvents()
    }

    fun fetchEvents() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, hasError = false)
            firestore.collection("events")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        _state.value = _state.value.copy(isLoading = false, hasError = true)
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
                        val userRef = document.getDocumentReference("user")

                        val userSnapshot = userRef?.get()?.result
                        val user = userSnapshot?.toObject(User::class.java) ?: User()

                        Event(id, title, description, imageUrl, latitude, longitude, date, time, address, user)
                    } ?: emptyList()

                    Log.e("fetch event","Fetched events: $newEvents") // Add logging

                    _state.value = _state.value.copy(
                        events = newEvents,
                        filteredEvents = filterAndSortEvents(newEvents, _state.value.searchText.text, _state.value.isSortedDescending),
                        isLoading = false,
                        hasError = false
                    )
                }
        }
    }
    fun onSearchTextChange(text: TextFieldValue) {
        _state.value = _state.value.copy(
            searchText = text, // Update with TextFieldValue
            filteredEvents = filterAndSortEvents(_state.value.events, text.text, _state.value.isSortedDescending)
        )
    }


    fun onSortToggle() {
        val isDescending = !_state.value.isSortedDescending
        _state.value = _state.value.copy(
            isSortedDescending = isDescending,
            filteredEvents = filterAndSortEvents(_state.value.events,
                _state.value.searchText.toString(), isDescending)
        )
    }


    private fun filterAndSortEvents(events: List<Event>, query: String, isDescending: Boolean): List<Event> {
        // If the query is empty, return all events
        val filteredEvents = if (query.isEmpty()) {
            events
        } else {
            // Filter events based on the query
            events.filter { it.title.contains(query, ignoreCase = true) }
        }

        // Sort events by date
        val sortedEvents = filteredEvents.sortedByDescending { parseDate(it.date) }.toMutableList()
        if (!isDescending) {
            sortedEvents.reverse()
        }
        return sortedEvents
    }

    private fun parseDate(dateString: String): Date? {
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
}

data class EventListState(
    val events: List<Event> = emptyList(),
    val filteredEvents: List<Event> = emptyList(),
    val searchText: TextFieldValue = TextFieldValue(""), // Use TextFieldValue
    val isSortedDescending: Boolean = true,
    val isLoading: Boolean = false,
    val hasError: Boolean = false
)