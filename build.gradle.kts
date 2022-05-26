plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.0"
    id("io.quarkus") version "2.9.1.Final"
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

    testImplementation(group= "com.github.javafaker", name= "javafaker", version= Versions.FAKER) {
        exclude(group = "org.yaml")
    }
    testImplementation(group = "io.mockk", name = "mockk", version = Versions.MOCKK)
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.JUNIT}")
    testImplementation(group = "org.assertj", name = "assertj-core", version = Versions.ASSERTJ)
    testImplementation("io.quarkus:quarkus-junit5:${Versions.QUARKUS}")
    testImplementation("io.rest-assured:rest-assured:${Versions.REST_ASSURED}")
}

tasks.apply {
    test {
        enableAssertions = true
        useJUnitPlatform {}
    }
}
