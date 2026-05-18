package com.babyvault.android.presentation.screens.splash
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.babyvault.android.presentation.theme.Teal400
import com.babyvault.android.presentation.theme.Teal600
@Composable
fun SplashScreen(
    onVaultReady: () -> Unit,
    onNeedUnlock: () -> Unit,
    onNeedProfile: () -> Unit = {},
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state) {
        when (state) {
            SplashState.Ready -> onVaultReady()
            SplashState.NeedUnlock -> onNeedUnlock()
            SplashState.NeedProfile -> onNeedProfile()
            SplashState.Loading -> Unit
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Teal600, Teal400))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(Icons.Filled.ChildCare, contentDescription = null, tint = Color.White, modifier = Modifier.size(80.dp))
            Text(
                "Baby Vault",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
        }
    }
}
