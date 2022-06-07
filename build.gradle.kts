plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.0"
    id("io.quarkus") version "2.9.1.Final"
    id ("org.jetbrains.kotlin.plugin.allopen") version "1.5.0"
}

group = "alo.meetup"
version = "1.0.0"

object Versions {
    const val JUNIT = "5.8.2"
    const val MOCKK = "1.12.0"
    const val ASSERTJ = "3.20.2"
    const val ARROW = "1.1.2"
    const val FAKER = "1.0.2"
    const val QUARKUS = "2.9.1.Final"
    const val REST_ASSURED = "5.0.1"
    const val TESTCONTAINERS = "1.17.2"
    const val FLYWAY = "8.5.11"
    const val POSTGRES = "42.2.23"
    const val FUEL = "2.3.1"
    const val JDBI = "3.29.0"
    const val WIREMOCK = "2.27.2"
    const val MICROMETER = "1.9.0"
}
repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(enforcedPlatform("io.quarkus:quarkus-bom:${Versions.QUARKUS}"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.arrow-kt:arrow-core:${Versions.ARROW}")
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    implementation("io.quarkus:quarkus-resteasy-reactive")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-agroal")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-micrometer")
    implementation("io.quarkus:quarkus-narayana-jta")
    implementation("io.quarkus:quarkus-smallrye-reactive-messaging-kafka")
    implementation("org.jdbi:jdbi3-core:${Versions.JDBI}")
    implementation("org.postgresql:postgresql:${Versions.POSTGRES}")
    implementation("org.flywaydb:flyway-core:${Versions.FLYWAY}")
    implementation("com.github.kittinunf.fuel:fuel:${Versions.FUEL}")
    implementation("com.github.kittinunf.fuel:fuel-jackson:${Versions.FUEL}")
    implementation("io.micrometer:micrometer-registry-datadog:${Versions.MICROMETER}")


    testImplementation(group= "com.github.javafaker", name= "javafaker", version= Versions.FAKER) {
        exclude(group = "org.yaml")
    }
    testImplementation(group = "io.mockk", name = "mockk", version = Versions.MOCKK)
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.JUNIT}")
    testImplementation(group = "org.assertj", name = "assertj-core", version = Versions.ASSERTJ)
    testImplementation("io.quarkus:quarkus-junit5:${Versions.QUARKUS}")
    testImplementation("io.rest-assured:rest-assured:${Versions.REST_ASSURED}")
    testImplementation(group =  "org.testcontainers", name = "testcontainers", version = Versions.TESTCONTAINERS)
    testImplementation(group =  "org.testcontainers", name = "kafka", version = Versions.TESTCONTAINERS)
    testImplementation("org.testcontainers:postgresql:${Versions.TESTCONTAINERS}")
    testImplementation("com.github.tomakehurst:wiremock:${Versions.WIREMOCK}")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
}

allOpen {
    annotation("javax.ws.rs.Path")
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.apply {
    test {
        maxParallelForks = 1
        enableAssertions = true
        useJUnitPlatform {}
    }
}
