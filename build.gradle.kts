plugins {
    `java-library`
    id("io.spring.dependency-management") version "1.1.7"
    id("com.vanniktech.maven.publish") version "0.34.0"
}

group = "dev.alubenets"
version = "0.0.1-SNAPSHOT"
description =
    "A Spring AMQP Extensions library providing useful utilities and enhancements for RabbitMQ in Spring applications."

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
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

tasks.withType<GenerateModuleMetadata>().configureEach {
    dependsOn(tasks.named("plainJavadocJar"))
}

mavenPublishing {
    configure(
        com.vanniktech.maven.publish.JavaLibrary(
            javadocJar = com.vanniktech.maven.publish.JavadocJar.Javadoc(),
            sourcesJar = true
        )
    )
    publishToMavenCentral()
    signAllPublications()
    coordinates(project.group.toString(), project.name, project.version.toString())
    pom {
        name.set(project.name)
        description.set(project.description)
        url.set("https://github.com/snejokeee/amqpex")
        inceptionYear.set("2025")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("snejokeee")
                name.set("Aleksey Lubenets")
                email.set("an.lubenets@gmail.com")
                url.set("https://alubenets.dev")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/snejokeee/amqpex.git")
            developerConnection.set("scm:git:ssh://github.com/snejokeee/amqpex.git")
            url.set("https://github.com/snejokeee/amqpex")
        }
    }
}