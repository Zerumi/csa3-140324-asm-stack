import org.gradle.internal.classpath.Instrumented.systemProperty

plugins {
    kotlin("jvm") version "1.9.22"
    application
    `jvm-test-suite`
}

group = "io.github"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass = "io.github.zerumi.csa3.comp.MainKt"
}

dependencies {
    implementation(project(":isa"))
    implementation("org.slf4j:slf4j-log4j12:2.0.3")
    implementation("io.github.oshai:kotlin-logging-jvm:6.0.0")
    implementation("com.github.ajalt.clikt:clikt:4.3.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

fun SourceSet.addSrcSetMainClasspath() {
    compileClasspath += sourceSets.main.get().compileClasspath + sourceSets.main.get().output
    runtimeClasspath += output + compileClasspath
}

fun SourceSet.configureSrcSetDirs(dirName: String) {
    kotlin.setSrcDirs(listOf("src/test/$dirName/kotlin"))
    resources.setSrcDirs(listOf("src/test/$dirName/resources"))
}

fun JvmTestSuite.shouldRunAfter(vararg paths: Any) =
    targets.all {
        testTask.configure {
            jvmArgs("-DupdateExpected=${System.getProperty("updateExpected")}")
            shouldRunAfter(paths)
        }
    }

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                dependencies {
                    implementation(project(":isa"))
                    implementation(project(":asm"))
                    implementation(project(":comp"))
                }
                sources.addSrcSetMainClasspath()
            }
        }

        val test by getting(JvmTestSuite::class) {
            testType.set(TestSuiteType.UNIT_TEST)
            sources.configureSrcSetDirs("unit")
        }

        val integrationTest by registering(JvmTestSuite::class) {
            testType.set(TestSuiteType.INTEGRATION_TEST)
            sources.configureSrcSetDirs("integration")
            shouldRunAfter(test)
        }
    }
}

tasks.register("updateExpected") {
    System.setProperty("updateExpected", "true")
    systemProperty("updateExpected", "true")
    finalizedBy(testing.suites.named("integrationTest"))
}


tasks.named("check") {
    dependsOn(testing.suites.named("integrationTest"))
}

tasks.withType(Jar::class) {
    manifest {
        attributes["Manifest-Version"] = "1.0"
        attributes["Main-Class"] = "MainKt"
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
