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

        id("org.springframework.boot") version "4.0.0"
        id("io.spring.dependency-management") version "1.1.7"
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    repositories {
        mavenCentral()

        maven {
            name = "commons"
            url = uri("https://maven.pkg.github.com/gabrielcpdev/commons")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull
                password = providers.gradleProperty("gpr.token").orNull
            }
        }
    }
}
