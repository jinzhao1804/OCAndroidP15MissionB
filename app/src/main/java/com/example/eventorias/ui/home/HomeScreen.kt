package com.example.eventorias.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.eventorias.R
import com.example.eventorias.ui.theme.dark
import com.google.firebase.auth.FirebaseUser
import com.example.eventorias.ui.add.CreateEventActivity
import com.example.eventorias.ui.list.EventListScreen
import com.example.eventorias.ui.profile.UserProfileScreen

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(navController: NavController, user: FirebaseUser, onSignOut: () -> Unit) {
    val navController = rememberNavController()
    val context = LocalContext.current

    Scaffold(
        topBar = {/*
            TopAppBar(
                title = { Text(text = "Eventorias") }  // Dynamic title can be added if needed
            )*/
        },
        bottomBar = {
            Box (modifier = Modifier
                .fillMaxWidth()
                .background(dark)
                .wrapContentSize(Alignment.Center),
                contentAlignment = Alignment.Center
            ){
                Box(
                    modifier = Modifier
                        .width(200.dp)
                    ,
                    contentAlignment = Alignment.Center
                ) {
                    BottomNavigationBar(navController) // Put the nav bar inside the Box to center it
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(navController.context, CreateEventActivity::class.java)
                    navController.context.startActivity(intent)
                },
                content = { Text("+") }
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        content = {
            NavHost(navController = navController, startDestination = "events") {
                composable("events") {
                   // EventListScreen(user, onSignOut)
                    EventListScreen()
                }
                composable("profile") {
                    //ProfileScreen(user)
                    UserProfileScreen(context = context)
                }
                composable("createEvent") {
                    CreateEventActivity().CreateEventScreen()
                }
            }
        }
    )
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    NavigationBar(
        modifier = Modifier.background(color = dark),
        containerColor = dark
    ) {
        NavigationBarItem(
            selected = selectedTabIndex == 0,
            onClick = {
                selectedTabIndex = 0
                navController.navigate("events") {
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = {
                Icon(painter = painterResource(id = R.drawable.list), contentDescription = "Events")
            },
            label = { Text("Events") },
            modifier = Modifier
                .background(dark)
                .padding(horizontal = 0.dp), // Change background color when selected
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = Color.White,  // White color for unselected icons
                selectedIconColor = Color.White,    // White color for selected icons
                unselectedTextColor = Color.White,  // White color for unselected text
                selectedTextColor = Color.White     // White color for selected text
            )
        )

        NavigationBarItem(
            selected = selectedTabIndex == 1,
            onClick = {
                selectedTabIndex = 1
                navController.navigate("profile") {
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = {
                Icon(painter = painterResource(id = R.drawable.profile), contentDescription = "Profile")
            },
            label = { Text("Profile") },
            modifier = Modifier
                .background(dark)
                .padding(horizontal = 0.dp),
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = Color.White,  // White color for unselected icons
                selectedIconColor = Color.White,    // White color for selected icons
                unselectedTextColor = Color.White,  // White color for unselected text
                selectedTextColor = Color.White     // White color for selected text
            )
        )
    }
}