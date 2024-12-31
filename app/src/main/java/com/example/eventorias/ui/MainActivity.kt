package com.example.eventorias.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.eventorias.ui.home.HomeScreen
import com.example.eventorias.ui.theme.EventoriasTheme
import com.example.eventorias.ui.add.CreateEventActivity
import com.example.eventorias.ui.detail.EventDetailScreen
import com.firebase.ui.auth.AuthUI

class MainActivity : ComponentActivity() {
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EventoriasTheme {
                // A surface container using the 'background' color from the theme
                Surface {
                    // Setup the Navigation Host and NavController
                    val navController = rememberNavController()
                    val user = AuthUI.getInstance().auth.currentUser

                    // Function to sign out the user
                    val onSignOut: () -> Unit = {
                        AuthUI.getInstance().signOut(this)
                    }
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            if (user != null) {
                                HomeScreen(navController,user, onSignOut = onSignOut)
                            }
                        }
                        composable("createEvent") {
                            CreateEventActivity()
                        }
                        composable("detail/{eventId}") { backStackEntry ->
                            // Retrieve the eventId from the back stack entry
                            val eventId = backStackEntry.arguments?.getString("eventId")
                            if (eventId != null) {
                                // Pass the eventId to EventDetailScreen
                                EventDetailScreen(eventId = eventId, navController = navController)
                            }
                        }
                    }
                }
            }
        }
    }
}
