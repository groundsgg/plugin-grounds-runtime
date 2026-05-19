package gg.grounds.runtime.commands

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.LinkedBlockingQueue
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RuntimeCommandClientTest {
    private val requests = LinkedBlockingQueue<RecordedRequest>()
    private val server =
        HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).also { httpServer ->
            httpServer.createContext("/") { exchange -> handle(exchange) }
            httpServer.start()
        }

    private var leaseResponse = """{"command":null}"""
    private var resultResponseStatus = 204

    @AfterTest
    fun stopServer() {
        server.stop(0)
    }

    @Test
    fun `lease request includes deployment scope and bearer token`() {
        val client = client()

        val lease = client.leaseCommand()

        assertNull(lease)
        val request = requests.take()
        assertEquals("GET", request.method)
        assertEquals(
            "/v1/runtime/deployments/survival/commands/lease?projectId=project-1&pushId=push-9&waitMs=25000",
            request.pathWithQuery,
        )
        assertEquals("Bearer runtime-token", request.authorization)
    }

    @Test
    fun `lease response parses command payload`() {
        leaseResponse =
            """
            {
              "command": {
                "id": "command-1",
                "command": "say hello",
                "queuedAt": "2026-05-19T10:15:30Z",
                "leaseToken": "lease-1"
              }
            }
            """
                .trimIndent()
        val client = client()

        val lease = client.leaseCommand()

        assertNotNull(lease)
        assertEquals("command-1", lease.id)
        assertEquals("say hello", lease.command)
        assertEquals("2026-05-19T10:15:30Z", lease.queuedAt)
        assertEquals("lease-1", lease.leaseToken)
    }

    @Test
    fun `result request posts lease token status and escaped message`() {
        val client = client()

        client.postResult(
            commandId = "command-1",
            result =
                RuntimeCommandResult(
                    leaseToken = "lease-1",
                    status = RuntimeCommandStatus.FAILED,
                    message = "Command failed: \"no permission\"",
                ),
        )

        val request = requests.take()
        assertEquals("POST", request.method)
        assertEquals(
            "/v1/runtime/deployments/survival/commands/command-1/result",
            request.pathWithQuery,
        )
        assertEquals("Bearer runtime-token", request.authorization)
        assertEquals(
            """{"leaseToken":"lease-1","status":"failed","message":"Command failed: \"no permission\""}""",
            request.body,
        )
    }

    private fun client() =
        RuntimeCommandClient(
            env =
                RuntimeCommandEnv.Enabled(
                    forgeUrl = "http://127.0.0.1:${server.address.port}",
                    projectId = "project-1",
                    appName = "survival",
                    pushId = "push-9",
                    token = "runtime-token",
                )
        )

    private fun handle(exchange: HttpExchange) {
        val body = exchange.requestBody.bufferedReader().use { it.readText() }
        requests.add(
            RecordedRequest(
                method = exchange.requestMethod,
                pathWithQuery = exchange.requestURI.toASCIIString(),
                authorization = exchange.requestHeaders.getFirst("Authorization"),
                body = body,
            )
        )

        val response =
            if (exchange.requestMethod == "GET") {
                exchange.sendResponseHeaders(200, leaseResponse.toByteArray().size.toLong())
                leaseResponse
            } else {
                exchange.sendResponseHeaders(resultResponseStatus, -1)
                ""
            }
        if (response.isNotEmpty()) {
            exchange.responseBody.use { it.write(response.toByteArray()) }
        } else {
            exchange.close()
        }
    }

    private data class RecordedRequest(
        val method: String,
        val pathWithQuery: String,
        val authorization: String?,
        val body: String,
    )
}
