plugins {
    id("anidesk.android.library")
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    implementation(libs.javax.inject)
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":core:navigation"))
    implementation(libs.jetbrains.navigation3.ui)
}
