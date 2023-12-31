rootProject.name = "allDone"

include(":androidApp")
include(":shared")
include(":desktopApp")
include(":entities")
include(":entities-test")
include(":task-server")
include(":task-server:logic")
include(":shared:logic")
include(":shared:logic-test")


pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
    }

    plugins {
        val kotlinVersion = extra["kotlin.version"] as String
        val agpVersion = extra["agp.version"] as String
        val composeVersion = extra["compose.version"] as String
        val ktorVersion = extra["ktor_version"] as String

        kotlin("jvm").version(kotlinVersion)
        kotlin("multiplatform").version(kotlinVersion)
        kotlin("android").version(kotlinVersion)
        kotlin("plugin.serialization") version kotlinVersion

        id("com.android.application").version(agpVersion)
        id("com.android.library").version(agpVersion)

        id("org.jetbrains.compose").version(composeVersion)
        id("io.ktor.plugin").version(ktorVersion)
        id("org.jetbrains.kotlin.jvm") version "1.8.20"
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
