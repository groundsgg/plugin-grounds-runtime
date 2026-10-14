package gg.grounds.runtime

import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.pointer.Pointers
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

class RuntimeDiagnosticsPresenterTest {
    @Test
    fun `player output localizes headings and preserves exact diagnostics`() {
        val audience = RecordingAudience(Locale.GERMAN)
        val report =
            RuntimeDiagnostics.report(
                runtimeId = "plugin-grounds-runtime-velocity",
                version = "0.1.1",
                platform = "velocity",
            )

        RuntimeDiagnosticsPresenter.sendPlayer(audience, report)

        val messages = audience.sent.map(PlainTextComponentSerializer.plainText()::serialize)
        assertEquals("[Grounds] Allgemein › Grounds-Laufzeit", messages.first())
        assertEquals("[Grounds] Allgemein › runtimeId=plugin-grounds-runtime-velocity", messages[1])
        assertEquals("[Grounds] Allgemein › Bibliotheken", messages[6])
        assertContains(messages, "[Grounds] Allgemein › - io.grpc:grpc-api:1.81.0")
    }

    @Test
    fun `console output remains plain and copyable`() {
        val report =
            RuntimeDiagnostics.report(
                runtimeId = "plugin-grounds-runtime-paper",
                version = "0.1.1",
                platform = "paper",
            )

        val lines = report.consoleLines()
        assertEquals("Grounds Runtime", lines.first())
        assertEquals("runtimeId=plugin-grounds-runtime-paper", lines[1])
        assertEquals("version=0.1.1", lines[2])
        assertEquals("platform=paper", lines[3])
        assertEquals("libraries:", lines[6])
        assertContains(lines, "- io.grpc:grpc-api:1.81.0")
    }
}

private class RecordingAudience(locale: Locale) : Audience {
    val sent = mutableListOf<Component>()
    private val pointers = Pointers.builder().withStatic(Identity.LOCALE, locale).build()

    override fun pointers(): Pointers = pointers

    override fun sendMessage(message: Component) {
        sent += message
    }
}
