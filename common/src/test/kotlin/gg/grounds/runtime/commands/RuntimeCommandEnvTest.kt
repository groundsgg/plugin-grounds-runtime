package gg.grounds.runtime.commands

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RuntimeCommandEnvTest {
    @Test
    fun `env is enabled when all command polling variables are present`() {
        val env = RuntimeCommandEnv.from(validEnv())

        assertIs<RuntimeCommandEnv.Enabled>(env)
        assertEquals("https://platform.grnds.io", env.forgeUrl)
        assertEquals("project-1", env.projectId)
        assertEquals("survival", env.appName)
        assertEquals("push-1", env.pushId)
        assertEquals("runtime-token", env.token)
    }

    @Test
    fun `env is disabled when push id is missing`() {
        val env = RuntimeCommandEnv.from(validEnv() - "GROUNDS_PUSH_ID")

        assertIs<RuntimeCommandEnv.Disabled>(env)
        assertEquals(RuntimeCommandDisabledReason.MISSING_PUSH_ID, env.reason)
        assertEquals("project-1", env.projectId)
        assertEquals("survival", env.appName)
    }

    private fun validEnv(): Map<String, String?> =
        mapOf(
            "GROUNDS_FORGE_URL" to "https://platform.grnds.io",
            "GROUNDS_PROJECT_ID" to "project-1",
            "GROUNDS_APP_NAME" to "survival",
            "GROUNDS_PUSH_ID" to "push-1",
            "GROUNDS_TOKEN" to "runtime-token",
        )
}
