package com.example.eventorias.ui.profile

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eventorias.R
import com.example.eventorias.ui.theme.app_white
import com.example.eventorias.ui.theme.dark
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.eventorias.ui.theme.grey

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UserProfileScreen(context: Context, viewModel: UserProfileViewModel = viewModel(), navController: NavController) {
    // Collect state from ViewModel
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val avatarImage = painterResource(id = R.drawable.profile1)

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                // For devices below Android 13, no need to request this permission
                true
            }
        )
    }

    // Launcher for requesting permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    Scaffold(
        containerColor = dark,
        contentColor = app_white
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Profile Header
            ProfileHeader(avatarImage = avatarImage)

            Spacer(modifier = Modifier.height(16.dp))

            // Name TextField
            ProfileTextField(label = "Name", value = userName)

            Spacer(modifier = Modifier.padding(8.dp))

            // Email TextField
            ProfileTextField(label = "Email", value = userEmail)

            Spacer(modifier = Modifier.height(16.dp))

            // Notifications Switch
            NotificationsSwitch(
                notificationsEnabled = notificationsEnabled,
                onCheckedChange = { isChecked ->
                    viewModel.updateNotificationsSetting(isChecked)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Button
            LogoutButton(onClick = { viewModel.signOut(context) },
                navController)
        }
    }
}

@Composable
fun ProfileHeader(avatarImage: Painter) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "User profile",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.weight(1f)
        )

        Image(
            painter = avatarImage,
            contentDescription = "User Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
        )
    }
}
@Composable
fun ProfileTextField(label: String, value: String) {
    TextField(
        value = value,
        onValueChange = {},
        label = { Text(label, color = app_white) },
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            disabledTextColor = app_white,
            disabledContainerColor = grey,
            disabledIndicatorColor = grey,
            disabledLabelColor = grey
        )
    )
}
@Composable
fun NotificationsSwitch(
    notificationsEnabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        checked = notificationsEnabled,
        onCheckedChange = onCheckedChange
    )
}

@Composable
fun LogoutButton(
    onClick: () -> Unit, // Handles the logout logic
    navController: NavController // Handles navigation
) {
    Button(onClick = {
        Log.d("LogoutButton", "Logout button clicked")
        onClick() // Perform logout
        Log.d("LogoutButton", "Navigating to login screen")
        navController.navigate("login") {
            // Clear the back stack up to and including the start destination
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = true
            }
        }
    }) {
        Text(text = "Logout")
    }
}