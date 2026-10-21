plugins {
    id("java-platform")
    id("internal.publishing-convention")
    alias(libs.plugins.nmcp)
}

dependencies {
    constraints {
        api(project(":jsenml-api"))
        api(project(":jsenml-cbor"))
        api(project(":jsenml-json"))
        api(project(":jsenml-xml"))

        api(project(":jsenml-json-gson"))

        api(project(":jsenml-cbor-jackson"))
        api(project(":jsenml-json-jackson"))
        api(project(":jsenml-xml-jackson"))

        api(project(":jsenml-cbor-jackson2"))
        api(project(":jsenml-json-jackson2"))
        api(project(":jsenml-xml-jackson2"))
    }
}

// see build-logic/src/main/kotlin/internal.publishing-convention.gradle.kts
internalBuild {
    displayName = "JSenML BOM"
    description = "Bill of materials aligning versions of JSenML, SenML (RFC 8428) library for Java"
}
