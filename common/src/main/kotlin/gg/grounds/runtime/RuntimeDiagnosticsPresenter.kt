package gg.grounds.runtime

import gg.grounds.i18n.SystemCategory
import gg.grounds.i18n.SystemLevel
import gg.grounds.i18n.SystemMessageKey
import gg.grounds.i18n.SystemMessages
import gg.grounds.i18n.Translations
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component

enum class RuntimeDiagnosticMessage(override val id: String, override val level: SystemLevel) :
    SystemMessageKey {
    HEADING("diagnostics.heading", SystemLevel.INFO),
    LIBRARIES("diagnostics.libraries", SystemLevel.INFO),
}

object RuntimeDiagnosticsPresenter {
    private val messages =
        Translations.forBundle("gg.grounds.runtime.messages", javaClass.classLoader)

    fun sendPlayer(audience: Audience, report: RuntimeDiagnosticsReport) {
        messages.send(audience, RuntimeDiagnosticMessage.HEADING)
        report.fields.forEach { line ->
            audience.sendMessage(
                SystemMessages.format(
                    Component.text(line),
                    category = SystemCategory.GENERAL,
                    locale = messages.localeOf(audience),
                    level = SystemLevel.INFO,
                )
            )
        }
        messages.send(audience, RuntimeDiagnosticMessage.LIBRARIES)
        report.libraries.forEach { coordinate ->
            audience.sendMessage(
                SystemMessages.format(
                    Component.text("- $coordinate"),
                    category = SystemCategory.GENERAL,
                    locale = messages.localeOf(audience),
                    level = SystemLevel.INFO,
                )
            )
        }
    }
}
