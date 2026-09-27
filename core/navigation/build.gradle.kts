plugins {
    id("anidesk.android.library.compose")
    alias(libs.plugins.kotlinSerialization)
    id("anidesk.android.hilt")
}

dependencies {
    implementation(libs.bundles.compose.core)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    api(libs.jetbrains.navigation3.ui)
    implementation(libs.androidx.navigation3.viewmodel)
    implementation(libs.kotlinx.serialization.json)
}