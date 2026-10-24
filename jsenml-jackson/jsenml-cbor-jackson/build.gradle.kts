plugins {
    id("internal.errorprone-convention")
    id("internal.kover-convention")
    id("internal.java-library-convention")
    id("internal.publishing-convention")
    alias(libs.plugins.nmcp)
}

dependencies {
    // Main
    api(project(":jsenml-core:jsenml-cbor"))
    api(libs.jackson.dataformat.cbor)

    // Test
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    testImplementation(libs.assertj.core)
    testImplementation(testFixtures(project(":jsenml-core:jsenml-cbor")))

    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)
}

// see build-logic/src/main/kotlin/internal.publishing-convention.gradle.kts
internalBuild {
    displayName = "JSenML CBOR Jackson"
    description = "Jackson 3 CBOR backend for JSenML, SenML (RFC 8428) library for Java"
}
