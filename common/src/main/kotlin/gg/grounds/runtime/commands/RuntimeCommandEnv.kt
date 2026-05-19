package gg.grounds.runtime.commands

sealed class RuntimeCommandEnv {
    data class Enabled(
        val forgeUrl: String,
        val projectId: String,
        val appName: String,
        val pushId: String,
        val token: String,
    ) : RuntimeCommandEnv()

    data class Disabled(
        val reason: RuntimeCommandDisabledReason,
        val projectId: String? = null,
        val appName: String? = null,
        val pushId: String? = null,
    ) : RuntimeCommandEnv()

    companion object {
        fun from(env: Map<String, String?> = System.getenv()): RuntimeCommandEnv {
            val forgeUrl = env.requiredEnv("GROUNDS_FORGE_URL")
            val projectId = env.requiredEnv("GROUNDS_PROJECT_ID")
            val appName = env.requiredEnv("GROUNDS_APP_NAME")
            val pushId = env.requiredEnv("GROUNDS_PUSH_ID")
            val token = env.requiredEnv("GROUNDS_TOKEN")

            return when {
                forgeUrl == null ->
                    Disabled(
                        reason = RuntimeCommandDisabledReason.MISSING_FORGE_URL,
                        projectId = projectId,
                        appName = appName,
                        pushId = pushId,
                    )
                projectId == null ->
                    Disabled(
                        reason = RuntimeCommandDisabledReason.MISSING_PROJECT_ID,
                        projectId = projectId,
                        appName = appName,
                        pushId = pushId,
                    )
                appName == null ->
                    Disabled(
                        reason = RuntimeCommandDisabledReason.MISSING_APP_NAME,
                        projectId = projectId,
                        appName = appName,
                        pushId = pushId,
                    )
                pushId == null ->
                    Disabled(
                        reason = RuntimeCommandDisabledReason.MISSING_PUSH_ID,
                        projectId = projectId,
                        appName = appName,
                        pushId = pushId,
                    )
                token == null ->
                    Disabled(
                        reason = RuntimeCommandDisabledReason.MISSING_TOKEN,
                        projectId = projectId,
                        appName = appName,
                        pushId = pushId,
                    )
                else ->
                    Enabled(
                        forgeUrl = forgeUrl,
                        projectId = projectId,
                        appName = appName,
                        pushId = pushId,
                        token = token,
                    )
            }
        }

        private fun Map<String, String?>.requiredEnv(name: String): String? =
            this[name]?.trim()?.takeUnless { it.isBlank() }
    }
}

enum class RuntimeCommandDisabledReason(val logValue: String) {
    MISSING_FORGE_URL("missing_forge_url"),
    MISSING_PROJECT_ID("missing_project_id"),
    MISSING_APP_NAME("missing_app_name"),
    MISSING_PUSH_ID("missing_push_id"),
    MISSING_TOKEN("missing_token"),
}
