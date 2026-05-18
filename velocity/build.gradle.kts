import com.github.gmazzo.buildconfig.BuildConfigExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("com.github.gmazzo.buildconfig")
    id("com.gradleup.shadow")
    id("gg.grounds.kotlin-conventions")
    id("gg.grounds.runtime-provider-dependencies")
    id("maven-publish")
}

repositories { maven("https://repo.papermc.io/repository/maven-public/") }

dependencies {
    compileOnly(libs.velocity.api)
    kapt(libs.velocity.api)
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
    exclude("META-INF/maven/**")
    relocate("io.grpc", "gg.grounds.runtime.libs.grpc")
    relocate("com.google.protobuf", "gg.grounds.runtime.libs.protobuf")
    mergeServiceFiles()
}

publishing {
    publications {
        withType<MavenPublication>().configureEach {
            artifactId = "${rootProject.name}-${project.name}"
            setArtifacts(listOf(tasks.named<ShadowJar>("shadowJar")))
            pom {
                name.set("Grounds Runtime Velocity")
                description.set("Shared runtime libraries for Grounds Velocity plugins")
            }
        }
    }
}
