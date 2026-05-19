package gg.grounds.runtime.commands

import kotlin.test.Test
import kotlin.test.assertEquals

class RuntimeCommandPollerTest {
    @Test
    fun `disabled poller logs one startup warning and does not lease commands`() {
        val client = RecordingClient()
        val logger = RecordingLogger()
        val poller =
            RuntimeCommandPoller(
                env = RuntimeCommandEnv.Disabled(RuntimeCommandDisabledReason.MISSING_TOKEN),
                client = client,
                executor = RecordingExecutor(RuntimeCommandExecution.executed("unused")),
                logger = logger,
            )

        poller.start()
        poller.start()

        assertEquals(0, client.leaseRequests)
        assertEquals(
            listOf(
                "Runtime command polling disabled (reason=missing_token, deploymentName=unavailable, projectId=unavailable, pushId=unavailable)"
            ),
            logger.warnings,
        )
    }

    @Test
    fun `enabled poller executes leased command and posts executed result`() {
        val command =
            RuntimeCommandLease(
                id = "command-1",
                command = "say hello",
                queuedAt = "2026-05-19T10:15:30Z",
                leaseToken = "lease-1",
            )
        val client = RecordingClient(command)
        val executor = RecordingExecutor(RuntimeCommandExecution.executed("Command executed"))
        val poller =
            RuntimeCommandPoller(
                env = enabledEnv(),
                client = client,
                executor = executor,
                logger = RecordingLogger(),
            )

        poller.pollOnce()

        assertEquals(listOf("say hello"), executor.commands)
        assertEquals(
            listOf(
                RuntimeCommandResult(
                    leaseToken = "lease-1",
                    status = RuntimeCommandStatus.EXECUTED,
                    message = "Command executed",
                )
            ),
            client.results,
        )
    }

    @Test
    fun `enabled poller posts failed result when execution fails`() {
        val command =
            RuntimeCommandLease(
                id = "command-1",
                command = "bad command",
                queuedAt = "2026-05-19T10:15:30Z",
                leaseToken = "lease-1",
            )
        val client = RecordingClient(command)
        val executor = RecordingExecutor(RuntimeCommandExecution.failed("Command was not handled"))
        val poller =
            RuntimeCommandPoller(
                env = enabledEnv(),
                client = client,
                executor = executor,
                logger = RecordingLogger(),
            )

        poller.pollOnce()

        assertEquals(
            listOf(
                RuntimeCommandResult(
                    leaseToken = "lease-1",
                    status = RuntimeCommandStatus.FAILED,
                    message = "Command was not handled",
                )
            ),
            client.results,
        )
    }

    @Test
    fun `enabled poller backs off when command lease fails`() {
        val client = FailingLeaseClient()
        val backoffs = mutableListOf<Long>()
        val poller =
            RuntimeCommandPoller(
                env = enabledEnv(),
                client = client,
                executor = RecordingExecutor(RuntimeCommandExecution.executed("unused")),
                logger = RecordingLogger(),
                backoffMillis = 123,
                sleeper = backoffs::add,
            )

        poller.pollOnce()

        assertEquals(1, client.leaseRequests)
        assertEquals(listOf(123L), backoffs)
    }

    private fun enabledEnv() =
        RuntimeCommandEnv.Enabled(
            forgeUrl = "https://platform.grnds.io",
            projectId = "project-1",
            appName = "survival",
            pushId = "push-1",
            token = "runtime-token",
        )

    private class RecordingClient(private val nextLease: RuntimeCommandLease? = null) :
        RuntimeCommandService {
        var leaseRequests = 0
        val results = mutableListOf<RuntimeCommandResult>()

        override fun leaseCommand(): RuntimeCommandLease? {
            leaseRequests += 1
            return nextLease
        }

        override fun postResult(commandId: String, result: RuntimeCommandResult) {
            results.add(result)
        }
    }

    private class RecordingExecutor(private val result: RuntimeCommandExecution) :
        RuntimeCommandExecutor {
        val commands = mutableListOf<String>()

        override fun execute(command: String): RuntimeCommandExecution {
            commands.add(command)
            return result
        }
    }

    private class FailingLeaseClient : RuntimeCommandService {
        var leaseRequests = 0

        override fun leaseCommand(): RuntimeCommandLease? {
            leaseRequests += 1
            error("forge unavailable")
        }

        override fun postResult(commandId: String, result: RuntimeCommandResult) = Unit
    }

    private class RecordingLogger : RuntimeCommandLogger {
        val warnings = mutableListOf<String>()

        override fun warn(message: String) {
            warnings.add(message)
        }

        override fun info(message: String) = Unit

        override fun error(message: String, throwable: Throwable?) = Unit
    }
}
