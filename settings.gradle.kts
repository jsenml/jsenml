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

include(":jsenml-bom")
include(":jsenml-core:jsenml-api")
include(":jsenml-core:jsenml-json")
include(":jsenml-core:jsenml-cbor")
include(":jsenml-core:jsenml-kotlin")
include(":jsenml-jackson:jsenml-json-jackson")
include(":jsenml-jackson:jsenml-cbor-jackson")
include(":jsenml-jackson2:jsenml-json-jackson2")
include(":jsenml-jackson2:jsenml-cbor-jackson2")
include(":jsenml-gson:jsenml-json-gson")
include(":jsenml-spring-boot:jsenml-spring-boot-autoconfigure")
include(":jsenml-spring-boot:jsenml-spring-boot-starter")
include(":examples:example-producer")
include(":examples:example-webflux")
include(":examples:example-webmvc")
