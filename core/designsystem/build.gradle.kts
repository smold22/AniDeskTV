plugins {
    id("anidesk.android.library.compose")
    alias(libs.plugins.kotlinSerialization)
    id("anidesk.android.hilt")
}

dependencies {
    implementation(libs.bundles.compose.core)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.bundles.coil.full)
    implementation(libs.androidx.core.ktx)
    implementation(project(":core:model"))
    implementation(project(":core:mvi"))
}
