import com.diffplug.spotless.LineEnding

plugins {
    alias(libs.plugins.kover)
    alias(libs.plugins.nmcp) apply false
    alias(libs.plugins.nmcp.aggregation)
    alias(libs.plugins.spotless)
}

dependencies {
    nmcpAggregation(project(":jsenml-api"))
    nmcpAggregation(project(":jsenml-cbor"))
    nmcpAggregation(project(":jsenml-json"))
    nmcpAggregation(project(":jsenml-xml"))
    nmcpAggregation(project(":jsenml-json-jackson"))

    kover(project(":jsenml-api"))
    kover(project(":jsenml-cbor"))
    kover(project(":jsenml-json"))
    kover(project(":jsenml-xml"))
    kover(project(":jsenml-json-jackson"))
}

nmcpAggregation {
    centralPortal {
        username = System.getenv("PUBLISHING_USERNAME")
        password = System.getenv("PUBLISHING_PASSWORD")

        publishingType = "USER_MANAGED"
    }
}

spotless {
    val licenseHeader = "${rootProject.rootDir}/gradle/license-header.txt"

    java {
        target("**/src/**/*.java")
        licenseHeaderFile(licenseHeader)

        googleJavaFormat()
        forbidWildcardImports()
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }
    format("javaMisc") {
        target("**/src/**/package-info.java", "**/src/**/module-info.java")

        // License headers in these files are not formatted with standard java group, so we need to use custom settings.
        // The regex is designed to find out where the code starts in these files, so the license header can be placed
        // before it.
        //
        // The code starts with either:
        //
        // - any annotation (ex. @NullMarked before package declaration),
        // - package, module or import declaration,
        // - "/**" in case of a pre-package (or pre-module) JavaDoc.
        val delimiter = "^(@|package|import|module|/\\*\\*)"

        licenseHeaderFile(licenseHeader, delimiter)
    }
    kotlin {
        target("**/src/**/*.kt")
        targetExclude("build-logic/**")

        ktfmt().metaStyle()
        licenseHeaderFile(licenseHeader)
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**")

        ktlint().editorConfigOverride(mapOf("max_line_length" to "120"))
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }
    format("yaml") {
        target("**/*.yml", "**/*.yaml")

        trimTrailingWhitespace()
        leadingTabsToSpaces(2)
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }
    format("misc") {
        target("**/.gitattributes", "**/.gitignore")

        trimTrailingWhitespace()
        leadingTabsToSpaces(4)
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }
}

// Aggregated coverage of all modules, merged by Kover into `build/reports/kover`.
tasks.named<Task>("check").configure {
    finalizedBy(tasks.named("koverHtmlReport"), tasks.named("koverXmlReport"))
}

defaultTasks("spotlessApply", "build")
