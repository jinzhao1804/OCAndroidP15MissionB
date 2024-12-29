package com.example.eventorias.ui.profile

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.eventorias.MyFirebaseMessagingService
import com.example.eventorias.R
import com.example.eventorias.data.User
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

@Composable
fun UserProfileScreen(context: Context) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    var userName by remember { mutableStateOf("John Doe") }
    var userEmail by remember { mutableStateOf("john.doe@example.com") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var avatarImage: Painter? by remember { mutableStateOf(null) }

    avatarImage = painterResource(id = R.drawable.profile)  // Update state with the image resource

    // Set profile title and update user information if authenticated
    LaunchedEffect(auth.currentUser) {
        auth.currentUser?.let { user ->
            userName = user.displayName ?: "John Doe"
            userEmail = user.email ?: "john.doe@example.com"

            // Set the avatar image once the user is authenticated

            // Initialize switch state from Firestore
            val userDocRef = firestore.collection("users").document(user.uid)
            userDocRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        notificationsEnabled = document.getBoolean("receive_notifications") ?: true
                    } else {
                        // Create new user document if none exists
                        val newUser = User(receive_notifications = true)
                        userDocRef.set(newUser).addOnSuccessListener {
                            notificationsEnabled = newUser.receive_notifications
                        }.addOnFailureListener { e ->
                            Log.e("UserProfileScreen", "Error creating user document", e)
                        }
                    }
                } else {
                    Log.e("UserProfileScreen", "Error fetching user data", task.exception)
                }
            }
        }
    }

    // User Profile UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Profile", style = MaterialTheme.typography.headlineMedium)

        // Use the avatar image if it's set
        avatarImage?.let {
            Image(painter = it, contentDescription = "User Avatar", modifier = Modifier.size(100.dp))
        } ?: run {
            // Fallback if avatarImage is still null
            Image(painter = painterResource(id = R.drawable.profile), contentDescription = "Default Avatar", modifier = Modifier.size(100.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = userName, style = MaterialTheme.typography.bodyLarge)
        Text(text = userEmail, style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Switch(
            checked = notificationsEnabled,
            onCheckedChange = { isChecked ->
                val userDocRef = firestore.collection("users").document(auth.currentUser?.uid ?: "")
                userDocRef.update("receive_notifications", isChecked)
                    .addOnSuccessListener {
                        notificationsEnabled = isChecked
                        if(isChecked == true){
                            FirebaseMessaging.getInstance().subscribeToTopic("all")
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        println("Successfully subscribed to 'all' topic.")
                                    } else {
                                        println("Failed to subscribe to 'all' topic.")
                                    }
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("UserProfileScreen", "Error updating notifications setting", e)
                    }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { signOut(context) }) {
            Text(text = "Logout")
        }
    }
}

fun signOut(context: Context) {
    AuthUI.getInstance()
        .signOut(context) // Pass context for sign-out
        .addOnCompleteListener {
            // Show sign-out confirmation toast
            Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
        }
}
