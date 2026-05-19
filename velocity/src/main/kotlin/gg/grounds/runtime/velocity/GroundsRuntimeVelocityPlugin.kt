package gg.grounds.runtime.velocity

import com.google.inject.Inject
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ConsoleCommandSource
import com.velocitypowered.api.proxy.ProxyServer
import gg.grounds.BuildInfo
import gg.grounds.runtime.RuntimeDiagnostics
import gg.grounds.runtime.RuntimeManifestLoader
import gg.grounds.runtime.commands.RuntimeCommandEnv
import gg.grounds.runtime.commands.RuntimeCommandExecution
import gg.grounds.runtime.commands.RuntimeCommandExecutor
import gg.grounds.runtime.commands.RuntimeCommandLogger
import gg.grounds.runtime.commands.RuntimeCommandPoller
import java.util.concurrent.ExecutionException
import net.kyori.adventure.text.Component
import org.slf4j.Logger

@Plugin(
    id = "plugin-grounds-runtime",
    name = "Grounds Plugin Runtime",
    version = BuildInfo.VERSION,
    description = "Shared runtime libraries for Grounds Velocity plugins",
    authors = ["Grounds Development Team and contributors"],
    url = "https://github.com/groundsgg/plugin-grounds-runtime",
)
class GroundsRuntimeVelocityPlugin
@Inject
constructor(private val proxyServer: ProxyServer, private val logger: Logger) {
    private var commandPoller: RuntimeCommandPoller? = null

    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {
        proxyServer.commandManager.register(
            proxyServer.commandManager.metaBuilder("grounds-runtime").build(),
            RuntimeInfoCommand(),
        )

        logger.info(
            "Runtime plugin started successfully (runtimeId=plugin-grounds-runtime-velocity, version={}, platform=velocity, manifestPath={})",
            BuildInfo.VERSION,
            RuntimeManifestLoader.defaultPath,
        )

        commandPoller =
            RuntimeCommandPoller(
                    env = RuntimeCommandEnv.from(),
                    executor = VelocityRuntimeCommandExecutor(proxyServer),
                    logger = VelocityRuntimeCommandLogger(logger),
                )
                .also { it.start() }
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        commandPoller?.close()
        commandPoller = null
    }

    private class RuntimeInfoCommand : SimpleCommand {
        override fun execute(invocation: SimpleCommand.Invocation) {
            RuntimeDiagnostics.lines(
                    runtimeId = "plugin-grounds-runtime-velocity",
                    version = BuildInfo.VERSION,
                    platform = "velocity",
                )
                .forEach { line -> invocation.source().sendMessage(Component.text(line)) }
        }

        override fun hasPermission(invocation: SimpleCommand.Invocation): Boolean {
            val source = invocation.source()
            return source is ConsoleCommandSource || source.hasPermission(PERMISSION)
        }

        companion object {
            private const val PERMISSION = "grounds.runtime.info"
        }
    }

    private class VelocityRuntimeCommandExecutor(private val proxyServer: ProxyServer) :
        RuntimeCommandExecutor {
        override fun execute(command: String): RuntimeCommandExecution {
            val future =
                proxyServer.commandManager.executeAsync(proxyServer.consoleCommandSource, command)
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

    private class VelocityRuntimeCommandLogger(private val logger: Logger) : RuntimeCommandLogger {
        override fun warn(message: String) {
            logger.warn(message)
        }

        override fun info(message: String) {
            logger.info(message)
        }

        override fun error(message: String, throwable: Throwable?) {
            logger.error(message, throwable)
        }
    }
}
