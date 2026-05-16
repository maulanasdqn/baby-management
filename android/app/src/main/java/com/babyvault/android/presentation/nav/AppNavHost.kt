package com.babyvault.android.presentation.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.babyvault.android.presentation.screens.home.HomeScreen
import com.babyvault.android.presentation.screens.settings.SyncSettingsScreen
import com.babyvault.android.presentation.screens.splash.SplashScreen
import com.babyvault.android.presentation.screens.unlock.UnlockScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onVaultReady = { navController.navigate(Routes.HOME) { popUpTo(Routes.SPLASH) { inclusive = true } } },
                onNeedUnlock = { navController.navigate(Routes.UNLOCK) { popUpTo(Routes.SPLASH) { inclusive = true } } },
            )
        }
        composable(Routes.UNLOCK) {
            UnlockScreen(onUnlocked = { navController.navigate(Routes.HOME) { popUpTo(Routes.UNLOCK) { inclusive = true } } })
        }
        composable(Routes.HOME) {
            HomeScreen(onNavigateToSettings = { navController.navigate(Routes.SETTINGS) })
        }
        composable(Routes.SETTINGS) {
            SyncSettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
