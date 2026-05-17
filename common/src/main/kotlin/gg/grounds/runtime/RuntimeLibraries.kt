package gg.grounds.runtime

object RuntimeLibraries {
    val provided =
        listOf(
            RuntimeLibraryInfo("org.jetbrains.kotlin", "kotlin-stdlib", "2.3.0"),
            RuntimeLibraryInfo("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", "2.3.0"),
            RuntimeLibraryInfo("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.10.2"),
            RuntimeLibraryInfo("com.google.protobuf", "protobuf-java", "4.34.1"),
            RuntimeLibraryInfo("io.grpc", "grpc-api", "1.81.0"),
            RuntimeLibraryInfo("io.grpc", "grpc-core", "1.81.0"),
            RuntimeLibraryInfo("io.grpc", "grpc-context", "1.81.0"),
            RuntimeLibraryInfo("io.grpc", "grpc-stub", "1.81.0"),
            RuntimeLibraryInfo("io.grpc", "grpc-protobuf", "1.81.0"),
            RuntimeLibraryInfo("io.grpc", "grpc-netty-shaded", "1.81.0"),
        )
}
