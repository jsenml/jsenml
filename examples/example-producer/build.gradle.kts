import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("application")
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(project(":jsenml-jackson:jsenml-json-jackson"))
    implementation(project(":jsenml-jackson:jsenml-cbor-jackson"))
    implementation(project(":jsenml-jackson:jsenml-xml-jackson"))
    implementation(project(":jsenml-core:jsenml-kotlin"))
    implementation(kotlin("stdlib"))

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    testImplementation(libs.assertj.core)
}

application {
    mainClass = "com.example.producer.ProducerKt"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    testLogging {
        events(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
        exceptionFormat = TestExceptionFormat.SHORT
    }
}

tasks.named<Task>("build").configure {
    dependsOn(tasks.named("installDist"))
}

tasks.register<JavaExec>("runJson") {
    group = ApplicationPlugin.APPLICATION_GROUP
    description = "Sends packs of random measurements in SenML JSON to http://localhost:8080/packs."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = application.mainClass
    args("json", "--count=1")
}

tasks.register<JavaExec>("runCbor") {
    group = ApplicationPlugin.APPLICATION_GROUP
    description = "Sends packs of random measurements in SenML CBOR to http://localhost:8080/packs."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = application.mainClass
    args("cbor", "--count=1")
}

tasks.register<JavaExec>("runXml") {
    group = ApplicationPlugin.APPLICATION_GROUP
    description = "Sends packs of random measurements in SenML XML to http://localhost:8080/packs."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = application.mainClass
    args("xml", "--count=1")
}
