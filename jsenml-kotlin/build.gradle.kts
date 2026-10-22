plugins {
    id("internal.kover-convention")
    id("internal.kotlin-library-convention")
    id("internal.publishing-convention")
    alias(libs.plugins.nmcp)
}

dependencies {
    // Main
    api(project(":jsenml-api"))
    compileOnly(kotlin("stdlib"))

    // Test
    testImplementation(kotlin("stdlib"))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    testImplementation(libs.assertj.core)
}

// see build-logic/src/main/kotlin/internal.publishing-convention.gradle.kts
internalBuild {
    displayName = "JSenML Kotlin"
    description = "Kotlin DSL and extensions for JSenML, SenML (RFC 8428) library for Java"
}

// Kotlin module for JPMS uses an automatic module semantics.
tasks.named<Jar>("jar") {
    manifest {
        attributes["Automatic-Module-Name"] = "io.github.jsenml.kotlin"
    }
}
