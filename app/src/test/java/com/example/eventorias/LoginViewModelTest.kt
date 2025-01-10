package com.example.eventorias

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.eventorias.ui.login.LoginViewModel
import com.firebase.ui.auth.FirebaseUiException
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class LoginViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var firebaseAuth: FirebaseAuth

    @Mock
    private lateinit var firebaseFirestore: FirebaseFirestore

    @Mock
    private lateinit var mockIntent: Intent

    @Mock
    private lateinit var firebaseUser: FirebaseUser


    private lateinit var loginViewModel: LoginViewModel

    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        loginViewModel = LoginViewModel(firebaseAuth, firebaseFirestore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `checkCurrentUser updates user state when user is logged in`() {
        `when`(firebaseAuth.currentUser).thenReturn(firebaseUser)
        loginViewModel.checkCurrentUser()
        assert(loginViewModel.user.value == firebaseUser)
    }

    @Test
    fun `checkCurrentUser updates user state to null when user is not logged in`() {
        `when`(firebaseAuth.currentUser).thenReturn(null)
        loginViewModel.checkCurrentUser()
        assert(loginViewModel.user.value == null)
    }

    @Test
    fun `handleSignInResult updates user state when result is RESULT_OK`() {
        // Arrange
        val intent = mock(Intent::class.java)
        `when`(firebaseAuth.currentUser).thenReturn(firebaseUser)
        // Mock FirebaseMessaging and its token method
        val task = mock(Task::class.java) as Task<String>
//        `when`(task.isSuccessful).thenReturn(true)
        // Act
        loginViewModel.handleSignInResult(RESULT_OK, intent)
        // Assert
        assert(loginViewModel.user.value == firebaseUser)
    }

    @Test
    fun `handleSignInResult with RESULT_CANCELED and null response should update default error message`() = runTest {
        // Arrange
        `when`(mockIntent.getParcelableExtra<IdpResponse>(any())).thenReturn(null)

        // Act
        loginViewModel.handleSignInResult(RESULT_CANCELED, mockIntent)

        // Assert
        assertEquals("Sign-in failed", loginViewModel.errorMessage.first())
    }

}