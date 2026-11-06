package yumo.achat.core.backend

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import org.junit.Assert.*
import org.junit.Test

class GenerationModeRequestTest {
    @Test fun `image and video task requests preserve selected quality resource and idempotency`() {
        var path = ""
        var payload = ""
        var key = ""
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            path = exchange.requestURI.path
            payload = exchange.requestBody.use { String(it.readBytes()) }
            key = exchange.requestHeaders.getFirst("Idempotency-Key")
            val body = """{"code":400101,"message":"test rejection"}""".toByteArray()
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.start()
        try {
            val client = AchatBackendClient("http://127.0.0.1:${server.address.port}")
            for (modality in listOf("image", "video")) for (quality in listOf("fast", "quality")) {
                val requestKey = "$modality-$quality-request"
                assertThrows(GenerationCreationRejectedException::class.java) {
                    client.createVisualGenerationTask("token", modality, "template", quality, "uploaded-resource", requestKey)
                }
                assertEquals("/api/v1/visual-generation/$modality/tasks", path)
                assertEquals(quality, field(payload, "quality"))
                assertEquals("template", field(payload, "template_id"))
                assertEquals("uploaded-resource", field(payload, "resource_id"))
                assertEquals(requestKey, field(payload, "idempotency_key"))
                assertEquals(requestKey, key)
            }
        } finally { server.stop(0) }
    }

    private fun field(body: String, name: String): String =
        body.substringAfter("name=\"$name\"\r\n\r\n", "").substringBefore("\r\n")
}
