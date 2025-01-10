package com.example.eventorias.ui.login

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel (
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
): ViewModel() {


    // State to track the current user
    private val _user = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> get() = _user

    // State to track login errors
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    init {
        checkCurrentUser()
    }

    // Check if the user is already logged in
    fun checkCurrentUser() {
        _user.value = auth.currentUser
    }

    fun handleSignInResult(resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            _user.value = auth.currentUser
            //getNewFCMToken()
        } else {
            val response = IdpResponse.fromResultIntent(data)
            println("Response error: ${response?.error}")
            println("Response error message: ${response?.error?.message}")
            _errorMessage.value = response?.error?.message ?: "Sign-in failed"
            println("Updated error message: ${_errorMessage.value}")
        }
    }

    // Get a new FCM token and save it to Firestore
    fun getNewFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val newToken = task.result
                Log.d("FCM", "New token: $newToken")
                saveTokenToFirestore(newToken)
            } else {
                Log.w("FCM", "Failed to get token", task.exception)
            }
        }
    }

    // Save the FCM token to Firestore
    fun saveTokenToFirestore(token: String) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(userId)

        userRef.update("fcmToken", token)
            .addOnSuccessListener {
                Log.d("FCM", "Token saved successfully")
            }
            .addOnFailureListener { e ->
                Log.w("FCM", "Error saving token", e)
            }
    }
}