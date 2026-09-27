package com.anidesk.tv.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        extensions.configure<LibraryExtension> {
            namespace = defaultNamespace(path)
            compileSdk = libs.versionInt("android-compileSdk")
            defaultConfig {
                minSdk = libs.versionInt("android-minSdk")
            }
            configureJava21()
        }
        addCoreLibraryDesugaring()
        enforceLayering()
    }
}

class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("anidesk.android.library")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        configureComposeCompiler()
        dependencies.add("implementation", libs.findLibrary("compose-uiToolingPreview").get())
        dependencies.add("debugImplementation", libs.findLibrary("compose-uiTooling").get())
        dependencies.add("api", libs.findLibrary("kotlinx-collections-immutable").get())
        Unit
    }
}

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        configureComposeCompiler()
        extensions.configure<ApplicationExtension> {
            namespace = defaultNamespace(path)
            compileSdk = libs.versionInt("android-compileSdk")
            defaultConfig {
                minSdk = libs.versionInt("android-minSdk")
            }
            configureJava21()
        }
        addCoreLibraryDesugaring()
    }
}

class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val kotlinMetadataJvmVersion =
                libs.findVersion("kotlin-metadata-jvm").get().requiredVersion
            configurations.configureEach {
                resolutionStrategy.eachDependency {
                    if (requested.group == "org.jetbrains.kotlin" && requested.name == "kotlin-metadata-jvm") {
                        useVersion(kotlinMetadataJvmVersion)
                        because("Hilt depends on kotlin-metadata-jvm which cannot read Kotlin 2.4 metadata.")
                    }
                }
            }
            pluginManager.apply("com.google.dagger.hilt.android")
            pluginManager.apply("com.google.devtools.ksp")
            dependencies.add("implementation", libs.findLibrary("hilt-android").get())
            dependencies.add("ksp", libs.findLibrary("hilt-compiler").get())
        }
    }
}

private val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

private fun defaultNamespace(path: String): String =
    "com.anidesk.tv" + path.removePrefix(":").replace(':', '.').replace("-", "")

private fun VersionCatalog.versionInt(name: String): Int =
    findVersion(name).get().requiredVersion.toInt()

private fun LibraryExtension.configureJava21() {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        isCoreLibraryDesugaringEnabled = true
    }
}

private fun ApplicationExtension.configureJava21() {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        isCoreLibraryDesugaringEnabled = true
    }
}

private fun Project.enforceLayering() {
    val consumer = path
    if (!consumer.startsWith(":core:")) return
    val strict = providers.gradleProperty("strictLayering").orNull != "false"
    val log = logger
    configurations.configureEach {
        dependencies.whenObjectAdded {
            val target = (this as? ProjectDependency)?.path
            if (target != null && target.startsWith(":feature:")) {
                val message = "Нарушение слоёв: $consumer зависит от $target. " +
                        "core-модули не должны знать про feature-модули."
                if (strict) throw GradleException(message) else log.warn("w: $message")
            }
        }
    }
}

private fun Project.addCoreLibraryDesugaring() {
    dependencies.add("coreLibraryDesugaring", libs.findLibrary("desugar-jdk-libs").get())
}

private fun Project.configureComposeCompiler() {
    extensions.configure<ComposeCompilerGradlePluginExtension> {
        if (providers.gradleProperty("enableComposeCompilerReports").orNull == "true") {
            val outputDir = layout.buildDirectory.dir("compose_compiler")
            metricsDestination.set(outputDir)
            reportsDestination.set(outputDir)
        }
    }
}