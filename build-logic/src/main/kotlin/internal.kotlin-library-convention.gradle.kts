import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("internal.java-library-convention")
    id("org.jetbrains.kotlin.jvm")
}

// Kotlin modules are compiled with the same JDK toolchain as Java modules, targeting the same Java version (see
// JavaVersions.kt). Sources are compiled by the latest Kotlin compiler, but the Kotlin API and language versions are
// pinned to the lowest supported one.

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    compilerOptions {
        explicitApi()
        moduleName = project.name
        apiVersion = KotlinVersion.KOTLIN_2_2
        languageVersion = KotlinVersion.KOTLIN_2_2
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        javaParameters = true
    }
}

tasks.named<KotlinCompile>("compileKotlin").configure {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        freeCompilerArgs.add("-Xjdk-release=17")
    }
}
