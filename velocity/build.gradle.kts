import com.github.gmazzo.buildconfig.BuildConfigExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.gmazzo.buildconfig")
    id("com.gradleup.shadow")
    id("gg.grounds.kotlin-conventions")
}

repositories { maven("https://repo.papermc.io/repository/maven-public/") }

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
    kapt("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
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
