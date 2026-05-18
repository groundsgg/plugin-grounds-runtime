import com.github.gmazzo.buildconfig.BuildConfigExtension
import groovy.json.JsonSlurper
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    id("com.github.gmazzo.buildconfig")
    id("gg.grounds.kotlin-conventions")
}

dependencies { testImplementation(kotlin("test")) }

val runtimeCatalogFile =
    rootProject.layout.projectDirectory.file("runtime-catalog/grounds-runtime-libraries.json")

data class RuntimeCatalogLibrary(val group: String, val name: String, val version: String)

val RuntimeCatalogLibrary.coordinate: String
    get() = "$group:$name:$version"

fun runtimeCatalogLibraries(catalogContent: String): List<RuntimeCatalogLibrary> {
    val catalog =
        (JsonSlurper().parseText(catalogContent) as? Map<*, *>)
            ?: error("Runtime catalog must be a JSON object")
    val libraries =
        (catalog["libraries"] as? List<*>)
            ?: error("Runtime catalog field libraries must be an array")

    return libraries.mapIndexed { index, value ->
        val library =
            (value as? Map<*, *>)
                ?: error("Runtime catalog field libraries[$index] must be an object")
        RuntimeCatalogLibrary(
            group = library.requiredString(index, "group"),
            name = library.requiredString(index, "name"),
            version = library.requiredString(index, "version"),
        )
    }
}

fun Map<*, *>.requiredString(index: Int, field: String): String {
    val value =
        this[field] as? String
            ?: error("Runtime catalog field libraries[$index].$field must be a string")
    require(value.isNotBlank()) {
        "Runtime catalog field libraries[$index].$field must not be blank"
    }
    return value
}

val runtimeLibraryCoordinates =
    providers.fileContents(runtimeCatalogFile).asText.map { catalogContent ->
        ArrayList(runtimeCatalogLibraries(catalogContent).map { it.coordinate })
    }

configure<BuildConfigExtension> {
    className("RuntimeLibraryCatalog")
    packageName("gg.grounds.runtime")
    useKotlinOutput()
    buildConfigField("List<String>", "providedCoordinates", runtimeLibraryCoordinates)
}

tasks.named<ProcessResources>("processResources") { from(runtimeCatalogFile) { into("") } }
