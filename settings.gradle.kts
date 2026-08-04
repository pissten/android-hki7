pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
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
    // Kotlin/Wasm configures an Ivy repository for the Node.js distribution used by its browser
    // toolchain. FAIL_ON_PROJECT_REPOS rejects that repository before any Wasm task can run.
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
    }
}

// This becomes an NPM workspace/package name for Kotlin/Wasm and therefore cannot contain spaces.
// The Android application label and every user-visible HKI 7 name are defined elsewhere.
rootProject.name = "hki7"
include(":app")
include(":sharedUi")
include(":webApp")
