plugins { id("gg.grounds.base-conventions") version "0.6.0" }

val versionOverride = providers.gradleProperty("versionOverride").orNull

allprojects {
    group = "gg.grounds"
    version = versionOverride ?: "0.1.0"
}
