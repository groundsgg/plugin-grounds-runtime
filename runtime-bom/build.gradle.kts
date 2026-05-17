import groovy.json.JsonSlurper
import org.gradle.api.publish.maven.MavenPublication

plugins {
    `java-platform`
    `maven-publish`
}

javaPlatform { allowDependencies() }

val runtimeCatalogFile =
    rootProject.layout.projectDirectory.file("runtime-catalog/grounds-runtime-libraries.json")

data class CatalogRuntimeLibrary(val group: String, val name: String, val version: String)

fun runtimeLibraries(): List<CatalogRuntimeLibrary> {
    val catalog =
        (JsonSlurper().parse(runtimeCatalogFile.asFile) as? Map<*, *>)
            ?: error("Runtime catalog must be a JSON object")
    val libraries =
        (catalog["libraries"] as? List<*>)
            ?: error("Runtime catalog field libraries must be an array")

    return libraries.mapIndexed { index, value ->
        val runtimeLibrary =
            (value as? Map<*, *>)
                ?: error("Runtime catalog field libraries[$index] must be an object")
        CatalogRuntimeLibrary(
            group =
                runtimeLibrary["group"] as? String
                    ?: error("Runtime catalog field libraries[$index].group must be a string"),
            name =
                runtimeLibrary["name"] as? String
                    ?: error("Runtime catalog field libraries[$index].name must be a string"),
            version =
                runtimeLibrary["version"] as? String
                    ?: error("Runtime catalog field libraries[$index].version must be a string"),
        )
    }
}

dependencies {
    constraints {
        runtimeLibraries().forEach { runtimeLibrary ->
            api("${runtimeLibrary.group}:${runtimeLibrary.name}:${runtimeLibrary.version}")
        }
    }
}

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
        create<MavenPublication>("groundsRuntimeBom") {
            from(components["javaPlatform"])
            artifactId = "grounds-runtime-bom"
        }
    }
}
