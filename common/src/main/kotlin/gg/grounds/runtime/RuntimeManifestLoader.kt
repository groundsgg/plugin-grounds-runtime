package gg.grounds.runtime

import java.nio.file.Files
import java.nio.file.Path

object RuntimeManifestLoader {
    val defaultPath: Path = Path.of("/opt/grounds/runtime-manifest.json")

    fun loadRaw(path: Path = defaultPath): String? =
        if (Files.isRegularFile(path)) Files.readString(path) else null
}
