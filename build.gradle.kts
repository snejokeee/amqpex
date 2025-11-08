plugins {
    `java-library`
    id("io.spring.dependency-management") version "1.1.7"
}

group = "dev.alubenets"
version = "0.0.1-SNAPSHOT"
description = "Spring AMQP Extensions"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withSourcesJar()
    withJavadocJar()
}

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

    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework.amqp:spring-amqp")
    implementation("org.springframework.amqp:spring-rabbit")
    implementation("org.springframework:spring-context")
    implementation("org.slf4j:slf4j-api:2.0.16")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.amqp:spring-rabbit-test")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.assertj:assertj-core")

    testImplementation("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.withType<Test> {
    useJUnitPlatform()
}