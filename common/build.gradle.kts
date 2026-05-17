import groovy.json.JsonSlurper

plugins { id("gg.grounds.kotlin-conventions") }

dependencies { testImplementation(kotlin("test")) }

val generatedRuntimeCatalogDirectory =
    layout.buildDirectory.dir("generated/sources/runtimeCatalog/kotlin")

val generateRuntimeLibraries =
    tasks.register("generateRuntimeLibraries") {
        val catalogFile =
            layout.projectDirectory.file("src/main/resources/grounds-runtime-libraries.json")
        val outputFile =
            generatedRuntimeCatalogDirectory.map {
                it.file("gg/grounds/runtime/RuntimeLibraries.kt")
            }

        inputs.file(catalogFile)
        outputs.file(outputFile)

        doLast {
            val catalog = JsonSlurper().parse(catalogFile.asFile) as Map<*, *>
            val libraries = catalog["libraries"] as List<*>
            val source = buildString {
                appendLine("package gg.grounds.runtime")
                appendLine()
                appendLine("object RuntimeLibraries {")
                appendLine("    val provided =")
                appendLine("        listOf(")
                libraries.forEach { item ->
                    val library = item as Map<*, *>
                    appendLine(
                        "            RuntimeLibraryInfo(\"${library["group"]}\", \"${library["name"]}\", \"${library["version"]}\"),"
                    )
                }
                appendLine("        )")
                appendLine("}")
            }

            outputFile.get().asFile.apply {
                parentFile.mkdirs()
                writeText(source)
            }
        }
    }

kotlin { sourceSets.named("main") { kotlin.srcDir(generatedRuntimeCatalogDirectory) } }

tasks
    .matching { it.name in setOf("compileKotlin", "kaptGenerateStubsKotlin") }
    .configureEach { dependsOn(generateRuntimeLibraries) }
