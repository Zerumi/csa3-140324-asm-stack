plugins {
    kotlin("jvm") version "1.9.22"
    application
}

group = "io.github"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass = "MainKt"
}

dependencies {
    implementation(project(":isa"))
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
