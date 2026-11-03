plugins {
    id("internal.java-library-convention")
    id("internal.publishing-convention")
    alias(libs.plugins.nmcp)
}

dependencies {
    api(project(":jsenml-core:jsenml-kotlin"))
    api(project(":jsenml-jackson:jsenml-json-jackson"))
    api(project(":jsenml-spring-boot:jsenml-spring-boot-autoconfigure"))
    api(libs.spring.boot.starter.jackson)
}

// see build-logic/src/main/kotlin/internal.publishing-convention.gradle.kts
internalBuild {
    displayName = "JSenML Spring Boot Starter"
    description = "Spring Boot starter for SenML (RFC 8428) library for Java"
}

// Starter has no sources, it only aggregates dependencies. Automatic-Module-Name keeps a stable module name anyway.
tasks.named<Jar>("jar") {
    manifest {
        attributes["Automatic-Module-Name"] = "io.github.jsenml.spring.boot.starter"
    }
}
