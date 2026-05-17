package com.babyvault.android.presentation.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.ChildFriendly
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.babyvault.android.presentation.theme.CardWhite
import com.babyvault.android.presentation.theme.Lavender100
import com.babyvault.android.presentation.theme.NavyPrimary
import com.babyvault.android.presentation.theme.TextSecondary

sealed class BottomTab(val route: String, val label: String) {
    object Home     : BottomTab("home",     "Home")
    object History  : BottomTab("history",  "History")
    object Insights : BottomTab("insights", "Insights")
    object Chat     : BottomTab("chat",     "AI")
    object Settings : BottomTab("settings", "Settings")
}

private val navItemColors @Composable get() = NavigationBarItemDefaults.colors(
    selectedIconColor   = NavyPrimary,
    selectedTextColor   = NavyPrimary,
    indicatorColor      = Lavender100,
    unselectedIconColor = TextSecondary,
    unselectedTextColor = TextSecondary,
)

@Composable
fun BottomNavBar(currentRoute: String, onNavigate: (String) -> Unit) {
    NavigationBar(
        containerColor = CardWhite,
        contentColor   = Color.Unspecified,
    ) {
        NavigationBarItem(
            selected = currentRoute == BottomTab.Home.route,
            onClick  = { onNavigate(BottomTab.Home.route) },
            icon     = { Icon(Icons.Filled.ChildFriendly, null) },
            label    = { Text("Home") },
            colors   = navItemColors,
        )
        NavigationBarItem(
            selected = currentRoute == BottomTab.History.route,
            onClick  = { onNavigate(BottomTab.History.route) },
            icon     = { Icon(Icons.AutoMirrored.Filled.EventNote, null) },
            label    = { Text("History") },
            colors   = navItemColors,
        )
        NavigationBarItem(
            selected = currentRoute == BottomTab.Insights.route,
            onClick  = { onNavigate(BottomTab.Insights.route) },
            icon     = { Icon(Icons.Filled.AutoGraph, null) },
            label    = { Text("Insights") },
            colors   = navItemColors,
        )
        NavigationBarItem(
            selected = currentRoute == BottomTab.Chat.route,
            onClick  = { onNavigate(BottomTab.Chat.route) },
            icon     = { Icon(Icons.Filled.SmartToy, null) },
            label    = { Text("AI") },
            colors   = navItemColors,
        )
        NavigationBarItem(
            selected = currentRoute == BottomTab.Settings.route,
            onClick  = { onNavigate(BottomTab.Settings.route) },
            icon     = { Icon(Icons.Filled.Tune, null) },
            label    = { Text("Settings") },
            colors   = navItemColors,
        )
    }
}
