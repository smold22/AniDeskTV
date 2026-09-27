plugins {
    id("anidesk.android.library.compose")
    id("anidesk.android.hilt")
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    implementation(libs.bundles.compose.core)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.jetbrains.navigation3.ui)
    implementation(libs.androidx.navigation3.viewmodel)
    implementation(libs.hilt.navigation.compose)
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(project(":core:mvi"))
    implementation(project(":core:navigation"))
    implementation(project(":core:network"))
    implementation(project(":core:preferences"))
    implementation(project(":feature:main:api"))
    implementation(project(":feature:main:presentation"))
}
