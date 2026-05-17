import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.gradleup.shadow")
    id("gg.grounds.kotlin-conventions")
}

apply(from = rootProject.file("gradle/runtime-provider-dependencies.gradle.kts"))

repositories { maven("https://repo.papermc.io/repository/maven-public/") }

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.63-stable")
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
