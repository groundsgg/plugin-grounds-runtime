plugins { id("gg.grounds.base-conventions") version "0.6.0" }

val versionOverride = providers.gradleProperty("versionOverride").orNull

allprojects {
    group = "gg.grounds"
    version = versionOverride ?: "0.1.0"

    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/groundsgg/library-i18n")
            credentials {
                username =
                    providers.gradleProperty("github.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password =
                    providers.gradleProperty("github.token").orNull ?: System.getenv("GITHUB_TOKEN")
            }
            content { includeModule("gg.grounds", "library-i18n") }
        }
    }
}
