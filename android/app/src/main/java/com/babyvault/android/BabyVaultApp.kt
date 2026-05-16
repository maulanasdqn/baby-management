package com.babyvault.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BabyVaultApp : Application() {
    init {
        System.loadLibrary("vault")
    }
}
