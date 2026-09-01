import internal.InternalBuildExtension
import internal.getBooleanProperty

plugins {
    id("maven-publish")
    id("signing")
}

val internalBuild: InternalBuildExtension = extensions.create(
    "internalBuild",
    InternalBuildExtension::class.java,
)

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            plugins.withType<JavaPlugin> {
                from(components["java"])
            }

            plugins.withType<JavaPlatformPlugin> {
                from(components["javaPlatform"])
            }

            pom {
                name = internalBuild.displayName
                description = internalBuild.description
                url = "https://github.com/jsenml/jsenml"
                inceptionYear = "2026"
                licenses {
                    license {
                        name = "Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                    }
                }
                scm {
                    connection = "scm:git:https://github.com/jsenml/jsenml.git"
                    developerConnection = "scm:git:git@github.com:jsenml/jsenml.git"
                    url = "https://github.com/jsenml/jsenml"
                }
                issueManagement {
                    system = "GitHub Issues"
                    url = "https://github.com/jsenml/jsenml/issues"
                }
            }
        }
    }
}

signing {
    if (project.getBooleanProperty("sign")) {
        useInMemoryPgpKeys(System.getenv("SIGNING_KEY"), System.getenv("SIGNING_PASSWORD"))
        sign(publishing.publications["maven"])
    }
}
