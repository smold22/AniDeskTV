plugins {
    id("anidesk.android.library")
    alias(libs.plugins.kotlinSerialization)
    id("anidesk.android.hilt")
}

dependencies {
    implementation(libs.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":core:model"))
}