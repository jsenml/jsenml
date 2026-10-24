plugins {
    id("java-platform")
    id("internal.publishing-convention")
    alias(libs.plugins.nmcp)
}

dependencies {
    constraints {
        api(project(":jsenml-core:jsenml-api"))
        api(project(":jsenml-core:jsenml-cbor"))
        api(project(":jsenml-core:jsenml-json"))
        api(project(":jsenml-core:jsenml-kotlin"))
        api(project(":jsenml-core:jsenml-xml"))

        api(project(":jsenml-gson:jsenml-json-gson"))

        api(project(":jsenml-jackson:jsenml-cbor-jackson"))
        api(project(":jsenml-jackson:jsenml-json-jackson"))
        api(project(":jsenml-jackson:jsenml-xml-jackson"))

        api(project(":jsenml-jackson2:jsenml-cbor-jackson2"))
        api(project(":jsenml-jackson2:jsenml-json-jackson2"))
        api(project(":jsenml-jackson2:jsenml-xml-jackson2"))

        api(project(":jsenml-spring-boot:jsenml-spring-boot-autoconfigure"))
    }
}

// see build-logic/src/main/kotlin/internal.publishing-convention.gradle.kts
internalBuild {
    displayName = "JSenML BOM"
    description = "Bill of materials aligning versions of JSenML, SenML (RFC 8428) library for Java"
}
