package com.example.eventorias.ui.login

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.example.eventorias.R

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val user by viewModel.user.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Handle sign-in result
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleSignInResult(result.resultCode, result.data)
    }

    // Start the sign-in flow
    LaunchedEffect(Unit) {
        if (user == null) {
            startSignInFlow(signInLauncher)
        }
    }

    // Navigate to the events screen if the user is logged in
    LaunchedEffect(user) {
        user?.let {
            navController.navigate("events") {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
            }
        }
    }

    // Show error message if any
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }
}

private fun startSignInFlow(signInLauncher: ActivityResultLauncher<Intent>) {
    val providers = listOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build()
    )

    val authUiLayout = AuthMethodPickerLayout
        .Builder(R.layout.my_custom_auth_ui)
        .setGoogleButtonId(R.id.GoogleButton)
        .setEmailButtonId(R.id.emailButton)
        .build()

    val signInIntent = AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(providers)
        .setAuthMethodPickerLayout(authUiLayout)
        .setLogo(R.drawable.logo)
        .setTheme(R.style.FirebaseAuthCustomTheme)
        .build()

    signInLauncher.launch(signInIntent)
}