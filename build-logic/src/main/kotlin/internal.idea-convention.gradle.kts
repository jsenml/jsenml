import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.Gradle
import org.jetbrains.gradle.ext.JUnit
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings

plugins {
    id("org.jetbrains.gradle.plugin.idea-ext")
}

idea {
    project {
        settings {
            runConfigurations {
                create<Gradle>("Clean [jsenml]") {
                    taskNames = listOf("clean")
                    projectPath = rootProject.rootDir.absolutePath
                }
                create<Gradle>("Build [jsenml]") {
                    taskNames = listOf("spotlessApply build")
                    projectPath = rootProject.rootDir.absolutePath
                }
                create<Gradle>("Format Code [jsenml]") {
                    taskNames = listOf("spotlessApply")
                    projectPath = rootProject.rootDir.absolutePath
                }
                create<Application>("Run [example-webflux]") {
                    mainClass = "com.example.webflux.WebFluxApplicationKt"
                    moduleName = "jsenml.examples.example-webflux.main"
                    workingDirectory = rootProject.rootDir.absolutePath
                }
                create<Application>("Run [example-webmvc]") {
                    mainClass = "com.example.webmvc.WebMvcApplicationKt"
                    moduleName = "jsenml.examples.example-webmvc.main"
                    workingDirectory = rootProject.rootDir.absolutePath
                }
                create<JUnit>("JUnit [jsenml-api]") {
                    moduleName = "jsenml.jsenml-core.jsenml-api.test"
                    workingDirectory = rootProject.rootDir.absolutePath
                    packageName = "io.github.jsenml"
                }
                create<JUnit>("JUnit [jsenml-json]") {
                    moduleName = "jsenml.jsenml-core.jsenml-json.test"
                    workingDirectory = rootProject.rootDir.absolutePath
                    packageName = "io.github.jsenml.json"
                }
                create<JUnit>("JUnit [jsenml-json-jackson]") {
                    moduleName = "jsenml.jsenml-jackson.jsenml-json-jackson.test"
                    workingDirectory = rootProject.rootDir.absolutePath
                    packageName = "io.github.jsenml.json.jackson"
                }
                create<JUnit>("JUnit [jsenml-json-jackson2]") {
                    moduleName = "jsenml.jsenml-jackson2.jsenml-json-jackson2.test"
                    workingDirectory = rootProject.rootDir.absolutePath
                    packageName = "io.github.jsenml.json.jackson2"
                }
                create<JUnit>("JUnit [jsenml-json-gson]") {
                    moduleName = "jsenml.jsenml-gson.jsenml-json-gson.test"
                    workingDirectory = rootProject.rootDir.absolutePath
                    packageName = "io.github.jsenml.json.gson"
                }
                create<JUnit>("JUnit [jsenml-cbor]") {
                    moduleName = "jsenml.jsenml-core.jsenml-cbor.test"
                    workingDirectory = rootProject.rootDir.absolutePath
                    packageName = "io.github.jsenml.cbor"
                }
                create<JUnit>("JUnit [jsenml-cbor-jackson]") {
                    moduleName = "jsenml.jsenml-jackson.jsenml-cbor-jackson.test"
                    workingDirectory = rootProject.rootDir.absolutePath
                    packageName = "io.github.jsenml.cbor.jackson"
                }
                create<JUnit>("JUnit [jsenml-cbor-jackson2]") {
                    moduleName = "jsenml.jsenml-jackson2.jsenml-cbor-jackson2.test"
                    workingDirectory = rootProject.rootDir.absolutePath
                    packageName = "io.github.jsenml.cbor.jackson2"
                }
                create<JUnit>("JUnit [jsenml-xml]") {
                    moduleName = "jsenml.jsenml-core.jsenml-xml.test"
                    workingDirectory = rootProject.rootDir.absolutePath
                    packageName = "io.github.jsenml.xml"
                }
                create<JUnit>("JUnit [jsenml-xml-jackson]") {
                    moduleName = "jsenml.jsenml-jackson.jsenml-xml-jackson.test"
                    workingDirectory = rootProject.rootDir.absolutePath
                    packageName = "io.github.jsenml.xml.jackson"
                }
                create<JUnit>("JUnit [jsenml-xml-jackson2]") {
                    moduleName = "jsenml.jsenml-jackson2.jsenml-xml-jackson2.test"
                    workingDirectory = rootProject.rootDir.absolutePath
                    packageName = "io.github.jsenml.xml.jackson2"
                }
                create<JUnit>("JUnit [jsenml-spring-boot-autoconfigure]") {
                    moduleName = "jsenml.jsenml-spring-boot.jsenml-spring-boot-autoconfigure.test"
                    workingDirectory = rootProject.rootDir.absolutePath
                    packageName = "io.github.jsenml.spring.boot.autoconfigure"
                }
                create<JUnit>("JUnit [jsenml-kotlin]") {
                    moduleName = "jsenml.jsenml-core.jsenml-kotlin.test"
                    workingDirectory = rootProject.rootDir.absolutePath
                    packageName = "io.github.jsenml.kotlin"
                }
            }
        }
    }
}
