package com.example.eventorias.ui.detail

import android.content.Context
import android.location.Geocoder
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.eventorias.data.Event
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import com.example.eventorias.BuildConfig


@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class EventDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher()

    @Mock
    private lateinit var mockFirestore: FirebaseFirestore

    @Mock
    private lateinit var mockGeocoder: Geocoder

    @Mock
    private lateinit var mockContext: Context


    @Mock
    private lateinit var mockDocumentSnapshot: DocumentSnapshot

    private lateinit var eventDetailViewModel: EventDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        eventDetailViewModel = EventDetailViewModel(mockFirestore, mockGeocoder)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }


    @Test
    fun `fetchEventDetails should update event state when document exists`() = runBlockingTest {
        // Arrange
        val eventId = "event123"
        val event = Event(id = eventId, title = "Test Event", address = "123 Test St")

        // Mock the Task<DocumentSnapshot>
        val mockTask = mock(Task::class.java) as Task<DocumentSnapshot>
        // Ensure the Task returns itself for chaining
        `when`(mockTask.addOnSuccessListener(any())).thenAnswer {
            val listener = it.getArgument<OnSuccessListener<DocumentSnapshot>>(0)
            listener.onSuccess(mockDocumentSnapshot) // Trigger the success listener
            mockTask
        }
        `when`(mockTask.addOnFailureListener(any())).thenAnswer {
            mockTask
        }

        // Mock the DocumentSnapshot
        `when`(mockDocumentSnapshot.exists()).thenReturn(true)
        `when`(mockDocumentSnapshot.toObject(Event::class.java)).thenReturn(event)

        // Mock the DocumentReference
        val mockDocumentReference = mock(DocumentReference::class.java)
        `when`(mockDocumentReference.get()).thenReturn(mockTask)

        // Mock the CollectionReference
        val mockCollectionReference = mock(CollectionReference::class.java)
        `when`(mockCollectionReference.document(eventId)).thenReturn(mockDocumentReference)

        // Mock Firestore to return the CollectionReference
        `when`(mockFirestore.collection("events")).thenReturn(mockCollectionReference)

        // Act
        eventDetailViewModel.fetchEventDetails(eventId, mockContext)

        // Assert
        assert(eventDetailViewModel.event.value == event)
    }


    @Test
    fun `fetchEventDetails should not update event state when document does not exist`() = runBlockingTest {
        // Arrange
        val eventId = "event123"

        // Mock the Task<DocumentSnapshot>
        val mockTask = mock(Task::class.java) as Task<DocumentSnapshot>
        // Ensure the Task returns itself for chaining
        `when`(mockTask.addOnSuccessListener(any())).thenAnswer {
            val listener = it.getArgument<OnSuccessListener<DocumentSnapshot>>(0)
            listener.onSuccess(mockDocumentSnapshot) // Trigger the success listener
            mockTask
        }
        // Mock the DocumentSnapshot
        `when`(mockDocumentSnapshot.exists()).thenReturn(false)

        // Mock the DocumentReference
        val mockDocumentReference = mock(DocumentReference::class.java)
        `when`(mockDocumentReference.get()).thenReturn(mockTask)

        // Mock the CollectionReference
        val mockCollectionReference = mock(CollectionReference::class.java)
        `when`(mockCollectionReference.document(eventId)).thenReturn(mockDocumentReference)

        // Mock Firestore to return the CollectionReference
        `when`(mockFirestore.collection("events")).thenReturn(mockCollectionReference)

        // Act
        eventDetailViewModel.fetchEventDetails(eventId, mockContext)

        // Assert
        assert(eventDetailViewModel.event.value == null)
    }




    @Test
    fun `getCoordinatesFromAddress should return coordinates when address is valid`() {
        // Arrange
        val address = "123 Test St"
        val latitude = 37.7749
        val longitude = -122.4194
        val mockAddress = mock(android.location.Address::class.java)
        `when`(mockAddress.latitude).thenReturn(latitude)
        `when`(mockAddress.longitude).thenReturn(longitude)
        `when`(mockGeocoder.getFromLocationName(eq(address), eq(1))).thenReturn(listOf(mockAddress))

        // Act
        val coordinates = eventDetailViewModel.getCoordinatesFromAddress(address, mockContext)

        // Assert
        assert(coordinates == Pair(latitude, longitude))
    }

    @Test
    fun `getCoordinatesFromAddress should return null when address is invalid`() {
        // Arrange
        val address = "Invalid Address"
        `when`(mockGeocoder.getFromLocationName(eq(address), eq(1))).thenReturn(emptyList())

        // Act
        val coordinates = eventDetailViewModel.getCoordinatesFromAddress(address, mockContext)

        // Assert
        assert(coordinates == null)
    }








    @Test
    fun `generateMapImageUrl should return correct URL`() {
        // Arrange
        val latitude = 37.7749
        val longitude = -122.4194
        val apiKey = BuildConfig.MY_API_KEY
        System.setProperty("MY_API_KEY", apiKey) // Mock the API key

        // Act
        val mapImageUrl = eventDetailViewModel.generateMapImageUrl(latitude, longitude)

        // Expected URL
        val expectedUrl = "https://maps.googleapis.com/maps/api/staticmap?center=$latitude,$longitude&zoom=14&size=400x400&key=$apiKey"

        // Print the expected and actual URLs for debugging
        println("Expected URL: $expectedUrl")
        println("Actual URL: $mapImageUrl")

        // Assert
        assert(mapImageUrl == expectedUrl)
    }

    @Test
    fun `formatDate should return formatted date`() {
        // Arrange
        val dateString = "31/12/2023"
        val expectedFormattedDate = "December 31, 2023"

        // Act
        val formattedDate = eventDetailViewModel.formatDate(dateString)

        // Assert
        assert(formattedDate == expectedFormattedDate)
    }


    @Test
    fun `formatTime should return formatted time`() {
        // Arrange
        val timeString = "12:30"
        val expectedFormattedTime = "12:30 pm"

        // Act
        val formattedTime = eventDetailViewModel.formatTime(timeString)

        // Log the actual and expected values
        println("Expected: $expectedFormattedTime")
        println("Actual: $formattedTime")

        // Assert
        assert(formattedTime == expectedFormattedTime)

        assertTrue(
            "Expected: $expectedFormattedTime, but got: $formattedTime",
            formattedTime == expectedFormattedTime

        )
    }
}