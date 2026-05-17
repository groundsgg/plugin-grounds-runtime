package gg.grounds.runtime

import kotlin.test.Test
import kotlin.test.assertContains

class RuntimeDiagnosticsTest {
    @Test
    fun `diagnostics include runtime identity and provided libraries`() {
        val diagnostics =
            RuntimeDiagnostics.lines(
                runtimeId = "plugin-grounds-runtime-paper",
                version = "0.1.0",
                platform = "paper",
            )

        assertContains(diagnostics, "runtimeId=plugin-grounds-runtime-paper")
        assertContains(diagnostics, "version=0.1.0")
        assertContains(diagnostics, "platform=paper")
        assertContains(diagnostics, "- io.grpc:grpc-api:1.81.0")
        assertContains(diagnostics, "- com.google.protobuf:protobuf-java:4.34.1")
    }
}
