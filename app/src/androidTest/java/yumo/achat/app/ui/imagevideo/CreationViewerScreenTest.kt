package yumo.achat.app.ui.imagevideo

import androidx.annotation.OptIn

import android.view.KeyEvent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

@OptIn(androidx.media3.common.util.UnstableApi::class)
class CreationViewerScreenTest {
    @get:Rule val compose = createComposeRule()
    private fun photo(): String {
        val inst = InstrumentationRegistry.getInstrumentation()
        val file = File(inst.targetContext.cacheDir, "creation-viewer.png")
        inst.context.assets.open("elora-video/male.png").use { input -> file.outputStream().use(input::copyTo) }
        return file.toURI().toString()
    }
    private fun task() = TrackedGenerationTask("created", "My image", "image", "succeeded", photo(), "image/png", null)

    @Test fun imageIsFullscreenWithoutPlaybackOrTaskMetadataAndBackReturnsToImages() {
        val image = task()
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            val stableUserId = "user"
            var loadedProfile by remember { mutableStateOf("") }
            LaunchedEffect(Unit) { loadedProfile = "Viewer tester" }
            var selected by rememberSaveable(stableUserId) { mutableStateOf<TrackedGenerationTask?>(null) }
            var filter by rememberSaveable(stableUserId) { mutableStateOf(MeCreationFilter.Images) }
            val current = selected
            if (current != null) CreationViewerScreen(current, { selected = null })
            else MeCreationsScreen(loadedProfile, null, 2, false, filter, 1, listOf(image), false, null, false, false, null,
                { filter = it }, {}, {}, {}, {}, {}, { selected = it }, {})
        }
        compose.onNodeWithContentDescription("Open creation: My image").performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithContentDescription("Generated image").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Generated image").assertIsDisplayed()
        val fd = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("screencap -p /sdcard/Download/elora-viewer-image.png")
        android.os.ParcelFileDescriptor.AutoCloseInputStream(fd).use { it.readBytes() }
        compose.onNodeWithText("Result").assertDoesNotExist()
        compose.onNodeWithText("My image").assertDoesNotExist()
        compose.onNodeWithContentDescription("Play video").assertDoesNotExist()
        restoration.emulateSavedInstanceStateRestore()
        compose.waitUntil(5_000) { compose.onAllNodesWithContentDescription("Generated image").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Generated image").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back to Me").performClick()
        compose.onNodeWithText("Images").assertIsSelected()
        compose.onNodeWithContentDescription("Open creation: My image").performClick()
        compose.onNodeWithContentDescription("Back to Me").assertIsDisplayed()
        compose.waitForIdle()
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        compose.onNodeWithText("Images").assertIsSelected()
    }

    @Test fun localVideoStartsPausedPlaysAndPausesWithLifecycleAndSourceChanges() {
        val inst = InstrumentationRegistry.getInstrumentation()
        fun video(name: String): String {
            val file = File(inst.targetContext.cacheDir, name)
            inst.context.assets.open("elora-viewer/pattern.mp4").use { input -> file.outputStream().use(input::copyTo) }
            return file.toURI().toString()
        }
        val first = video("viewer-first.mp4")
        val second = video("viewer-second.mp4")
        val owner = object : androidx.lifecycle.LifecycleOwner {
            val registry = androidx.lifecycle.LifecycleRegistry(this)
            override val lifecycle: androidx.lifecycle.Lifecycle get() = registry
        }
        inst.runOnMainSync { owner.registry.currentState = androidx.lifecycle.Lifecycle.State.RESUMED }
        var current by mutableStateOf(TrackedGenerationTask("video-first", "My video", "video", "succeeded", first, "video/mp4", null, photo()))
        compose.setContent { CompositionLocalProvider(androidx.lifecycle.compose.LocalLifecycleOwner provides owner) { CreationViewerScreen(current, {}) } }
        var player: androidx.media3.common.Player? = null
        androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom(androidx.media3.ui.PlayerView::class.java))
            .check { view, _ -> player = (view as androidx.media3.ui.PlayerView).player }
        fun onPlayer(check: (androidx.media3.common.Player) -> Boolean): Boolean {
            var result = false
            inst.runOnMainSync { result = player?.let(check) ?: false }
            return result
        }
        compose.waitUntil(10_000) { onPlayer { it.playbackState == androidx.media3.common.Player.STATE_READY } }
        compose.onNodeWithContentDescription("Play video").assertIsDisplayed()
        compose.waitUntil(5_000) { onPlayer { !it.playWhenReady } }
        val fd = inst.uiAutomation.executeShellCommand("screencap -p /sdcard/Download/elora-viewer-video.png")
        android.os.ParcelFileDescriptor.AutoCloseInputStream(fd).use { it.readBytes() }
        compose.onNodeWithContentDescription("Play video").performClick()
        compose.waitUntil(5_000) { onPlayer { it.currentPosition > 100L } }
        compose.onNodeWithContentDescription("Pause video").assertIsDisplayed()
        compose.runOnIdle { owner.registry.currentState = androidx.lifecycle.Lifecycle.State.CREATED }
        compose.waitUntil(5_000) { onPlayer { !it.playWhenReady } }
        compose.runOnIdle { owner.registry.currentState = androidx.lifecycle.Lifecycle.State.RESUMED }
        compose.waitUntil(5_000) { onPlayer { it.playWhenReady } }
        compose.onNodeWithContentDescription("Pause video").performClick()
        compose.waitUntil(5_000) { onPlayer { !it.playWhenReady } }
        compose.onNodeWithContentDescription("Play video").performClick()
        compose.runOnIdle { current = current.copy(taskId = "video-second", resultUrl = second) }
        compose.onNodeWithContentDescription("Play video").assertIsDisplayed()
        androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom(androidx.media3.ui.PlayerView::class.java))
            .check { view, _ -> assertFalse((view as androidx.media3.ui.PlayerView).player!!.playWhenReady) }
    }

    @Test fun videoFailureShowsRetryWithoutPlaybackOverlayAndRetainsBack() {
        val image = task()
        var back = false
        val inst = InstrumentationRegistry.getInstrumentation()
        val file = File(inst.targetContext.cacheDir, "viewer-retry.mp4")
        inst.context.assets.open("elora-viewer/pattern.mp4").use { input -> file.outputStream().use(input::copyTo) }
        compose.setContent { CreationViewerScreen(image.copy(modality = "video", mimeType = "video/mp4", resultUrl = file.toURI().toString()), { back = true }, initialVideoPlaybackFailed = true) }
        compose.onNodeWithText("Result file unavailable, pull to refresh or try later.", substring = true).assertIsDisplayed()
        compose.onNodeWithContentDescription("Play video").assertDoesNotExist()
        compose.onNodeWithText("RETRY").assertIsDisplayed().performClick()
        compose.onNodeWithContentDescription("Play video").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back to Me").performClick()
        compose.runOnIdle { assertTrue(back) }
    }
}
