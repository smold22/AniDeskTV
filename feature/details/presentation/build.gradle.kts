plugins {
    id("anidesk.android.library")
    id("anidesk.android.hilt")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(project(":core:model"))
    implementation(project(":core:mvi"))
    implementation(project(":core:navigation"))
    implementation(project(":core:preferences"))
    implementation(project(":core:network"))
    implementation(project(":feature:details:domain"))
    implementation(project(":feature:details:api"))
}
