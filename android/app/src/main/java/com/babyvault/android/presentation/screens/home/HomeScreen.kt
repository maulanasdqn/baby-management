package com.babyvault.android.presentation.screens.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.babyvault.android.presentation.nav.Routes
import com.babyvault.android.presentation.screens.growth.GrowthScreen
import com.babyvault.android.presentation.screens.media.MediaScreen
import com.babyvault.android.presentation.screens.timeline.TimelineScreen
import com.babyvault.android.presentation.ui.BottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigateToSettings: () -> Unit) {
    val tabNavController = rememberNavController()
    val backStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val title = when (currentRoute) {
        Routes.Tab.GROWTH -> "Growth Tracker"
        Routes.Tab.MEDIA -> "Media Vault"
        else -> "Timeline"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Baby Vault — $title") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Sync Settings")
                    }
                },
            )
        },
        bottomBar = { BottomNavBar(navController = tabNavController) },
    ) { padding ->
        NavHost(
            navController = tabNavController,
            startDestination = Routes.Tab.TIMELINE,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.Tab.TIMELINE) { TimelineScreen() }
            composable(Routes.Tab.GROWTH) { GrowthScreen() }
            composable(Routes.Tab.MEDIA) { MediaScreen() }
        }
    }
}
