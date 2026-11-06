package yumo.achat.app.ui.imagevideo

import android.os.ParcelFileDescriptor
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import yumo.achat.core.backend.VisualCategory
import yumo.achat.core.backend.VisualTemplate
import yumo.achat.app.R

class VideoTemplateCatalogTest {
    @get:Rule val composeRule = createComposeRule()
    private val categories = listOf(VisualCategory("avatar-id", "Avatar VFX", 0), VisualCategory("commercial-id", "Commercial", 1))
    private fun templates(): List<VisualTemplate> {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        return listOf("male", "fragrance", "fire", "roses", "portrait", "cyber").mapIndexed { index, name ->
            val image = File(instrumentation.targetContext.cacheDir, "catalog-$name.png")
            instrumentation.context.assets.open("elora-video/$name.png").use { input -> image.outputStream().use(input::copyTo) }
            VisualTemplate("id-$index", "avatar-id", "Avatar VFX", "Template $name", "https://example.com/$name.mp4",
                image.toURI().toString(), "video/mp4", 600, 1000, 20, 5, 10, 20)
        }
    }

    @Test fun homeFeedSwipesBothDirectionsAndKeepsHomeNavigationSelected() {
        val data = templates().take(2).map { it.copy(fileUrl = it.previewUrl, mimeType = "image/png") }
        var picked = ""
        var destination = -1
        composeRule.setContent {
            HomeVideoFeedScreen(data, 2, false, null, false, false, null, onLoadMore = {}, onRetry = {},
                onUse = { picked = it.id }, onExpose = { _, _ -> }, onBalance = { destination = 2 },
                onNavigation = { destination = it })
        }
        composeRule.onNodeWithText("Trending Templates").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("HOME").assertIsSelected()
        composeRule.onNodeWithContentDescription("Use Template male template").assertIsDisplayed()
        composeRule.onNodeWithTag("home-video-pager").performTouchInput { swipeUp() }
        composeRule.onNodeWithContentDescription("Use Template fragrance template").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Use Template fragrance template").performClick()
        composeRule.runOnIdle { assertEquals("id-1", picked) }
        composeRule.onNodeWithTag("home-video-pager").performTouchInput { swipeDown() }
        composeRule.onNodeWithContentDescription("Use Template male template").assertIsDisplayed()
        composeRule.onNodeWithTag("home-video-media-id-0").performClick()
        composeRule.runOnIdle { assertEquals("id-0", picked) }
        composeRule.onNodeWithContentDescription("Play template").assertDoesNotExist()
        composeRule.waitForIdle()
        val screenshot = InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("screencap -p /sdcard/Download/elora-home-feed.png")
        ParcelFileDescriptor.AutoCloseInputStream(screenshot).use { it.readBytes() }
        composeRule.onNodeWithContentDescription("VIDEO").performClick()
        composeRule.runOnIdle { assertEquals(0, destination) }
        composeRule.onNodeWithContentDescription("IMAGE").performClick()
        composeRule.runOnIdle { assertEquals(1, destination) }
        composeRule.onNodeWithText("AI Studio").assert(hasClickAction().not())
        composeRule.runOnIdle { assertEquals(1, destination) }
    }

    @Test fun homeMediaOpensUploadPreviewAndReturnsHome() {
        val template = templates().first().let { it.copy(fileUrl = it.previewUrl, mimeType = "image/png") }
        var selected by mutableStateOf<VisualTemplate?>(null)
        composeRule.setContent {
            if (selected == null) HomeVideoFeedScreen(listOf(template), 2, false, null, false, false, null,
                onLoadMore = {}, onRetry = {}, onUse = { selected = it }, onExpose = { _, _ -> }, onBalance = {}, onNavigation = {})
            else UploadTemplatePreviewScreen(selected!!.name, selected!!.toPreviewMedia(), 0,
                true, {}, { selected = null })
        }
        composeRule.onNodeWithTag("home-video-media-id-0").performClick()
        composeRule.onNodeWithText("Upload Photo").assertIsDisplayed()
        composeRule.onNodeWithText("Trending Templates").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Back to templates").performClick()
        composeRule.onNodeWithText("Trending Templates").assertIsDisplayed()
    }

