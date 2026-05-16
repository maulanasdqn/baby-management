package com.babyvault.android.presentation.screens.unlock

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun UnlockScreen(
    onUnlocked: () -> Unit,
    viewModel: UnlockViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state) {
        if (state is UnlockState.Unlocked) onUnlocked()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Unlock Baby Vault", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        if (state is UnlockState.Error) {
            Text(
                (state as UnlockState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }

        Button(
            onClick = {
                val activity = context as FragmentActivity
                val executor = ContextCompat.getMainExecutor(context)
                val callback = object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        viewModel.onBiometricSuccess()
                    }
                }
                BiometricPrompt(activity, executor, callback).authenticate(
                    BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Unlock Baby Vault")
                        .setSubtitle("Use biometric authentication")
                        .setNegativeButtonText("Cancel")
                        .build(),
                )
            },
            enabled = state !is UnlockState.Unlocking,
        ) {
            if (state is UnlockState.Unlocking) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Authenticate")
            }
        }
    }
}
