import org.gradle.internal.classpath.Instrumented.systemProperty

plugins {
    kotlin("jvm") version "1.9.22"
    application
    idea
}

group = "io.github.zerumi"
version = "1.1"

repositories {
    mavenCentral()
}

application {
    mainClass = "io.github.zerumi.csa3.comp.MainKt"
}

sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().compileClasspath + sourceSets.main.get().output
        runtimeClasspath += output + compileClasspath
        kotlin.setSrcDirs(listOf("src/test/integration/kotlin"))
        resources.setSrcDirs(listOf("src/test/integration/resources"))
    }
}

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

val integrationTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
    implementation(project(":isa"))
    implementation("org.slf4j:slf4j-log4j12:2.0.3")
    implementation("io.github.oshai:kotlin-logging-jvm:6.0.0")
    implementation("com.github.ajalt.clikt:clikt:4.3.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    integrationTestImplementation(project(":isa"))
    integrationTestImplementation(project(":asm"))
    integrationTestImplementation(project(":comp"))
    integrationTestImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.register("updateExpected") {
    System.setProperty("updateExpected", "true")
    systemProperty("updateExpected", "true")
    finalizedBy(tasks.named("integrationTest"))
}

// Integration test gradle task
val integrationTest = task<Test>("integrationTest") {
    useJUnitPlatform()
    jvmArgs("-DupdateGolden=${System.getProperty("updateGolden")}")

    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath

    shouldRunAfter("test")
}

idea {
    module {
        testSourceDirs.plusAssign(sourceSets["integrationTest"].allSource.srcDirs)
    }
}

tasks.named("check") {
    dependsOn("integrationTest")
}

tasks.named<Test>("integrationTest") {
    dependsOn("test")
}

tasks.withType(Jar::class) {
    manifest {
        attributes["Manifest-Version"] = "1.0"
        attributes["Main-Class"] = "io.github.zerumi.csa3.comp.MainKt"
    }
    // To avoid the duplicate handling strategy error
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // To add all the dependencies
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}


tasks.test {
    useJUnitPlatform()
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

kotlin {
    jvmToolchain(17)
}
