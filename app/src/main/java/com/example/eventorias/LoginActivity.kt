package com.example.eventorias

import android.os.Bundle
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
            .build()

        signInLauncher.launch(signInIntent)
    }

    private fun navigateToHome(user: FirebaseUser) {
        // Navigate to home screen using Compose
        setContent {
            HomeScreen(user = user)
        }
    }
}

@Composable
fun HomeScreen(user: FirebaseUser) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Hi ${user.displayName ?: "User"}")
        Button(onClick = {
            FirebaseAuth.getInstance().signOut()
        }) {
            Text(text = "Sign Out")
        }
    }
}
