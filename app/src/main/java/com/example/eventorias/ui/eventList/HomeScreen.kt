package com.example.eventorias.ui.eventList

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseUser

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(user: FirebaseUser, onSignOut: () -> Unit) {
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Eventorias") }  // Dynamic title can be added if needed
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Handle click here */ },
                content = { Text("+") }
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        content = {
            NavHost(navController = navController, startDestination = "events") {
                composable("events") {
                    EventListScreen(user, onSignOut)
                }
                composable("profile") {
                    ProfileScreen(user)
                }
            }
        }
    )
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    NavigationBar {
        NavigationBarItem(
            selected = selectedTabIndex == 0,
            onClick = {
                selectedTabIndex = 0
                navController.navigate("events") {
                    // Avoid multiple backstack entries for the same screen
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = {
                Icon(Icons.Filled.List, contentDescription = "Events")
            },
            label = { Text("Events") }
        )
        NavigationBarItem(
            selected = selectedTabIndex == 1,
            onClick = {
                selectedTabIndex = 1
                navController.navigate("profile") {
                    // Avoid multiple backstack entries for the same screen
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = {
                Icon(Icons.Filled.Person, contentDescription = "Profile")
            },
            label = { Text("Profile") }
        )
    }
}

@Composable
fun EventListScreen(user: FirebaseUser, onSignOut: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Hi ${user.displayName ?: "User"}")
        Spacer(modifier = Modifier.height(8.dp)) // Adding space between Text and Button
        Button(onClick = onSignOut) {
            Text(text = "Sign Out")
        }
        // Add Event List content here
    }
}

@Composable
fun ProfileScreen(user: FirebaseUser) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Profile of ${user.displayName ?: "User"}")
        // Add Profile content here
    }
}
