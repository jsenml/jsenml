import internal.isTestTask
import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("java")
    id("io.github.malczuuu.nullmarked")
    id("net.ltgt.errorprone")
}

// Every package must declare a hand-written package-info.java annotated with @NullMarked. Nothing is generated.
nullmarked {
    verifyOnly = true
    verify {
        strict()
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        if (isTestTask()) {
            disableAllChecks = true
        }

        error("NullAway")
        option("NullAway:OnlyNullMarked", "true")
        option("NullAway:JSpecifyMode", "true")
    }
}
