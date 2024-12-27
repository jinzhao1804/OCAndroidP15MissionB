package com.example.eventorias

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import androidx.activity.result.contract.ActivityResultContracts

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

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.logo)  // Replace with your logo's resource ID
            .setTheme(R.style.FirebaseAuthCustomTheme)  // Apply the custom theme for centering
            .build()

        signInLauncher.launch(signInIntent)
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
        Button(onClick = onSignOut) {
            Text(text = "Sign Out")
        }
    }
}
