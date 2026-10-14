package gg.grounds.runtime

import gg.grounds.i18n.BundleCheck
import kotlin.test.Test
import kotlin.test.assertEquals

class RuntimeDiagnosticMessagesTest {
    @Test
    fun `message bundles are complete`() {
        assertEquals(
            emptyList(),
            BundleCheck.problems(
                "gg.grounds.runtime.messages",
                javaClass.classLoader,
                RuntimeDiagnosticMessage.entries,
            ),
        )
    }
}
