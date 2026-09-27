plugins {
    `kotlin-dsl`
}

group = "com.anidesk.tv.buildlogic"

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.compose.compiler.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)
    implementation(libs.hilt.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "anidesk.android.library"
            implementationClass = "com.anidesk.tv.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "anidesk.android.library.compose"
            implementationClass = "com.anidesk.tv.buildlogic.AndroidLibraryComposeConventionPlugin"
        }
        register("androidApplication") {
            id = "anidesk.android.application"
            implementationClass = "com.anidesk.tv.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidHilt") {
            id = "anidesk.android.hilt"
            implementationClass = "com.anidesk.tv.buildlogic.AndroidHiltConventionPlugin"
        }
    }
}