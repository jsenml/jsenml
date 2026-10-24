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
    api(libs.jspecify)

    // Test
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    testImplementation(libs.assertj.core)

    // Test Fixtures (shared SenmlJsonCodec contract tests for backend modules)
    testFixturesApi(libs.junit.jupiter)
    testFixturesApi(libs.assertj.core)

    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)
}

// see build-logic/src/main/kotlin/internal.publishing-convention.gradle.kts
internalBuild {
    displayName = "JSenML API"
    description = "SenML (RFC 8428) model and codec SPI for Java"
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:-module")
}

// Test fixtures are consumed by backend modules only and are not published.
val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.withVariantsFromConfiguration(configurations["testFixturesApiElements"]) { skip() }
javaComponent.withVariantsFromConfiguration(configurations["testFixturesRuntimeElements"]) { skip() }
