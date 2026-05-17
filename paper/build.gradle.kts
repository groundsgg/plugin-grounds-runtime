import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.gradleup.shadow")
    id("gg.grounds.kotlin-conventions")
}

repositories { maven("https://repo.papermc.io/repository/maven-public/") }

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.63-stable")
    implementation(project(":common"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.3.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("com.google.protobuf:protobuf-java:4.34.1")
    implementation("io.grpc:grpc-api:1.81.0")
    implementation("io.grpc:grpc-core:1.81.0")
    implementation("io.grpc:grpc-context:1.81.0")
    implementation("io.grpc:grpc-stub:1.81.0")
    implementation("io.grpc:grpc-protobuf:1.81.0")
    implementation("io.grpc:grpc-netty-shaded:1.81.0")
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
