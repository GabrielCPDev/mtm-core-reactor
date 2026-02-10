plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    kotlin("kapt")
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("maven-publish")
}

group = "io.iggdrasil"
version = "0.0.1-rc16-SNAPSHOT"
description = "Multi-tenancy Manager Reactor Core"

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    // Shared / Commons
    implementation("com.iggdrasil.shared:commons:1.0.9-SNAPSHOT")

    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")

    // Annotation Processor
    kapt("org.springframework.boot:spring-boot-configuration-processor")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Kotlin & Coroutines
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")

    // Database Drivers & SPI
    implementation("io.r2dbc:r2dbc-spi")
    implementation("io.r2dbc:r2dbc-pool")
    implementation("org.postgresql:r2dbc-postgresql")
    implementation("io.asyncer:r2dbc-mysql")
    implementation("org.mongodb:mongodb-driver-reactivestreams")

    // JSON & HTTP Client
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.slf4j:slf4j-api")

    // --- TEST DEPENDENCIES ---
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("io.mockk:mockk:1.13.8")

    testImplementation(platform("org.testcontainers:testcontainers-bom:1.20.4"))

    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:mongodb")
    testImplementation("org.testcontainers:mysql")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    failOnNoDiscoveredTests = false
}

publishing {
    publications {
        create<MavenPublication>("gpr") {
            groupId = "io.igdrasil.mtm"
            artifactId = "mtm-core-reactor"
            version = project.version.toString()

            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/gabrielcpdev/mtm-core-reactor")
            credentials {
                username = project.findProperty("gpr.user")?.toString() ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.token")?.toString() ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}