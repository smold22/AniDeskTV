plugins {
    id("anidesk.android.library.compose")
    id("anidesk.android.hilt")
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    implementation(libs.bundles.compose.core)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.bundles.coil.full)
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(project(":core:mvi"))
    implementation(project(":core:navigation"))
    implementation(project(":core:network"))
    implementation(project(":feature:favorites:api"))
    implementation(project(":feature:favorites:presentation"))
}
