package gg.grounds.runtime.paper

import gg.grounds.runtime.RuntimeDiagnostics
import gg.grounds.runtime.RuntimeManifestLoader
import gg.grounds.runtime.commands.RuntimeCommandEnv
import gg.grounds.runtime.commands.RuntimeCommandExecution
import gg.grounds.runtime.commands.RuntimeCommandExecutor
import gg.grounds.runtime.commands.RuntimeCommandLogger
import gg.grounds.runtime.commands.RuntimeCommandPoller
import java.util.concurrent.ExecutionException
import java.util.logging.Level
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class GroundsRuntimePaperPlugin : JavaPlugin() {
    private var commandPoller: RuntimeCommandPoller? = null

    override fun onEnable() {
        logger.info(
            "Runtime plugin started successfully (runtimeId=plugin-grounds-runtime-paper, version=${pluginMeta.version}, platform=paper, manifestPath=${RuntimeManifestLoader.defaultPath})"
        )
        commandPoller =
            RuntimeCommandPoller(
                    env = RuntimeCommandEnv.from(),
                    executor = PaperRuntimeCommandExecutor(this),
                    logger = PaperRuntimeCommandLogger(this),
                )
                .also { it.start() }
    }

    override fun onDisable() {
        commandPoller?.close()
        commandPoller = null
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

        RuntimeDiagnostics.lines(
                runtimeId = "plugin-grounds-runtime-paper",
                version = pluginMeta.version,
                platform = "paper",
            )
            .forEach(sender::sendMessage)
        return true
    }

    private class PaperRuntimeCommandExecutor(private val plugin: GroundsRuntimePaperPlugin) :
        RuntimeCommandExecutor {
        override fun execute(command: String): RuntimeCommandExecution {
            val future =
                plugin.server.scheduler.callSyncMethod(plugin) {
                    plugin.server.dispatchCommand(plugin.server.consoleSender, command)
                }
            val handled =
                try {
                    future.get()
                } catch (exception: InterruptedException) {
                    future.cancel(true)
                    Thread.currentThread().interrupt()
                    return RuntimeCommandExecution.failed("Command execution interrupted")
                } catch (exception: ExecutionException) {
                    return RuntimeCommandExecution.failed(
                        "Command execution failed (reason=${exception.cause.reason()})"
                    )
                }

            return if (handled) {
                RuntimeCommandExecution.executed("Command executed")
            } else {
                RuntimeCommandExecution.failed("Command was not handled")
            }
        }

        private fun Throwable?.reason(): String = this?.javaClass?.simpleName ?: "unknown"
    }

    private class PaperRuntimeCommandLogger(private val plugin: GroundsRuntimePaperPlugin) :
        RuntimeCommandLogger {
        override fun warn(message: String) {
            plugin.logger.warning(message)
        }

        override fun info(message: String) {
            plugin.logger.info(message)
        }

        override fun error(message: String, throwable: Throwable?) {
            plugin.logger.log(Level.SEVERE, message, throwable)
        }
    }
}
