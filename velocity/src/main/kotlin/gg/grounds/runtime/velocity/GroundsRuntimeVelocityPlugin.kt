package gg.grounds.runtime.velocity

import com.google.inject.Inject
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ConsoleCommandSource
import com.velocitypowered.api.proxy.ProxyServer
import gg.grounds.BuildInfo
import gg.grounds.runtime.RuntimeDiagnostics
import gg.grounds.runtime.RuntimeManifestLoader
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
}
