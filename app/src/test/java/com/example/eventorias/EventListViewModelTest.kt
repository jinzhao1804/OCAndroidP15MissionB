package com.example.eventorias

import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import com.example.eventorias.data.Event
import com.example.eventorias.data.User
import com.example.eventorias.ui.list.EventListViewModel
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.text.SimpleDateFormat

@ExperimentalCoroutinesApi
class EventListViewModelTest {

    @Mock
    private lateinit var mockFirestore: FirebaseFirestore

    @Mock
    private lateinit var mockQuerySnapshot: QuerySnapshot

    @Mock
    private lateinit var mockCollectionReference: CollectionReference

    @Mock
    private lateinit var mockDocumentSnapshot: DocumentSnapshot

    @Mock
    private lateinit var mockDocumentReference: DocumentReference

    @Mock
    private lateinit var mockListenerRegistration: ListenerRegistration

    private lateinit var viewModel: EventListViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // Suppress Log.e calls
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0

        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)


        // Mock Firestore to return a valid CollectionReference
        whenever(mockFirestore.collection("events")).thenReturn(mockCollectionReference)
        viewModel = EventListViewModel(mockFirestore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    @Test
    fun `parseDate should return correct date for valid formats`() {
        // Arrange
        val dateString1 = "1/1/2023"
        val dateString2 = "January 1, 2023"

        // Act
        val date1 = viewModel.parseDate(dateString1)
        val date2 = viewModel.parseDate(dateString2)

        // Assert
        assertEquals("2023-01-01", SimpleDateFormat("yyyy-MM-dd").format(date1!!))
        assertEquals("2023-01-01", SimpleDateFormat("yyyy-MM-dd").format(date2!!))
    }

    @Test
    fun `parseDate should return null for invalid format`() {
        // Arrange
        val invalidDateString = "Invalid Date"

        // Act
        val date = viewModel.parseDate(invalidDateString)

        // Assert
        assertEquals(null, date)
    }


    @Test
    fun `fetchEvents should update state with events`() = runTest {
        // Arrange
        val mockEvents = listOf(
            Event(
                id = "1",
                title = "Event 1",
                description = "Description 1",
                imageUrl = "url1",
                latitude = 0.0,
                longitude = 0.0,
                date = "10/10/2023",
                time = "10:00",
                address = "Address 1",
                user = User()
            )
        )

        // Mock CollectionReference behavior
        whenever(mockCollectionReference.addSnapshotListener(any())).thenAnswer {
            val listener = it.getArgument<EventListener<QuerySnapshot>>(0)
            listener.onEvent(mockQuerySnapshot, null) // Simulate a successful Firestore response
            mockListenerRegistration // Return a mock ListenerRegistration
        }

        // Mock QuerySnapshot behavior
        whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
        whenever(mockDocumentSnapshot.id).thenReturn("1")
        whenever(mockDocumentSnapshot.getString("title")).thenReturn("Event 1")
        whenever(mockDocumentSnapshot.getString("description")).thenReturn("Description 1")
        whenever(mockDocumentSnapshot.getString("imageUrl")).thenReturn("url1")
        whenever(mockDocumentSnapshot.getDouble("latitude")).thenReturn(0.0)
        whenever(mockDocumentSnapshot.getDouble("longitude")).thenReturn(0.0)
        whenever(mockDocumentSnapshot.getString("date")).thenReturn("10/10/2023")
        whenever(mockDocumentSnapshot.getString("time")).thenReturn("10:00")
        whenever(mockDocumentSnapshot.getString("address")).thenReturn("Address 1")
        whenever(mockDocumentSnapshot.getDocumentReference("user")).thenReturn(mockDocumentReference)
        whenever(mockDocumentReference.get()).thenReturn(mock())
        whenever(mockDocumentSnapshot.toObject(User::class.java)).thenReturn(User())

        // Act
        viewModel.fetchEvents()

        // Wait for the coroutine to complete
        advanceUntilIdle()

        // Assert
        assertEquals(mockEvents, viewModel.state.value.events)
    }

    @Test
    fun `onSearchTextChange should update filteredEvents`() = runTest {
        // Arrange
        val initialEvents = listOf(
            Event(
                id = "1",
                title = "Event 1",
                description = "Description 1",
                imageUrl = "url1",
                latitude = 0.0,
                longitude = 0.0,
                date = "10/10/2023",
                time = "10:00",
                address = "Address 1",
                user = User()
            ),
            Event(
                id = "2",
                title = "Event 2",
                description = "Description 2",
                imageUrl = "url2",
                latitude = 0.0,
                longitude = 0.0,
                date = "10/11/2023",
                time = "11:00",
                address = "Address 2",
                user = User()
            )
        )

        viewModel._state.value = viewModel.state.value.copy(events = initialEvents)

        // Act
        viewModel.onSearchTextChange(TextFieldValue("Event 1"))

        // Assert
        assertEquals(1, viewModel.state.value.filteredEvents.size)
        assertEquals("Event 1", viewModel.state.value.filteredEvents[0].title)
    }

    @Test
    fun `onSortToggle should reverse the order of filteredEvents`() = runTest {
        // Arrange
        val initialEvents = listOf(
            Event(
                id = "1",
                title = "Event 1",
                description = "Description 1",
                imageUrl = "url1",
                latitude = 0.0,
                longitude = 0.0,
                date = "10/10/2023",
                time = "10:00",
                address = "Address 1",
                user = User()
            ),
            Event(
                id = "2",
                title = "Event 2",
                description = "Description 2",
                imageUrl = "url2",
                latitude = 0.0,
                longitude = 0.0,
                date = "10/11/2023",
                time = "11:00",
                address = "Address 2",
                user = User()
            )
        )

        viewModel._state.value = viewModel.state.value.copy(events = initialEvents, isSortedDescending = true)

        // Act
        viewModel.onSortToggle()

        // Assert
        assertEquals("Event 1", viewModel.state.value.filteredEvents[0].title)
    }







}