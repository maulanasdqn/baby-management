// Top-level build file where you can add configuration options common to all sub-projects/modules.

// KSP must be on the root buildscript classpath so Hilt's Gradle plugin can find
// com.google.devtools.ksp.gradle.KspTaskJvm in the same classloader (see dagger/issues/3965).
buildscript {
    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:${libs.versions.hilt.get()}")
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:${libs.versions.ksp.get()}")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}
