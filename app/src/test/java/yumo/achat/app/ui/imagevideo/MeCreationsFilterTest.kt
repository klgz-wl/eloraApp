package yumo.achat.app.ui.imagevideo

import org.junit.Assert.*
import org.junit.Test
import kotlinx.coroutines.runBlocking
import yumo.achat.core.backend.VisualResource
import yumo.achat.core.backend.PagedResult

class MeCreationsFilterTest {
    private fun task(id: String, modality: String, url: String?, status: String = "succeeded") =
        TrackedGenerationTask(id, id, modality, status, url, "$modality/test", null)

    @Test fun `creations include both media and exclude missing addresses and unfinished results`() {
        val tasks = listOf(task("video", "video", "https://example.test/v.mp4"), task("image", "image", "https://example.test/i.png"),
            task("empty", "image", ""), task("null", "video", null), task("spaces", "image", "  "), task("pending", "video", "url", "processing"))
        assertEquals(listOf("video", "image"), visibleMeCreations(tasks, MeCreationFilter.Creations).map { it.taskId })
        assertEquals(listOf("video"), visibleMeCreations(tasks, MeCreationFilter.Videos).map { it.taskId })
        assertEquals(listOf("image"), visibleMeCreations(tasks, MeCreationFilter.Images).map { it.taskId })
        assertNull(MeCreationFilter.Creations.modality)
        assertEquals("video", MeCreationFilter.Videos.modality)
        assertEquals("image", MeCreationFilter.Images.modality)
    }

    @Test fun `empty addresses are skipped across pages before deciding empty`() = runBlocking {
        val requests = mutableListOf<Pair<String?, Int>>()
        val result = readMeCreationPage(MeCreationFilter.Images, 1, "Result") { modality, page ->
            requests += modality to page
            PagedResult(listOf(VisualResource("result-$page", null, "generated", "image",
                url = if (page == 1) "" else "https://example.test/image.png", thumbnailUrl = null,
                mimeType = "image/png", width = 10, height = 10, durationSeconds = 0)), page, 1, 2)
        }
        assertEquals(listOf("image" to 1, "image" to 2), requests)
        assertEquals(listOf("result-2"), result.items.map { it.taskId })
        assertFalse(result.hasMore)
    }
}
