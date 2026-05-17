package com.babyvault.android.presentation.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

sealed class BottomTab(val route: String, val label: String) {
    object Home : BottomTab("home", "Home")
    object History : BottomTab("history", "History")
    object Insights : BottomTab("insights", "Insights")
    object Settings : BottomTab("settings", "Settings")
}

@Composable
fun BottomNavBar(currentRoute: String, onNavigate: (String) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == BottomTab.Home.route,
            onClick = { onNavigate(BottomTab.Home.route) },
            icon = { Icon(Icons.Filled.Home, null) },
            label = { Text("Home") },
        )
        NavigationBarItem(
            selected = currentRoute == BottomTab.History.route,
            onClick = { onNavigate(BottomTab.History.route) },
            icon = { Icon(Icons.Filled.History, null) },
            label = { Text("History") },
        )
        NavigationBarItem(
            selected = currentRoute == BottomTab.Insights.route,
            onClick = { onNavigate(BottomTab.Insights.route) },
            icon = { Icon(Icons.Filled.BarChart, null) },
            label = { Text("Insights") },
        )
        NavigationBarItem(
            selected = currentRoute == BottomTab.Settings.route,
            onClick = { onNavigate(BottomTab.Settings.route) },
            icon = { Icon(Icons.Filled.Settings, null) },
            label = { Text("Settings") },
        )
    }
}
