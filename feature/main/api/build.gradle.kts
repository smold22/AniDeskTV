plugins {
    id("anidesk.android.library.compose")
}

dependencies {
    implementation(libs.compose.runtime)
    implementation(project(":core:navigation"))
}
