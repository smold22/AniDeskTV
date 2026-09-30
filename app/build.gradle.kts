plugins {
    id("anidesk.android.application")
    alias(libs.plugins.kotlinSerialization)
    id("anidesk.android.hilt")
}

val baseApplicationId = providers.gradleProperty("anidesk.applicationId").get()
val appVersionName = providers.gradleProperty("anidesk.versionName").get()
val appVersionCode = providers.gradleProperty("anidesk.versionCode").get().toInt()

android {
    namespace = "com.anidesk.tv"

    defaultConfig {
        applicationId = baseApplicationId
        targetSdk = libs.versions.android.compileSdk.get().toInt()

        versionName = appVersionName
        versionCode = appVersionCode
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            isDebuggable = false
        }
    }
    buildFeatures {
        buildConfig = true
        resValues = true
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(project(":core:mvi"))
    implementation(project(":core:navigation"))
    implementation(project(":core:network"))
    implementation(project(":core:preferences"))

    implementation(project(":feature:main:api"))
    implementation(project(":feature:main:presentation"))
    implementation(project(":feature:main:ui-tv"))

    implementation(project(":feature:home:api"))
    implementation(project(":feature:home:domain"))
    implementation(project(":feature:home:data"))
    implementation(project(":feature:home:presentation"))
    implementation(project(":feature:home:ui-tv"))

    implementation(project(":feature:details:api"))
    implementation(project(":feature:details:domain"))
    implementation(project(":feature:details:data"))
    implementation(project(":feature:details:presentation"))
    implementation(project(":feature:details:ui-tv"))

    implementation(project(":feature:search:api"))
    implementation(project(":feature:search:domain"))
    implementation(project(":feature:search:data"))
    implementation(project(":feature:search:presentation"))
    implementation(project(":feature:search:ui-tv"))

    implementation(project(":feature:top:api"))
    implementation(project(":feature:top:domain"))
    implementation(project(":feature:top:data"))
    implementation(project(":feature:top:presentation"))
    implementation(project(":feature:top:ui-tv"))

    implementation(project(":feature:genres:api"))
    implementation(project(":feature:genres:presentation"))
    implementation(project(":feature:genres:ui-tv"))

    implementation(project(":feature:bookmarks:api"))
    implementation(project(":feature:bookmarks:presentation"))
    implementation(project(":feature:bookmarks:ui-tv"))

    implementation(project(":feature:favorites:api"))
    implementation(project(":feature:favorites:presentation"))
    implementation(project(":feature:favorites:ui-tv"))

    implementation(project(":feature:schedule:api"))
    implementation(project(":feature:schedule:domain"))
    implementation(project(":feature:schedule:data"))
    implementation(project(":feature:schedule:presentation"))
    implementation(project(":feature:schedule:ui-tv"))

    implementation(project(":feature:settings:api"))
    implementation(project(":feature:settings:presentation"))
    implementation(project(":feature:settings:ui-tv"))

    implementation(libs.bundles.compose.core)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.lifecycle.runtimeKtx)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.leanback)
    implementation(libs.cicerone)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.bundles.coil.full)
    implementation(libs.bundles.media3.player)
    implementation(libs.okhttp)
    implementation(libs.jetbrains.navigation3.ui)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.navigation3.viewmodel)
}