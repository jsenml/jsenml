pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "jsenml"

include(":jsenml-api")
include(":jsenml-json")
include(":jsenml-cbor")
include(":jsenml-xml")
include(":jsenml-json-jackson")
include(":jsenml-cbor-jackson")
include(":jsenml-xml-jackson")
include(":jsenml-json-jackson2")
include(":jsenml-cbor-jackson2")
