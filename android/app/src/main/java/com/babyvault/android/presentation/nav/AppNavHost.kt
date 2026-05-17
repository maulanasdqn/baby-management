package com.babyvault.android.presentation.nav
import android.app.Activity
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.babyvault.android.presentation.screens.chat.ChatScreen
import com.babyvault.android.presentation.screens.chat.ModelSetupScreen
import com.babyvault.android.presentation.screens.growth.GrowthScreen
import com.babyvault.android.presentation.screens.history.HistoryScreen
import com.babyvault.android.presentation.screens.home.HomeScreen
import com.babyvault.android.presentation.screens.insights.InsightsScreen
import com.babyvault.android.presentation.screens.log.FeedTimerScreen
import com.babyvault.android.presentation.screens.log.LogDiaperScreen
import com.babyvault.android.presentation.screens.log.LogFeedScreen
import com.babyvault.android.presentation.screens.log.LogSleepScreen
import com.babyvault.android.presentation.screens.media.MediaScreen
import com.babyvault.android.presentation.screens.profile.ProfileSetupScreen
import com.babyvault.android.presentation.screens.profile.ProfileScreen
import com.babyvault.android.presentation.screens.settings.SettingsScreen
import com.babyvault.android.presentation.screens.settings.SyncSettingsScreen
import com.babyvault.android.presentation.screens.splash.SplashScreen
import com.babyvault.android.presentation.screens.timeline.TimelineScreen
import com.babyvault.android.presentation.screens.unlock.UnlockScreen
import com.babyvault.android.presentation.ui.BottomNavBar
private val bottomNavRoutes = setOf(
    Routes.HOME,
    Routes.HISTORY,
    Routes.INSIGHTS,
    Routes.CHAT,
    Routes.SETTINGS,
)
private val lightStatusBarRoutes = setOf(
    Routes.HOME,
    Routes.HISTORY,
    Routes.INSIGHTS,
    Routes.SETTINGS,
    Routes.PROFILE,
    Routes.SYNC_SETTINGS,
    Routes.MEDIA,
    Routes.LOG_FEED,
    Routes.LOG_SLEEP,
    Routes.LOG_DIAPER,
    Routes.LOG_MILESTONE,
    Routes.LOG_GROWTH,
    Routes.FEED_TIMER,
    Routes.CHAT,
    Routes.MODEL_SETUP,
)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.SPLASH
    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars =
            currentRoute in lightStatusBarRoutes
    }
    Scaffold(
        contentWindowInsets = WindowInsets(0),
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
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
            enterTransition = { slideInHorizontally(tween(280)) { it / 4 } + fadeIn(tween(280)) },
            exitTransition = { slideOutHorizontally(tween(280)) { -it / 4 } + fadeOut(tween(180)) },
            popEnterTransition = { slideInHorizontally(tween(280)) { -it / 4 } + fadeIn(tween(280)) },
            popExitTransition = { slideOutHorizontally(tween(280)) { it / 4 } + fadeOut(tween(180)) },
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
                    onNeedProfile = {
                        navController.navigate(Routes.PROFILE_SETUP) {
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
                    onNeedProfile = {
                        navController.navigate(Routes.PROFILE_SETUP) {
                            popUpTo(Routes.UNLOCK) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.PROFILE_SETUP) {
                ProfileSetupScreen(
                    onDone = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.PROFILE_SETUP) { inclusive = true }
                        }
                    },
                )
            }
            composable(
                Routes.HOME,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(200)) },
                popExitTransition = { fadeOut(tween(200)) },
            ) {
                HomeScreen(onNavigate = { route -> navController.navigate(route) })
            }
            composable(
                Routes.HISTORY,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(200)) },
                popExitTransition = { fadeOut(tween(200)) },
            ) {
                HistoryScreen()
            }
            composable(
                Routes.INSIGHTS,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(200)) },
                popExitTransition = { fadeOut(tween(200)) },
            ) {
                InsightsScreen()
            }
            composable(
                Routes.SETTINGS,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(200)) },
                popExitTransition = { fadeOut(tween(200)) },
            ) {
                SettingsScreen(
                    onNavigateProfile = { navController.navigate(Routes.PROFILE) },
                    onNavigateSync = { navController.navigate(Routes.SYNC_SETTINGS) },
                    onNavigateModelSetup = { navController.navigate(Routes.MODEL_SETUP) },
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SYNC_SETTINGS) {
                SyncSettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.MEDIA) {
                MediaScreen()
            }
            composable(Routes.LOG_FEED) {
                LogFeedScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.FEED_TIMER) {
                FeedTimerScreen(onBack = { navController.popBackStack() })
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
            composable(
                Routes.CHAT,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(200)) },
                popExitTransition = { fadeOut(tween(200)) },
            ) {
                ChatScreen(onSetupModel = { navController.navigate(Routes.MODEL_SETUP) })
            }
            composable(Routes.MODEL_SETUP) {
                ModelSetupScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
