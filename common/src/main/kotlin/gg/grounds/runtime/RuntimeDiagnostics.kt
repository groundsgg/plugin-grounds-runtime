package gg.grounds.runtime

object RuntimeDiagnostics {
    fun report(runtimeId: String, version: String, platform: String): RuntimeDiagnosticsReport {
        val manifestStatus =
            if (RuntimeManifestLoader.loadRaw() == null) {
                "missing"
            } else {
                "found"
            }

        return RuntimeDiagnosticsReport(
            fields =
                listOf(
                    "runtimeId=$runtimeId",
                    "version=$version",
                    "platform=$platform",
                    "manifestPath=${RuntimeManifestLoader.defaultPath}",
                    "manifestStatus=$manifestStatus",
                ),
            libraries = RuntimeLibraries.provided.map { it.coordinate() },
        )
    }

    fun lines(runtimeId: String, version: String, platform: String): List<String> =
        report(runtimeId, version, platform).consoleLines()
}

data class RuntimeDiagnosticsReport(val fields: List<String>, val libraries: List<String>) {
    fun consoleLines(): List<String> =
        listOf("Grounds Runtime") + fields + "libraries:" + libraries.map { "- $it" }
}