    @Test fun homeMediaRetryReceivesClickBeforeParentGenerationAction() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val delayed = File(instrumentation.targetContext.cacheDir, "home-delayed-cover.png")
        delayed.delete()
        val template = templates().first().copy(fileUrl = delayed.toURI().toString(), previewUrl = "", mimeType = "image/png")
        val retryText = instrumentation.targetContext.getString(R.string.template_feed_retry)
        var used = 0
        composeRule.setContent {
            HomeVideoFeedScreen(listOf(template), 0, false, null, false, false, null,
                onLoadMore = {}, onRetry = {}, onUse = { used++ }, onExpose = { _, _ -> }, onBalance = {}, onNavigation = {})
        }
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithText(retryText, substring = true).fetchSemanticsNodes().isNotEmpty() }
        instrumentation.context.assets.open("elora-video/male.png").use { input -> delayed.outputStream().use(input::copyTo) }
        composeRule.onNodeWithText(retryText, substring = true).performClick()
        composeRule.waitUntil(5_000) { composeRule.onAllNodesWithText(retryText, substring = true).fetchSemanticsNodes().isEmpty() }
        composeRule.onNodeWithTag("home-video-media-id-0").assertHasClickAction()
        composeRule.runOnIdle { assertEquals(0, used) }
    }

    @Test fun designFiltersCardsAndNavigationUseRealState() {
        val data = templates()
        var selected by mutableIntStateOf(0)
        var picked = ""
        var navigated = -1
        composeRule.setContent {
            TemplateCatalogScreen(data, categories, selected, 2, false, null, false, false, null,
                onTabSelect = { selected = it }, onTemplateSelect = { template, _ -> picked = template.id },
                onExpose = { _, _ -> }, onLoadMore = {}, onRetry = {}, onBalance = { navigated = 2 }, onNavigation = { navigated = it })
        }
        composeRule.onNodeWithContentDescription("Video templates").assertIsDisplayed()
        composeRule.onNodeWithText("Elora").assertIsDisplayed()
        composeRule.onNodeWithText("All").assertIsSelected()
        composeRule.onNodeWithContentDescription("VIDEO").assertIsSelected()
        composeRule.onNodeWithContentDescription("Template male").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Template fragrance").assertIsDisplayed()
        composeRule.waitForIdle()
        val screenshot = InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("screencap -p /sdcard/Download/elora-video-catalog.png")
        ParcelFileDescriptor.AutoCloseInputStream(screenshot).use { it.readBytes() }
        composeRule.onNodeWithText("Avatar VFX").performClick().assertIsSelected()
        composeRule.runOnIdle { assertEquals(1, selected); assertEquals("avatar-id", templateCatalogCategoryId(categories, selected)) }
        composeRule.onNodeWithContentDescription("Template male").performClick()
        composeRule.runOnIdle { assertEquals("id-0", picked) }
        composeRule.onNodeWithContentDescription("IMAGE").performClick()
        composeRule.runOnIdle { assertEquals(1, navigated) }
        composeRule.onNodeWithContentDescription("ME").performClick()
        composeRule.runOnIdle { assertEquals(3, navigated) }
        composeRule.onNodeWithContentDescription("HOME").performClick()
        composeRule.runOnIdle { assertEquals(4, navigated) }
        composeRule.onNodeWithContentDescription("Balance: 2 diamonds").performClick()
        composeRule.runOnIdle { assertEquals(2, navigated) }
    }

    @Test fun imageLayoutUsesImageSelectionAndPreservesImageTemplate() {
        val data = templates().mapIndexed { index, template ->
            template.copy(id = "image-$index", mimeType = "image/png", fileUrl = template.previewUrl)
        }
        var section by mutableStateOf(TemplateSection.Image)
        var selected by mutableIntStateOf(0)
        var picked: VisualTemplate? = null
        val imageCategories = listOf(VisualCategory("image-category", "Portraits", 0))
        composeRule.setContent {
            TemplateCatalogScreen(data, imageCategories, selected, 2, false, null, false, false, null,
                onTabSelect = { selected = it }, onTemplateSelect = { item, _ -> picked = item },
                onExpose = { _, _ -> }, onLoadMore = {}, onRetry = {}, onBalance = {},
                onNavigation = { if (it == 0 || it == 1) { section = if (it == 1) TemplateSection.Image else TemplateSection.Video; selected = 0 } },
                section = section)
        }
        composeRule.onNodeWithContentDescription("Image templates").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("IMAGE").assertIsSelected()
        composeRule.onNodeWithText("All").assertIsSelected()
        composeRule.onAllNodesWithText("20S").assertCountEquals(0)
        composeRule.onNodeWithContentDescription("Template male").assertIsDisplayed()
        composeRule.waitForIdle()
        val screenshot = InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("screencap -p /sdcard/Download/elora-image-catalog.png")
        ParcelFileDescriptor.AutoCloseInputStream(screenshot).use { it.readBytes() }
        composeRule.onNodeWithText("Portraits").performClick().assertIsSelected()
        composeRule.runOnIdle { assertEquals("image-category", templateCatalogCategoryId(imageCategories, selected)) }
        composeRule.onNodeWithContentDescription("Template male").performClick()
        composeRule.runOnIdle { assertEquals("image/png", picked?.mimeType); assertEquals("image-0", picked?.id) }
        composeRule.onNodeWithContentDescription("VIDEO").performClick()
        composeRule.onNodeWithContentDescription("Video templates").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("VIDEO").assertIsSelected()
        composeRule.onNodeWithText("All").assertIsSelected()
        composeRule.onNodeWithContentDescription("IMAGE").performClick()
        composeRule.onNodeWithContentDescription("Image templates").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("IMAGE").assertIsSelected()
    }

    @Test fun nearEndRequestsMoreAndExposureIsDeduplicated() {
        val data = templates().take(2)
        var requests = 0
        val exposed = mutableListOf<String>()
        var loadingMore by mutableStateOf(false)
        composeRule.setContent {
            TemplateCatalogScreen(data, categories, 0, 0, false, null, true, loadingMore, null,
                onTabSelect = {}, onTemplateSelect = { _, _ -> }, onExpose = { item, _ -> exposed += item.id },
                onLoadMore = { requests++; loadingMore = true }, onRetry = {}, onBalance = {}, onNavigation = {})
        }
        composeRule.waitUntil(5_000) { requests > 0 && exposed.size == 2 }
        composeRule.runOnIdle { assertEquals(1, requests); assertEquals(2, exposed.distinct().size); assertEquals(2, exposed.size) }
    }

    @Test fun videoAndImageLoadingKeepCatalogLayoutWithoutTheLegacyLoadingPage() {
        var section by mutableStateOf(TemplateSection.Video)
        var loading by mutableStateOf(true)
        var error by mutableStateOf<String?>(null)
        var retries = 0
        composeRule.setContent {
            TemplateCatalogScreen(emptyList(), categories, 0, 2, loading, error, false, false, null,
                {}, { _, _ -> }, { _, _ -> }, {}, { retries++ }, {}, {}, section = section)
        }
        for (value in listOf(TemplateSection.Video, TemplateSection.Image)) {
            composeRule.runOnIdle { section = value }
            composeRule.onNodeWithText("All").assertIsDisplayed()
            composeRule.onNodeWithText("Loading live templates").assertDoesNotExist()
            composeRule.onNodeWithText("Fetching real templates from the server...").assertDoesNotExist()
            composeRule.onNodeWithContentDescription("Loading templates").assertIsDisplayed()
            composeRule.onNodeWithContentDescription(if (value == TemplateSection.Video) "VIDEO" else "IMAGE").assertIsSelected()
        }
        composeRule.runOnIdle { loading = false; error = "Template request failed" }
        composeRule.onNodeWithText("Template request failed").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Loading templates").assertDoesNotExist()
        composeRule.onNodeWithText("RETRY").performClick()
        composeRule.runOnIdle { assertEquals(1, retries) }
    }

    @Test fun successfullyLoadedEmptyCategoriesStayBlankForBothModalities() {
        var section by mutableStateOf(TemplateSection.Video)
        composeRule.setContent {
            TemplateCatalogScreen(emptyList(), categories, 1, 2, false, null, false, false, null,
                {}, { _, _ -> }, { _, _ -> }, {}, {}, {}, {}, section = section)
        }
        for (value in listOf(TemplateSection.Video, TemplateSection.Image)) {
            composeRule.runOnIdle { section = value }
            composeRule.onNodeWithText("Avatar VFX").assertIsSelected()
            composeRule.onNodeWithText("No live templates").assertDoesNotExist()
            composeRule.onNodeWithText("Please retry after templates are available.").assertDoesNotExist()
            composeRule.onNodeWithText("Loading live templates").assertDoesNotExist()
            composeRule.onNodeWithContentDescription("Loading templates").assertDoesNotExist()
            composeRule.onNodeWithText("RETRY").assertDoesNotExist()
            composeRule.onNodeWithContentDescription(if (value == TemplateSection.Video) "VIDEO" else "IMAGE").assertIsSelected()
        }
    }

    @Test fun paginationFailureAndInitialFailureKeepSeparateRetryActions() {
        val data = templates().take(2)
        var pageRetry = 0
        var initialRetry = 0
        var initial by mutableStateOf(false)
        composeRule.setContent {
            TemplateCatalogScreen(if (initial) emptyList() else data, categories, 0, 0, false,
                if (initial) "Initial failed" else null, true, false, if (initial) null else "Page failed",
                onTabSelect = {}, onTemplateSelect = { _, _ -> }, onExpose = { _, _ -> },
                onLoadMore = { pageRetry++ }, onRetry = { initialRetry++ }, onBalance = {}, onNavigation = {})
        }
        composeRule.onNodeWithText("Page failed").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").performClick()
        composeRule.runOnIdle { assertEquals(1, pageRetry); assertEquals(0, initialRetry); initial = true }
        composeRule.onNodeWithText("Initial failed").assertIsDisplayed()
        composeRule.onNodeWithText("RETRY").performClick()
        composeRule.runOnIdle { assertEquals(1, initialRetry) }
    }
}
