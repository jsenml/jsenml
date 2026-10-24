plugins {
    id("internal.errorprone-convention")
    id("internal.kover-convention")
    id("internal.java-library-convention")
    id("internal.publishing-convention")
    alias(libs.plugins.nmcp)
}

dependencies {
    // Main
    api(project(":jsenml-core:jsenml-json"))
    api(libs.gson)

    // Test
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    testImplementation(libs.assertj.core)
    testImplementation(testFixtures(project(":jsenml-core:jsenml-json")))

    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)
}

// see build-logic/src/main/kotlin/internal.publishing-convention.gradle.kts
internalBuild {
    displayName = "JSenML JSON Gson"
    description = "Gson JSON backend for JSenML, SenML (RFC 8428) library for Java"
}
