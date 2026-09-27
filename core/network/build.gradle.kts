plugins {
    id("anidesk.android.library")
    alias(libs.plugins.kotlinSerialization)
    id("anidesk.android.hilt")
}

dependencies {
    implementation(libs.bundles.ktor.client.json)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.core.ktx)
    implementation(project(":core:preferences"))
}