package yumo.achat.app.ui.imagevideo

import android.os.ParcelFileDescriptor
import android.view.KeyEvent
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import yumo.achat.app.R
import yumo.achat.core.backend.PagedResult
import yumo.achat.core.backend.VisualResource

class MeCreationsTest {
    @get:Rule val composeRule = createComposeRule()
    private fun resources(): List<VisualResource> {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        return listOf("fragrance", "male").mapIndexed { index, photo ->
            val file = File(instrumentation.targetContext.cacheDir, "me-$photo.png")
            instrumentation.context.assets.open("elora-video/$photo.png").use { input -> file.outputStream().use(input::copyTo) }
            VisualResource("resource-$index", "task-$index", "generated", if (index == 0) "image" else "video",
                templateName = if (index == 0) "Image creation" else "Video creation", url = file.toURI().toString(),
                thumbnailUrl = file.toURI().toString(), mimeType = if (index == 0) "image/png" else "video/mp4",
                width = 600, height = 1000, durationSeconds = 12)
        }
    }

    @Test fun allVideosImagesAndPrototypePurchaseKeepTheirExpectedBehavior() {
        val data = resources()
        val requests = mutableListOf<String?>()
        var opened = ""
        var settings = false
        var editName = false
        var editAvatar = false
        var purchase = 0
        composeRule.setContent {
            MeCreationsRoute("user", "Elora tester", data[1].thumbnailUrl, 2, false, "", loadPage = { modality, page ->
                requests += modality
                val items = data.filter { modality == null || it.modality == modality }
                PagedResult(items, page, 20, items.size.toLong())
            }, onEditName = { editName = true }, onEditAvatar = { editAvatar = true }, onSettings = { settings = true },
                onOpen = { opened = it.taskId }, onNavigation = {}, onPurchase = { purchase++ })
        }
        composeRule.onNodeWithText("Creations").assertIsSelected()
        composeRule.onNodeWithText("Auto-deletion enabled:", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("72 hours", substring = true).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Open creation: Image creation").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Open creation: Video creation").assertIsDisplayed()
        composeRule.onNodeWithText("Purchase Credits").assertIsEnabled().performClick()
        composeRule.runOnIdle { assertEquals(1, purchase) }
        composeRule.onNodeWithContentDescription("ME").assertIsSelected()
        composeRule.waitForIdle()
        val screenshot = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("screencap -p /sdcard/Download/elora-me-creations.png")
        ParcelFileDescriptor.AutoCloseInputStream(screenshot).use { it.readBytes() }
        composeRule.onNodeWithText("Videos").performClick().assertIsSelected()
        composeRule.onNodeWithContentDescription("Open creation: Video creation").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Open creation: Image creation").assertDoesNotExist()
        composeRule.onNodeWithText("Images").performClick().assertIsSelected()
        composeRule.onNodeWithContentDescription("Open creation: Image creation").assertIsDisplayed().performClick()
        composeRule.onNodeWithContentDescription("Open creation: Video creation").assertDoesNotExist()
        composeRule.runOnIdle { assertEquals("task-0", opened); assertEquals(listOf(null, "video", "image"), requests) }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.onNodeWithContentDescription(context.getString(R.string.profile_edit_description)).performClick()
        composeRule.onNodeWithContentDescription(context.getString(R.string.profile_avatar_edit_description)).performClick()
        composeRule.onNodeWithContentDescription("Settings").performClick()
        composeRule.runOnIdle { assertTrue(editName); assertTrue(editAvatar); assertTrue(settings) }
    }

