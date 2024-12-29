package com.example.eventorias.ui.login

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.eventorias.R
import com.example.eventorias.ui.home.HomeScreen
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private val navController = NavController

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                navigateToHome(user)
            }
        } else {
            // Handle error
            val response = IdpResponse.fromResultIntent(result.data)
            response?.error?.message?.let {
                // Display a user-friendly error message
                Toast.makeText(this, "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                navigateToHome(user)
                getNewFCMToken()
            } else {
                startSignInFlow()
            }
        }


    }

    private fun startSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create AuthMethodPickerLayout with your custom layout
        val authUiLayout = AuthMethodPickerLayout
            .Builder(R.layout.my_custom_auth_ui) // Custom layout you created in XML
            .setGoogleButtonId(R.id.GoogleButton)
            .setEmailButtonId(R.id.emailButton)
            .build()

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setAuthMethodPickerLayout(authUiLayout)
            .setLogo(R.drawable.logo)  // Optional: Set a logo
            .setTheme(R.style.FirebaseAuthCustomTheme)  // Optional: Apply custom theme
            .build()

        signInLauncher.launch(signInIntent)
    }

    private fun navigateToHome(user: FirebaseUser) {
        setContent {
            val navController = rememberNavController() // Create the NavController inside Composable

            // Pass user and navController to HomeScreen composable
            HomeScreen(navController = navController, user = user, onSignOut = { signOut() })
        }
    }


    private fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
        startSignInFlow()
    }

    private fun getNewFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                // Handle failure to get token
                return@addOnCompleteListener
            }

            // Get the new FCM token
            val newToken = task.result
            Log.d("FCM", "New token: $newToken")
            // Save the token to Firestore or your backend
            saveTokenToFirestore(newToken)
        }
    }

    private fun saveTokenToFirestore(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

        userRef.update("fcmToken", token)
            .addOnSuccessListener {
                Log.d("FCM", "Token saved successfully")
            }
            .addOnFailureListener { e ->
                Log.w("FCM", "Error saving token", e)
            }
    }
}


