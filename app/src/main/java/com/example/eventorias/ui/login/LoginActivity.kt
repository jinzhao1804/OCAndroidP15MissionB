package com.example.eventorias.ui.login

import android.os.Bundle
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
import com.example.eventorias.ui.eventList.HomeScreen
import com.firebase.ui.auth.AuthMethodPickerLayout

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
}

