import groovy.json.JsonSlurper
import org.gradle.api.publish.maven.MavenPublication

plugins {
    base
    `maven-publish`
}

val runtimeCatalogFile = layout.projectDirectory.file("grounds-runtime-libraries.json")

val validateRuntimeCatalog =
    tasks.register("validateRuntimeCatalog") {
        group = "verification"
        description = "Validates the published Grounds runtime catalog."
        inputs.file(runtimeCatalogFile)

        doLast {
            val catalog =
                (JsonSlurper().parse(runtimeCatalogFile.asFile) as? Map<*, *>)
                    ?: error("Runtime catalog must be a JSON object")
            val libraries =
                (catalog["libraries"] as? List<*>)
                    ?: error("Runtime catalog field libraries must be an array")

            require(libraries.isNotEmpty()) { "Runtime catalog field libraries must not be empty" }
        }
    }

tasks.named("check") { dependsOn(validateRuntimeCatalog) }

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/groundsgg/${rootProject.name}")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }

    publications {
        create<MavenPublication>("groundsRuntimeCatalog") {
            artifact(runtimeCatalogFile) {
                builtBy(validateRuntimeCatalog)
                extension = "json"
            }
            artifactId = "grounds-runtime-catalog"
        }
    }
}
