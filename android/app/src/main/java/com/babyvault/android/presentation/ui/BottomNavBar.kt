package com.babyvault.android.presentation.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.babyvault.android.presentation.nav.Routes

@Composable
fun BottomNavBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.EventNote, contentDescription = null) },
            label = { Text("Timeline") },
            selected = currentRoute == Routes.Tab.TIMELINE,
            onClick = {
                navController.navigate(Routes.Tab.TIMELINE) {
                    launchSingleTop = true
                    restoreState = true
                }
            },
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.ShowChart, contentDescription = null) },
            label = { Text("Growth") },
            selected = currentRoute == Routes.Tab.GROWTH,
            onClick = {
                navController.navigate(Routes.Tab.GROWTH) {
                    launchSingleTop = true
                    restoreState = true
                }
            },
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Photo, contentDescription = null) },
            label = { Text("Media") },
            selected = currentRoute == Routes.Tab.MEDIA,
            onClick = {
                navController.navigate(Routes.Tab.MEDIA) {
                    launchSingleTop = true
                    restoreState = true
                }
            },
        )
    }
}