    @Test fun emptyListShowsExactCopyAndCreateNowRoutesHome() {
        var destination = -1
        composeRule.setContent {
            MeCreationsRoute("user", "Elora tester", null, 0, false, "",
                loadPage = { _, page -> PagedResult(emptyList(), page, 20, 0) }, onEditName = {}, onEditAvatar = {},
                onSettings = {}, onOpen = {}, onNavigation = { destination = it })
        }
        composeRule.onNodeWithText("Auto-deletion enabled:", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("No Creations yet.").assertIsDisplayed()
        composeRule.waitForIdle()
        val screenshot = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("screencap -p /sdcard/Download/elora-me-empty.png")
        ParcelFileDescriptor.AutoCloseInputStream(screenshot).use { it.readBytes() }
        composeRule.onNodeWithText("Create Now").performClick()
        composeRule.runOnIdle { assertEquals(4, destination) }
        composeRule.onNodeWithText("Videos").performClick()
        composeRule.onNodeWithText("No Creations yet.").assertIsDisplayed()
        composeRule.onNodeWithText("Images").performClick()
        composeRule.onNodeWithText("No Creations yet.").assertIsDisplayed()
    }

    @Test fun staleFilterResponseCannotReplaceNewFilterAndEmptyUrlsAreSkipped() {
        val data = resources()
        val delayed = CompletableDeferred<PagedResult<VisualResource>>()
        composeRule.setContent {
            MeCreationsRoute("user", "Elora tester", null, 0, false, "", loadPage = { modality, page ->
                if (modality == "video") withContext(NonCancellable) { delayed.await() }
                else PagedResult(data.filter { modality == null || it.modality == modality } + data[0].copy(id = "bad", taskId = "bad", url = ""), page, 20, 3)
            }, onEditName = {}, onEditAvatar = {}, onSettings = {}, onOpen = {}, onNavigation = {})
        }
        composeRule.onNodeWithText("Videos").performClick()
        composeRule.onNodeWithText("Images").performClick()
        composeRule.onNodeWithContentDescription("Open creation: Image creation").assertIsDisplayed()
        composeRule.runOnIdle { delayed.complete(PagedResult(listOf(data[1]), 1, 20, 1)) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Images").assertIsSelected()
        composeRule.onNodeWithContentDescription("Open creation: Video creation").assertDoesNotExist()
        composeRule.onNodeWithText("No Creations yet.").assertDoesNotExist()
    }

    @Test fun failureDoesNotPretendEmptyAndSuccessfulRetryShowsData() {
        val data = resources()
        var attempts = 0
        composeRule.setContent {
            MeCreationsRoute("user", "Elora tester", null, 0, false, "", loadPage = { _, page ->
                attempts++
                if (attempts == 1) throw IllegalStateException()
                PagedResult(data, page, 20, 2)
            }, onEditName = {}, onEditAvatar = {}, onSettings = {}, onOpen = {}, onNavigation = {})
        }
        composeRule.onNodeWithText("Retry").performClick()
        composeRule.onNodeWithContentDescription("Open creation: Image creation").assertIsDisplayed()
        composeRule.onNodeWithText("No Creations yet.").assertDoesNotExist()
    }

    @Test fun paginationRetryKeepsTheSelectedModalityAndExistingCreations() {
        val video = resources()[1]
        var secondPageAttempts = 0
        val queries = mutableListOf<Pair<String?, Int>>()
        composeRule.setContent {
            MeCreationsRoute("user", "Elora tester", null, 0, false, "", loadPage = { modality, page ->
                queries += modality to page
                if (page == 2 && secondPageAttempts++ == 0) throw IllegalStateException()
                PagedResult(listOf(video.copy(id = "video-$page", taskId = "video-$page", templateName = "Video page $page")), page, 1, 2)
            }, onEditName = {}, onEditAvatar = {}, onSettings = {}, onOpen = {}, onNavigation = {})
        }
        composeRule.onNodeWithText("Videos").performClick()
        composeRule.onNodeWithText("Load more").performClick()
        composeRule.onNodeWithContentDescription("Open creation: Video page 1").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").performClick()
        composeRule.onNodeWithContentDescription("Open creation: Video page 2").assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(listOf(null to 1, "video" to 1, "video" to 2, "video" to 2), queries) }
        composeRule.onNodeWithText("No Creations yet.").assertDoesNotExist()
    }

    @Test fun systemBackFromResultInvokesReturnToMeCallback() {
        val task = resources()[0].toTrackedGenerationTask("Creation")
        var returned = false
        composeRule.setContent { GenerationResultScreen(task, onBack = { returned = true }) }
        composeRule.onNodeWithText("Result").assertIsDisplayed()
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        composeRule.runOnIdle { assertTrue(returned) }
    }
}
