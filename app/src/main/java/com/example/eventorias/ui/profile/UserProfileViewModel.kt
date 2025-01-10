package com.example.eventorias.ui.profile

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventorias.data.User
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Use MutableStateFlow for state management
    private val _userName = MutableStateFlow("John Doe")
    val userName: StateFlow<String> get() = _userName

    private val _userEmail = MutableStateFlow("john.doe@example.com")
    val userEmail: StateFlow<String> get() = _userEmail

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> get() = _notificationsEnabled


    private val _isSignedOut = MutableStateFlow(false)
    val isSignedOut: StateFlow<Boolean> get() = _isSignedOut

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        auth.currentUser?.let { user ->
            _userName.value = user.displayName ?: "John Doe"
            _userEmail.value = user.email ?: "john.doe@example.com"

            // Fetch user settings from Firestore
            val userDocRef = firestore.collection("users").document(user.uid)
            userDocRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        _notificationsEnabled.value = document.getBoolean("receive_notifications") ?: true
                    } else {
                        // Create new user document if none exists
                        val newUser = User(receive_notifications = true)
                        userDocRef.set(newUser).addOnSuccessListener {
                            _notificationsEnabled.value = newUser.receive_notifications
                        }.addOnFailureListener { e ->
                            Log.e("UserProfileViewModel", "Error creating user document", e)
                        }
                    }
                } else {
                    Log.e("UserProfileViewModel", "Error fetching user data", task.exception)
                }
            }
        }
    }

    fun updateNotificationsSetting(isChecked: Boolean) {
        viewModelScope.launch {
            val userDocRef = firestore.collection("users").document(auth.currentUser?.uid ?: "")
            userDocRef.update("receive_notifications", isChecked)
                .addOnSuccessListener {
                    _notificationsEnabled.value = isChecked
                    if (isChecked) {
                        FirebaseMessaging.getInstance().subscribeToTopic("all")
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("UserProfileViewModel", "Successfully subscribed to 'all' topic.")
                                } else {
                                    Log.e("UserProfileViewModel", "Failed to subscribe to 'all' topic.")
                                }
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("UserProfileViewModel", "Error updating notifications setting", e)
                }
        }
    }

    fun signOut(context: Context) {
        AuthUI.getInstance()
            .signOut(context)
            .addOnCompleteListener {
                _isSignedOut.value = true
                Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
            }
    }
}