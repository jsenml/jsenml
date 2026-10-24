plugins {
    id("internal.errorprone-convention")
    id("internal.kover-convention")
    id("internal.java-library-convention")
    id("internal.publishing-convention")
    id("java-test-fixtures")
    alias(libs.plugins.nmcp)
}

dependencies {
    // Main
    api(project(":jsenml-core:jsenml-api"))

    // Test
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    testImplementation(libs.assertj.core)

    // Test Fixtures (shared SenmlXmlCodec contract tests for backend modules)
    testFixturesApi(testFixtures(project(":jsenml-core:jsenml-api")))
    testFixturesApi(libs.junit.jupiter)
    testFixturesApi(libs.assertj.core)

    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)
}

// see build-logic/src/main/kotlin/internal.publishing-convention.gradle.kts
internalBuild {
    displayName = "JSenML XML"
    description = "SenML XML (application/senml+xml) codec API and SPI of JSenML, SenML (RFC 8428) library for Java"
}

// Test fixtures are consumed by backend modules only and are not published.
val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.withVariantsFromConfiguration(configurations["testFixturesApiElements"]) { skip() }
javaComponent.withVariantsFromConfiguration(configurations["testFixturesRuntimeElements"]) { skip() }
