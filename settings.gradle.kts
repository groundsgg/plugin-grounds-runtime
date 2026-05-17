rootProject.name = "plugin-grounds-runtime"

include("common", "paper", "velocity")

pluginManagement {
    repositories {
        mavenLocal()
        maven {
            url = uri("https://maven.pkg.github.com/groundsgg/*")
            credentials {
                username = providers.gradleProperty("github.user").orNull
                password = providers.gradleProperty("github.token").orNull
            }
        }
        gradlePluginPortal()
    }
}
