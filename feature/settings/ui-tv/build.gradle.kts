plugins {
    id("anidesk.android.library.compose")
    id("anidesk.android.hilt")
}

dependencies {
    implementation(libs.bundles.compose.core)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(project(":core:mvi"))
    implementation(project(":core:navigation"))
    implementation(project(":core:preferences"))
    implementation(project(":core:network"))
    implementation(project(":feature:settings:api"))
    implementation(project(":feature:settings:presentation"))
}
