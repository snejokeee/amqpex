plugins {
    `java-library`
    jacoco
    id("com.vanniktech.maven.publish") version "0.35.0"
}

group = "dev.alubenets"
version = "0.0.2"
description =
    "A Spring AMQP Extensions library providing useful utilities and enhancements for RabbitMQ in Spring applications."

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

val springBootVersion = "[3.3.0,4.0.0)"
val springAmqpVersion = "[3.1.5,3.2]"
val sl4fjVersion = "[2.0.13,2.1)"
val rabbitClientVersion = "[5.21.0,5.25]"

dependencies {
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:$springBootVersion")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor:$springBootVersion")

    compileOnly("com.rabbitmq:amqp-client:$rabbitClientVersion")

    api("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
    api("org.springframework.amqp:spring-rabbit:$springAmqpVersion")
    api("org.springframework.amqp:spring-amqp:$springAmqpVersion")
    api("org.slf4j:slf4j-api:$sl4fjVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("org.springframework.amqp:spring-rabbit-test:$springAmqpVersion")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.assertj:assertj-core")

    testImplementation("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.withType<Test> {
    useJUnitPlatform()

    reports {
        html.required.set(true)
        junitXml.required.set(true)
        junitXml.outputLocation.set(project.layout.buildDirectory.dir("test-results/junit/xml"))
    }

    finalizedBy(tasks.jacocoTestReport)
}

jacoco {
    toolVersion = "0.8.14"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
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