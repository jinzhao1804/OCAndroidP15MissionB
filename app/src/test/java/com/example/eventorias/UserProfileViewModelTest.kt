package com.example.eventorias

import android.content.Context
import com.example.eventorias.ui.profile.UserProfileViewModel
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class UserProfileViewModelTest {

    @Mock
    private lateinit var mockAuth: FirebaseAuth

    @Mock
    private lateinit var mockFirestore: FirebaseFirestore

    @Mock
    private lateinit var mockAuthUI: AuthUI

    @Mock
    private lateinit var mockFirebaseUser: FirebaseUser

    @Mock
    private lateinit var mockDocumentSnapshot: DocumentSnapshot

    @Mock
    private lateinit var mockContext: Context

    @Mock
    lateinit var mockCollectionRef: CollectionReference

    @Mock
    lateinit var mockDocumentRef: DocumentReference


    private lateinit var viewModel: UserProfileViewModel

    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        MockitoAnnotations.initMocks(this)
        viewModel = UserProfileViewModel(mockAuth, mockFirestore, mockAuthUI)
        whenever(mockFirestore.collection(anyString())).thenReturn(mockCollectionRef)
        whenever(mockCollectionRef.document(anyString())).thenReturn(mockDocumentRef)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `loadUserProfile should update userName and userEmail when user is logged in`() = runBlockingTest {
        // Arrange
        val expectedUserName = "John Doe"
        val expectedUserEmail = "john.doe@example.com"

        // Mock Firebase Auth
        whenever(mockAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.displayName).thenReturn(expectedUserName)
        whenever(mockFirebaseUser.email).thenReturn(expectedUserEmail)
        whenever(mockFirebaseUser.uid).thenReturn("123")

        // Mock Firestore
        whenever(mockFirestore.collection("users")).thenReturn(mockCollectionRef)
        whenever(mockCollectionRef.document("123")).thenReturn(mockDocumentRef)

        // Mock the Task returned by DocumentReference.get()
        val mockTask = mock<Task<DocumentSnapshot>>()
        whenever(mockDocumentRef.get()).thenReturn(mockTask)
        // Act
        viewModel.loadUserProfile()

        // Print expected and actual values for debugging
        println("Expected userName: $expectedUserName")
        println("Actual userName: ${viewModel.userName.value}")
        println("Expected userEmail: $expectedUserEmail")
        println("Actual userEmail: ${viewModel.userEmail.value}")

        // Assert
        assert(viewModel.userName.value == expectedUserName)
        assert(viewModel.userEmail.value == expectedUserEmail)
    }

    @Test
    fun `loadUserProfile should fetch notifications setting from Firestore`() = runBlockingTest {
        // Arrange
        whenever(mockAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn("123")

        // Mock Firestore collection and document
        val mockCollection = mock(CollectionReference::class.java)
        val mockDocument = mock(DocumentReference::class.java)
        whenever(mockFirestore.collection("users")).thenReturn(mockCollection)
        whenever(mockCollection.document("123")).thenReturn(mockDocument)

        // Mock Task<DocumentSnapshot>
        val mockTask = mock(Task::class.java) as Task<DocumentSnapshot>
        whenever(mockDocument.get()).thenReturn(mockTask)
        // Act
        viewModel.loadUserProfile()

        // Print expected and actual values for debugging
        val expectedNotificationsEnabled = false
        val actualNotificationsEnabled = viewModel.notificationsEnabled.value
        println("Expected notificationsEnabled: $expectedNotificationsEnabled")
        println("Actual notificationsEnabled: $actualNotificationsEnabled")

        // Assert
        assert(actualNotificationsEnabled == expectedNotificationsEnabled)
    }


    @Test
    fun `signOut should set isSignedOut to true and call authUI signOut`() = runBlocking {
        // Arrange
        val mockTask: Task<Void> = Tasks.forResult(null) // Create a successful Task
        `when`(mockAuthUI.signOut(mockContext)).thenReturn(mockTask) // Mock the signOut method to return the successful Task

        // Act
        viewModel.signOut(mockContext)

        // Assert
        verify(mockAuthUI, times(1)).signOut(mockContext) // Verify signOut was called
        assertTrue(viewModel.isSignedOut.first()) // Verify _isSignedOut was set to true
    }
}