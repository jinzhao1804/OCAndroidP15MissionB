package com.example.eventorias.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.eventorias.R
import com.example.eventorias.ui.theme.dark
import com.example.eventorias.ui.theme.EventoriasTheme
import com.example.eventorias.ui.add.CreateEventScreen
import com.example.eventorias.ui.add.CreateEventViewModel
import com.example.eventorias.ui.detail.EventDetailScreen
import com.example.eventorias.ui.list.EventListScreen
import com.example.eventorias.ui.login.LoginScreen
import com.example.eventorias.ui.login.LoginViewModel
import com.example.eventorias.ui.profile.UserProfileScreen
import com.example.eventorias.ui.profile.UserProfileViewModel

class MainActivity : ComponentActivity() {
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EventoriasTheme(true, false) {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Setup the Navigation Host and NavController
                    val navController = rememberNavController()
                    val context = LocalContext.current
                    // Current destination for FAB visibility
                    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

                    Scaffold(
                        topBar = {
                            // TopBar content (if any)
                        },
                        bottomBar = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(dark)
                                    .wrapContentSize(Alignment.Center),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    BottomNavigationBar(navController) // Put the nav bar inside the Box to center it
                                }
                            }
                        },
                        floatingActionButton = {
                            // Show FAB only on the EventListScreen
                            if (currentDestination == "events") {
                                FloatingActionButton(
                                    onClick = {
                                        // Navigate to the CreateEventScreen route
                                        navController.navigate("createEvent")
                                    },
                                    containerColor = colorResource(id = R.color.red)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Event",
                                        tint = colorResource(id = R.color.app_white)
                                    )
                                }
                            }
                        },
                        floatingActionButtonPosition = FabPosition.End,

                        content = { paddingValues ->
                            NavHost(
                                navController = navController,
                                startDestination = "login",
                                modifier = Modifier.padding(paddingValues)
                            ) {
                                composable("events") {
                                    EventListScreen(navController)
                                }

                                composable("profile") {
                                    UserProfileScreen(
                                        context = LocalContext.current,
                                        viewModel = UserProfileViewModel(
                                        ),
                                        navController = navController
                                    )
                                }
                                composable("createEvent") {
                                    CreateEventScreen(
                                        navController,
                                        onBackPressed = { navController.popBackStack() }
                                    )
                                }
                                // Define the EventDetailScreen route with an eventId argument
                                composable(
                                    route = "detail/{eventId}", // Define the route with a parameter
                                    arguments = listOf(navArgument("eventId") { type = NavType.StringType })
                                ) { backStackEntry ->
                                    // Extract the eventId from the route
                                    val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                                    EventDetailScreen(
                                        eventId = eventId,
                                        onBackPressed = { navController.popBackStack() }
                                    )
                                }
                                composable("login") {
                                    LoginScreen(
                                        navController,
                                        LoginViewModel()
                                    ) // Your SignIn screen composable
                                }
                            }
                        }
                    )
                }
            }
        }
    }
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