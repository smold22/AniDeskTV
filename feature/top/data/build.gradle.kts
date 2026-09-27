plugins {
    id("anidesk.android.library")
    id("anidesk.android.hilt")
}

dependencies {
    implementation(project(":core:network"))
    implementation(project(":core:preferences"))
    implementation(project(":core:model"))
    implementation(project(":feature:top:domain"))
}
