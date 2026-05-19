package gg.grounds.runtime.commands

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

interface RuntimeCommandService {
    fun leaseCommand(): RuntimeCommandLease?

    fun postResult(commandId: String, result: RuntimeCommandResult)
}

data class RuntimeCommandLease(
    val id: String,
    val command: String,
    val queuedAt: String,
    val leaseToken: String,
)

data class RuntimeCommandResult(
    val leaseToken: String,
    val status: RuntimeCommandStatus,
    val message: String,
)

enum class RuntimeCommandStatus(val wireValue: String) {
    EXECUTED("executed"),
    FAILED("failed"),
}

class RuntimeCommandClient(
    private val env: RuntimeCommandEnv.Enabled,
    private val httpClient: HttpClient =
        HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(),
) : RuntimeCommandService {
    override fun leaseCommand(): RuntimeCommandLease? {
        val response =
            httpClient.send(
                requestBuilder(leaseUri()).GET().build(),
                HttpResponse.BodyHandlers.ofString(),
            )
        requireSuccessful(response, "lease runtime command")
        return RuntimeCommandJson.parseLease(response.body())
    }

    override fun postResult(commandId: String, result: RuntimeCommandResult) {
        val response =
            httpClient.send(
                requestBuilder(resultUri(commandId))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(RuntimeCommandJson.result(result)))
                    .build(),
                HttpResponse.BodyHandlers.ofString(),
            )
        requireSuccessful(response, "post runtime command result")
    }

    private fun requestBuilder(uri: URI): HttpRequest.Builder =
        HttpRequest.newBuilder(uri)
            .timeout(Duration.ofSeconds(35))
            .header("Authorization", "Bearer ${env.token}")

    private fun leaseUri(): URI =
        endpoint(
            "/v1/runtime/deployments/${encode(env.appName)}/commands/lease",
            buildList {
                add("projectId=${encode(env.projectId)}")
                add("pushId=${encode(env.pushId)}")
                add("waitMs=25000")
            },
        )

    private fun resultUri(commandId: String): URI =
        endpoint(
            "/v1/runtime/deployments/${encode(env.appName)}/commands/${encode(commandId)}/result",
            emptyList(),
        )

    private fun endpoint(path: String, query: List<String>): URI {
        val base = env.forgeUrl.trimEnd('/')
        val queryString = query.takeIf { it.isNotEmpty() }?.joinToString("&")?.let { "?$it" } ?: ""
        return URI.create("$base$path$queryString")
    }

    private fun requireSuccessful(response: HttpResponse<String>, action: String) {
        if (response.statusCode() !in 200..299) {
            throw RuntimeCommandHttpException(
                "Failed to $action (statusCode=${response.statusCode()})"
            )
        }
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20")
}

class RuntimeCommandHttpException(message: String) : RuntimeException(message)

private object RuntimeCommandJson {
    fun parseLease(json: String): RuntimeCommandLease? {
        if (Regex(""""command"\s*:\s*null""").containsMatchIn(json)) {
            return null
        }

        return RuntimeCommandLease(
            id = requiredString(json, "id"),
            command = requiredString(json, "command"),
            queuedAt = requiredString(json, "queuedAt"),
            leaseToken = requiredString(json, "leaseToken"),
        )
    }

    fun result(result: RuntimeCommandResult): String = buildString {
        append("{")
        append(""""leaseToken":"${escape(result.leaseToken)}",""")
        append(""""status":"${result.status.wireValue}",""")
        append(""""message":"${escape(result.message)}"""")
        append("}")
    }

    private fun requiredString(json: String, field: String): String {
        val match =
            Regex(""""${Regex.escape(field)}"\s*:\s*"((?:\\.|[^"\\])*)"""").find(json)
                ?: throw IllegalArgumentException("Runtime command field missing (field=$field)")
        return unescape(match.groupValues[1])
    }

    private fun escape(value: String): String = buildString {
        value.forEach { character ->
            when (character) {
                '\\' -> append("""\\""")
                '"' -> append("\\\"")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> {
                    if (character.code < 0x20) {
                        append("\\u")
                        append(character.code.toString(16).padStart(4, '0'))
                    } else {
                        append(character)
                    }
                }
            }
        }
    }

    private fun unescape(value: String): String = buildString {
        var index = 0
        while (index < value.length) {
            val character = value[index]
            if (character != '\\' || index == value.lastIndex) {
                append(character)
                index += 1
                continue
            }

            val escaped = value[index + 1]
            when (escaped) {
                '"',
                '\\',
                '/' -> append(escaped)
                'b' -> append('\b')
                'f' -> append('\u000C')
                'n' -> append('\n')
                'r' -> append('\r')
                't' -> append('\t')
                'u' -> {
                    val hex = value.substring(index + 2, index + 6)
                    append(hex.toInt(16).toChar())
                    index += 4
                }
                else -> append(escaped)
            }
            index += 2
        }
    }
}
