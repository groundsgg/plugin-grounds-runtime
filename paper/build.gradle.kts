import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("com.gradleup.shadow")
    id("gg.grounds.kotlin-conventions")
    id("gg.grounds.runtime-provider-dependencies")
    `maven-publish`
}

repositories { maven("https://repo.papermc.io/repository/maven-public/") }

dependencies {
    compileOnly(libs.paper.api)
    implementation(project(":common"))
}

tasks.named("build") { dependsOn("shadowJar") }

tasks.named("jar") { enabled = false }

tasks.withType<ProcessResources> {
    inputs.property("version", project.version)
    filesMatching(listOf("**/plugin.yml")) { expand(mapOf("VERSION" to project.version)) }
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("${rootProject.name}-${project.name}")
    archiveClassifier.set("")
    archiveVersion.set("")
    relocate("io.grpc", "gg.grounds.runtime.libs.grpc")
    relocate("com.google.protobuf", "gg.grounds.runtime.libs.protobuf")
    mergeServiceFiles()
}

publishing {
    publications {
        withType<MavenPublication>().configureEach {
            artifactId = "${rootProject.name}-${project.name}"
            setArtifacts(listOf(tasks.named<ShadowJar>("shadowJar")))
        }
    }
}
