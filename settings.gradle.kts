rootProject.name = "AniDeskTv"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":app")

include(":core:model")
include(":core:mvi")
include(":core:navigation")
include(":core:preferences")
include(":core:network")
include(":core:designsystem")

include(":feature:main:api")
include(":feature:main:presentation")
include(":feature:main:ui-tv")

include(":feature:home:api")
include(":feature:home:domain")
include(":feature:home:data")
include(":feature:home:presentation")
include(":feature:home:ui-tv")

include(":feature:details:api")
include(":feature:details:domain")
include(":feature:details:data")
include(":feature:details:presentation")
include(":feature:details:ui-tv")

include(":feature:search:api")
include(":feature:search:domain")
include(":feature:search:data")
include(":feature:search:presentation")
include(":feature:search:ui-tv")

include(":feature:top:api")
include(":feature:top:domain")
include(":feature:top:data")
include(":feature:top:presentation")
include(":feature:top:ui-tv")

include(":feature:genres:api")
include(":feature:genres:presentation")
include(":feature:genres:ui-tv")

include(":feature:bookmarks:api")
include(":feature:bookmarks:presentation")
include(":feature:bookmarks:ui-tv")

include(":feature:schedule:api")
include(":feature:schedule:domain")
include(":feature:schedule:data")
include(":feature:schedule:presentation")
include(":feature:schedule:ui-tv")

include(":feature:settings:api")
include(":feature:settings:presentation")
include(":feature:settings:ui-tv")