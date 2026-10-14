package gg.grounds.runtime.paper

import gg.grounds.runtime.RuntimeDiagnostics
import gg.grounds.runtime.RuntimeDiagnosticsPresenter
import gg.grounds.runtime.RuntimeManifestLoader
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class GroundsRuntimePaperPlugin : JavaPlugin() {
    override fun onEnable() {
        logger.info(
            "Runtime plugin started successfully (runtimeId=plugin-grounds-runtime-paper, version=${pluginMeta.version}, platform=paper, manifestPath=${RuntimeManifestLoader.defaultPath})"
        )
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (!command.name.equals("grounds-runtime", ignoreCase = true)) {
            return false
        }

        val report =
            RuntimeDiagnostics.report(
                runtimeId = "plugin-grounds-runtime-paper",
                version = pluginMeta.version,
                platform = "paper",
            )
        if (sender is Player) RuntimeDiagnosticsPresenter.sendPlayer(sender, report)
        else report.consoleLines().forEach(sender::sendMessage)
        return true
    }
}
