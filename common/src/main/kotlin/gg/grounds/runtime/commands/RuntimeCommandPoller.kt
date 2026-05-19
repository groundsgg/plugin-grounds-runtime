package gg.grounds.runtime.commands

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean

interface RuntimeCommandExecutor {
    fun execute(command: String): RuntimeCommandExecution
}

data class RuntimeCommandExecution(val status: RuntimeCommandStatus, val message: String) {
    companion object {
        fun executed(message: String): RuntimeCommandExecution =
            RuntimeCommandExecution(RuntimeCommandStatus.EXECUTED, message)

        fun failed(message: String): RuntimeCommandExecution =
            RuntimeCommandExecution(RuntimeCommandStatus.FAILED, message)
    }
}

interface RuntimeCommandLogger {
    fun warn(message: String)

    fun info(message: String)

    fun error(message: String, throwable: Throwable? = null)
}

class RuntimeCommandPoller(
    private val env: RuntimeCommandEnv,
    private val client: RuntimeCommandService? =
        (env as? RuntimeCommandEnv.Enabled)?.let { RuntimeCommandClient(it) },
    private val executor: RuntimeCommandExecutor,
    private val logger: RuntimeCommandLogger,
    private val worker: ExecutorService =
        Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "grounds-runtime-command-poller").apply { isDaemon = true }
        },
    private val backoffMillis: Long = 5_000,
    private val sleeper: (Long) -> Unit = { millis -> Thread.sleep(millis) },
) : AutoCloseable {
    private val started = AtomicBoolean(false)
    private val warningLogged = AtomicBoolean(false)
    private var future: Future<*>? = null

    fun start() {
        when (env) {
            is RuntimeCommandEnv.Disabled -> logDisabledWarning()
            is RuntimeCommandEnv.Enabled -> {
                if (started.compareAndSet(false, true)) {
                    logger.info(
                        "Runtime command polling started successfully (deploymentName=${env.appName}, projectId=${env.projectId}, pushId=${env.pushId.logValue()})"
                    )
                    future = worker.submit { pollLoop() }
                }
            }
        }
    }

    fun pollOnce() {
        when (env) {
            is RuntimeCommandEnv.Disabled -> logDisabledWarning()
            is RuntimeCommandEnv.Enabled -> pollEnabledOnce()
        }
    }

    override fun close() {
        started.set(false)
        future?.cancel(true)
        worker.shutdownNow()
    }

    private fun pollLoop() {
        while (started.get() && !Thread.currentThread().isInterrupted) {
            pollEnabledOnce()
        }
    }

    private fun pollEnabledOnce() {
        val enabledEnv = env as? RuntimeCommandEnv.Enabled ?: return
        val service = client ?: return
        val lease =
            try {
                service.leaseCommand()
            } catch (exception: InterruptedException) {
                Thread.currentThread().interrupt()
                return
            } catch (exception: Exception) {
                logger.error(
                    "Failed to lease runtime command (deploymentName=${enabledEnv.appName}, projectId=${enabledEnv.projectId}, pushId=${enabledEnv.pushId.logValue()}, reason=${exception.reason()})",
                    exception,
                )
                sleepAfterLeaseFailure()
                return
            } ?: return

        val execution =
            try {
                executor.execute(lease.command)
            } catch (exception: Exception) {
                RuntimeCommandExecution.failed(
                    "Command execution failed (reason=${exception.reason()})"
                )
            }
        if (Thread.currentThread().isInterrupted) return

        try {
            service.postResult(
                commandId = lease.id,
                result =
                    RuntimeCommandResult(
                        leaseToken = lease.leaseToken,
                        status = execution.status,
                        message = execution.message,
                    ),
            )
            logger.info(
                "Runtime command result posted successfully (deploymentName=${enabledEnv.appName}, projectId=${enabledEnv.projectId}, pushId=${enabledEnv.pushId.logValue()}, commandId=${lease.id}, status=${execution.status.wireValue})"
            )
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (exception: Exception) {
            logger.error(
                "Failed to post runtime command result (deploymentName=${enabledEnv.appName}, projectId=${enabledEnv.projectId}, pushId=${enabledEnv.pushId.logValue()}, commandId=${lease.id}, reason=${exception.reason()})",
                exception,
            )
        }
    }

    private fun logDisabledWarning() {
        val disabledEnv = env as? RuntimeCommandEnv.Disabled ?: return
        if (warningLogged.compareAndSet(false, true)) {
            logger.warn(
                "Runtime command polling disabled (reason=${disabledEnv.reason.logValue}, deploymentName=${disabledEnv.appName.logValue()}, projectId=${disabledEnv.projectId.logValue()}, pushId=${disabledEnv.pushId.logValue()})"
            )
        }
    }

    private fun sleepAfterLeaseFailure() {
        try {
            sleeper(backoffMillis)
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    private fun String?.logValue(): String = this ?: "unavailable"

    private fun Exception.reason(): String =
        this.message?.replace(' ', '_')?.take(80) ?: this::class.simpleName ?: "unknown"
}
