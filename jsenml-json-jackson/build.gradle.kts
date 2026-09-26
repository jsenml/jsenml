plugins {
    id("internal.errorprone-convention")
    id("internal.kover-convention")
    id("internal.java-library-convention")
    id("internal.publishing-convention")
    alias(libs.plugins.nmcp)
}

dependencies {
    // Main
    api(project(":jsenml-json"))
    api(libs.jackson.databind)

    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)
}

// see build-logic/src/main/kotlin/internal.publishing-convention.gradle.kts
internalBuild {
    displayName = "JSenML JSON Jackson"
    description = "Jackson 3 JSON backend for JSenML, SenML (RFC 8428) library for Java"
}
