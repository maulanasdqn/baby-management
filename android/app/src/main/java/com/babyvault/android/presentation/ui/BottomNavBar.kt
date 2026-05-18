package com.babyvault.android.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.ChildFriendly
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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

private data class NavItem(
    val tab: BottomTab,
    val icon: ImageVector,
)

private val navItems = listOf(
    NavItem(BottomTab.Home,     Icons.Filled.ChildFriendly),
    NavItem(BottomTab.History,  Icons.AutoMirrored.Filled.EventNote),
    NavItem(BottomTab.Insights, Icons.Filled.AutoGraph),
    NavItem(BottomTab.Chat,     Icons.Filled.SmartToy),
    NavItem(BottomTab.Settings, Icons.Filled.Tune),
)

@Composable
fun BottomNavBar(currentRoute: String, onNavigate: (String) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = CardWhite,
            shadowElevation = 20.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                navItems.forEach { item ->
                    FloatingNavItem(
                        icon = item.icon,
                        label = item.tab.label,
                        selected = currentRoute == item.tab.route,
                        onClick = { onNavigate(item.tab.route) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (selected) Lavender100 else Color.Transparent)
            .clickable(onClick = onClick),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) NavyPrimary else TextSecondary,
            modifier = Modifier.size(22.dp),
        )
    }
}
