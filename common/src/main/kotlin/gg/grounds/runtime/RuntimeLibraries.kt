package gg.grounds.runtime

object RuntimeLibraries {
    val provided: List<RuntimeLibraryInfo> =
        RuntimeLibraryCatalog.providedCoordinates.map { coordinate ->
            val parts = coordinate.split(':', limit = 3)
            check(parts.size == 3) {
                "Runtime library coordinate must use group:name:version format (coordinate=$coordinate)"
            }
            RuntimeLibraryInfo(group = parts[0], name = parts[1], version = parts[2])
        }
}
