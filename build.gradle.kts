import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.0.20"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "dev.alubenets"
version = "1.0.0-SNAPSHOT"
description = "Spring AMQP Extensions"

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.7")
    }
}

dependencies {
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework.amqp:spring-amqp")
    implementation("org.springframework:spring-context")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.amqp:spring-rabbit-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testImplementation("org.assertj:assertj-core:3.25.3")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.test {
    useJUnitPlatform()
}