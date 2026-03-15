pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ecu-forge-app"
include(":app")
include(":core")
include(":transport")
include(":feature-diagnostics")
include(":feature-telemetry")
include(":feature-map")
