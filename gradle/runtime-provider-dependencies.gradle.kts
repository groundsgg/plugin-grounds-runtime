import groovy.json.JsonSlurper

data class RuntimeCatalogLibrary(val group: String, val name: String, val version: String) {
    val notation = "$group:$name:$version"
}

fun runtimeCatalogLibraries(): List<RuntimeCatalogLibrary> {
    val catalogFile = rootProject.layout.projectDirectory.file("common/src/main/resources/grounds-runtime-libraries.json").asFile
    val catalog = JsonSlurper().parse(catalogFile) as Map<*, *>
    val libraries = catalog["libraries"] as List<*>

    return libraries.map { item ->
        val library = item as Map<*, *>
        RuntimeCatalogLibrary(
            group = library["group"] as String,
            name = library["name"] as String,
            version = library["version"] as String,
        )
    }
}

dependencies {
    runtimeCatalogLibraries().forEach { library -> add("implementation", library.notation) }
}
