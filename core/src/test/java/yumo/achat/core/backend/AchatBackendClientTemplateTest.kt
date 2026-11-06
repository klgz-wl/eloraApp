package yumo.achat.core.backend

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AchatBackendClientTemplateTest {
    @Test
    fun `video templates send hot sort and omit empty category`() = withTemplateServer { client, query ->
        client.templates(
            token = "token-1",
            modality = "video",
            sortBy = "hot",
            categoryId = null,
        )

        assertTrue(query.get().contains("sort_by=hot"))
        assertFalse(query.get().contains("category_id"))
        assertTrue(query.get().contains("home_featured=false"))
    }

    @Test
    fun `image templates send latest sort and preserve category`() = withTemplateServer { client, query ->
        client.templates(
            token = "token-2",
            modality = "image",
            sortBy = "latest",
            categoryId = "portrait",
        )

        assertTrue(query.get().contains("sort_by=latest"))
        assertTrue(query.get().contains("category_id=portrait"))
        assertFalse(query.get().contains("home_featured"))
    }

    @Test
    fun `home feed keeps home featured on paginated requests`() = withTemplateServer { client, query ->
        client.templates("token", "video", "hot", page = 2, homeFeatured = true)
        assertTrue(query.get().contains("home_featured=true"))
        assertTrue(query.get().contains("page=2"))
        client.templates("token", "video", "hot", categoryId = "category one", page = 3, homeFeatured = false)
        assertTrue(query.get().contains("home_featured=false"))
        assertTrue(query.get().contains("category_id=category+one"))
        assertTrue(query.get().contains("page=3"))
    }

    @Test
    fun `generated creations filter modality while preserving pagination`() = withTemplateServer { client, query ->
        client.visualResources("token", page = 2, modality = "video", resourceType = "generated")
        assertTrue(query.get().contains("modality=video"))
        assertTrue(query.get().contains("resource_type=generated"))
        assertTrue(query.get().contains("page=2"))
        client.visualResources("token", modality = "image", resourceType = "generated")
        assertTrue(query.get().contains("modality=image"))
        client.visualResources("token", resourceType = "generated")
        assertFalse(query.get().contains("modality="))
    }

    private fun withTemplateServer(
        block: (AchatBackendClient, AtomicReference<String>) -> Unit,
    ) {
        val query = AtomicReference("")
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/api/v1/visual-generation/") { exchange ->
            query.set(exchange.requestURI.rawQuery.orEmpty())
            val bytes = """{"code":0,"data":{"items":[]}}""".toByteArray()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        try {
            block(AchatBackendClient("http://127.0.0.1:${server.address.port}"), query)
        } finally {
            server.stop(0)
        }
    }
}
