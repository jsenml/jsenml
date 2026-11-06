import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("org.jetbrains.kotlin.jvm")
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(project(":jsenml-spring-boot:jsenml-spring-boot-starter"))
    implementation(project(":jsenml-jackson:jsenml-cbor-jackson"))
    implementation(project(":jsenml-core:jsenml-kotlin"))

    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.springdoc.openapi.starter.webflux.ui)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    testImplementation(libs.spring.boot.starter.webflux.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    testLogging {
        events(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
        exceptionFormat = TestExceptionFormat.SHORT
    }
}
