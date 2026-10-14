package gg.grounds.runtime

import gg.grounds.i18n.BundleCheck
import gg.grounds.i18n.SystemCategory
import gg.grounds.i18n.SystemLevel
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

    @Test
    fun `diagnostic messages use general context at info level`() {
        RuntimeDiagnosticMessage.entries.forEach { message ->
            assertEquals(SystemCategory.GENERAL, message.category)
            assertEquals(SystemLevel.INFO, message.level)
        }
    }
}
