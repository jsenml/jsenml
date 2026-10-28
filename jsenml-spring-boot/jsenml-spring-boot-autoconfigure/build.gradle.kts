plugins {
    id("internal.errorprone-convention")
    id("internal.java-library-convention")
    id("internal.kover-convention")
    id("internal.publishing-convention")
    alias(libs.plugins.nmcp)
}

dependencies {
    // Main
    api(project(":jsenml-core:jsenml-api"))
    api(project(":jsenml-core:jsenml-kotlin"))

    compileOnly(project(":jsenml-gson:jsenml-json-gson"))
    compileOnly(project(":jsenml-jackson:jsenml-cbor-jackson"))
    compileOnly(project(":jsenml-jackson:jsenml-json-jackson"))
    compileOnly(project(":jsenml-jackson:jsenml-xml-jackson"))

    compileOnly(libs.spring.boot.autoconfigure)
    compileOnly(libs.spring.boot.gson)
    compileOnly(libs.spring.boot.http.converter)
    compileOnly(libs.spring.boot.jackson)
    compileOnly(libs.springdoc.openapi.starter.common)

    annotationProcessor(libs.spring.boot.autoconfigure.processor)
    annotationProcessor(libs.spring.boot.configuration.processor)

    // Test
    testImplementation(project(":jsenml-jackson:jsenml-cbor-jackson"))
    testImplementation(project(":jsenml-gson:jsenml-json-gson"))
    testImplementation(project(":jsenml-jackson:jsenml-json-jackson"))
    testImplementation(project(":jsenml-jackson:jsenml-xml-jackson"))

    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.spring.boot.gson)
    testImplementation(libs.spring.boot.jackson)
    testImplementation(libs.springdoc.openapi.starter.webmvc.api)
    testRuntimeOnly(libs.junit.platform.launcher)

    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)
}

// see build-logic/src/main/kotlin/internal.publishing-convention.gradle.kts
internalBuild {
    displayName = "JSenML Spring Boot"
    description = "Spring Boot autoconfiguration of SenML (RFC 8428) library for Java"
}

// Spring Boot provides no module descriptors, so this module is an automatic module.
tasks.named<Jar>("jar") {
    manifest {
        attributes["Automatic-Module-Name"] = "io.github.jsenml.spring.boot.autoconfigure"
    }
}
