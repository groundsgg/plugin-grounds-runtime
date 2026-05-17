import com.github.gmazzo.buildconfig.BuildConfigExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.gmazzo.buildconfig")
    id("com.gradleup.shadow")
    id("gg.grounds.kotlin-conventions")
}

apply(from = rootProject.file("gradle/runtime-provider-dependencies.gradle.kts"))

repositories { maven("https://repo.papermc.io/repository/maven-public/") }

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
    kapt("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
    implementation(project(":common"))
}

configure<BuildConfigExtension> {
    className("BuildInfo")
    packageName("gg.grounds")
    useKotlinOutput()
    buildConfigField("String", "VERSION", "\"${project.version}\"")
}

tasks.named("build") { dependsOn("shadowJar") }

tasks.named("jar") { enabled = false }

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("${rootProject.name}-${project.name}")
    archiveClassifier.set("")
    archiveVersion.set("")
    relocate("io.grpc", "gg.grounds.runtime.libs.grpc")
    relocate("com.google.protobuf", "gg.grounds.runtime.libs.protobuf")
    mergeServiceFiles()
}
