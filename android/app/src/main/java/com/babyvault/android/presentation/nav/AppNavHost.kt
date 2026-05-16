package com.babyvault.android.presentation.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.babyvault.android.presentation.screens.growth.GrowthScreen
import com.babyvault.android.presentation.screens.history.HistoryScreen
import com.babyvault.android.presentation.screens.home.HomeScreen
import com.babyvault.android.presentation.screens.insights.InsightsScreen
import com.babyvault.android.presentation.screens.log.LogDiaperScreen
import com.babyvault.android.presentation.screens.log.LogFeedScreen
import com.babyvault.android.presentation.screens.log.LogSleepScreen
import com.babyvault.android.presentation.screens.media.MediaScreen
import com.babyvault.android.presentation.screens.settings.SyncSettingsScreen
import com.babyvault.android.presentation.screens.splash.SplashScreen
import com.babyvault.android.presentation.screens.timeline.TimelineScreen
import com.babyvault.android.presentation.screens.unlock.UnlockScreen
import com.babyvault.android.presentation.ui.BottomNavBar

private val bottomNavRoutes = setOf(
    Routes.HOME,
    Routes.HISTORY,
    Routes.INSIGHTS,
    Routes.SETTINGS,
)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.HOME

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    onVaultReady = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                    onNeedUnlock = {
                        navController.navigate(Routes.UNLOCK) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.UNLOCK) {
                UnlockScreen(
                    onUnlocked = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.UNLOCK) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.HOME) {
                HomeScreen(onNavigate = { route -> navController.navigate(route) })
            }
            composable(Routes.HISTORY) {
                HistoryScreen()
            }
            composable(Routes.INSIGHTS) {
                InsightsScreen()
            }
            composable(Routes.SETTINGS) {
                SyncSettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.MEDIA) {
                MediaScreen()
            }
            composable(Routes.LOG_FEED) {
                LogFeedScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.LOG_SLEEP) {
                LogSleepScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.LOG_DIAPER) {
                LogDiaperScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.LOG_MILESTONE) {
                TimelineScreen()
            }
            composable(Routes.LOG_GROWTH) {
                GrowthScreen()
            }
        }
    }
}
