package gg.grounds.runtime

object RuntimeDiagnostics {
    fun lines(runtimeId: String, version: String, platform: String): List<String> {
        val manifestStatus =
            if (RuntimeManifestLoader.loadRaw() == null) {
                "missing"
            } else {
                "found"
            }

        return buildList {
            add("Grounds Runtime")
            add("runtimeId=$runtimeId")
            add("version=$version")
            add("platform=$platform")
            add("manifestPath=${RuntimeManifestLoader.defaultPath}")
            add("manifestStatus=$manifestStatus")
            add("libraries:")
            RuntimeLibraries.provided.forEach { library -> add("- ${library.coordinate()}") }
        }
    }
}
