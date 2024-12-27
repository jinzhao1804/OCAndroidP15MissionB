package com.example.eventorias

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button as ComposeButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.firebase.ui.auth.AuthMethodPickerLayout

class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                // Successfully signed in
                navigateToHome(user)
            }
        } else {
            val response = IdpResponse.fromResultIntent(result.data)
            // Handle error if necessary
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
        customizeButtons()
    }

    // After FirebaseUI is launched, customize buttons programmatically
    private fun customizeButtons() {
        // FirebaseUI handles sign-in flow and launches another activity.
        // You need to wait until FirebaseUI activity has been launched, which will contain the buttons you want to customize.

        val googleButton: Button? = findViewById(R.id.GoogleButton)
        val emailButton: Button? = findViewById(R.id.emailButton)

        // Check if buttons are found and then set their background color
        googleButton?.let {
            it.setBackgroundColor(ContextCompat.getColor(this, R.color.white)) // Set red background for Google button
        }

        emailButton?.let {
            it.setBackgroundColor(ContextCompat.getColor(this, R.color.red)) // Set white background for email button
        }
    }

    private fun navigateToHome(user: FirebaseUser) {
        setContent {
            HomeScreen(user = user, onSignOut = { signOut() })
        }
    }

    private fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
        startSignInFlow()
    }
}

@Composable
fun HomeScreen(user: FirebaseUser, onSignOut: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Hi ${user.displayName ?: "User"}")
        ComposeButton(onClick = onSignOut) {
            Text(text = "Sign Out")
        }
    }
}
