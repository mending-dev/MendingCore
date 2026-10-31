pluginManagement {
    // Shared build conventions (compiler flags, toolchain, tests) live in build-logic.
    includeBuild("build-logic")
}

plugins {
    // Downloads the JDK from the toolchain definition if it is missing locally.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    // Repositories are declared once, here. Modules must not declare their own.
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/") { name = "papermc" }
    }
}

rootProject.name = "MendingCore"

include("api", "plugin")