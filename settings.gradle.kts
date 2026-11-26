rootProject.name = "mtm-core-reactor"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        kotlin("jvm") version "2.2.0"
        kotlin("plugin.spring") version "2.2.0"
        kotlin("kapt") version "2.2.0"

        id("org.springframework.boot") version "4.2.0"
        id("io.spring.dependency-management") version "1.1.7"
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    repositories {
        mavenCentral()
    }
}
