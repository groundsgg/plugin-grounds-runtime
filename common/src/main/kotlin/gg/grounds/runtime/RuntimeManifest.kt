package gg.grounds.runtime

data class RuntimeManifest(
    val schemaVersion: Int,
    val runtime: RuntimeInfo,
    val libraries: List<RuntimeLibraryInfo>,
)

data class RuntimeInfo(val id: String, val name: String, val version: String, val platform: String)

data class RuntimeLibraryInfo(val group: String, val name: String, val version: String) {
    fun coordinate(): String = "$group:$name:$version"
}
