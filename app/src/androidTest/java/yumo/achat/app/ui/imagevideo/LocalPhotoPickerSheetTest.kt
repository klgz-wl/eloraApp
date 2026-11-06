package yumo.achat.app.ui.imagevideo

import android.os.ParcelFileDescriptor
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.File

class LocalPhotoPickerSheetTest {
    @get:Rule val composeRule = createComposeRule()
    private fun photos(): List<String> {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        return listOf("elora-preview/portrait.png", "elora-video/male.png").mapIndexed { index, asset ->
            val file = File(instrumentation.targetContext.cacheDir, "picker-$index.png")
            instrumentation.context.assets.open(asset).use { input -> file.outputStream().use(input::copyTo) }
            file.toURI().toString()
        }
    }

    @Test fun sheetDisplaysSelectedPhotosAlbumsAndSelectsAnActualUri() {
        val uris = photos()
        var open by mutableStateOf(true)
        var picked = ""
        var browse = 0
        composeRule.setContent {
            UploadTemplatePreviewScreen("Portrait style", TemplatePreviewMedia.RemoteImage(uris.first()), 0, true,
                onChoosePhoto = { open = true }, onBack = {}, playPreview = !open)
            if (open) LocalPhotoPickerSheet(uris, uris.first(), onDismiss = { open = false },
                onBrowseDevice = { browse++ }, onSelectPhoto = { picked = it; open = false }, onClearRecent = {})
        }
        composeRule.onNodeWithText("Photos").assertIsSelected()
        composeRule.onNodeWithContentDescription("Recent photo 1").assertIsSelected().assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Recent photo 2").assertIsNotSelected()
        composeRule.waitForIdle()
        val screenshot = InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("screencap -p /sdcard/Download/elora-photo-picker.png")
        ParcelFileDescriptor.AutoCloseInputStream(screenshot).use { it.readBytes() }
        composeRule.onNodeWithContentDescription("Add photo").performClick()
        composeRule.runOnIdle { assertEquals(1, browse) }
        composeRule.onNodeWithText("Albums").performClick()
        composeRule.onNode(hasText("Albums") and isSelected()).assertIsDisplayed()
        composeRule.onNodeWithText("Browse device albums").performClick()
        composeRule.runOnIdle { assertEquals(2, browse) }
        composeRule.onNodeWithText("Selected photos (2)").performClick()
        composeRule.onNodeWithText("Photos").assertIsSelected()
        composeRule.onNodeWithContentDescription("Recent photo 2").performClick()
        composeRule.runOnIdle { assertEquals(uris[1], picked) }
        composeRule.onNodeWithText("Upload Photo").assertIsDisplayed()
    }

    @Test fun optionsClearOnlyHistoryAndClosingReturnsToPreview() {
        var recent by mutableStateOf(photos())
        var open by mutableStateOf(true)
        var browse = 0
        composeRule.setContent {
            if (open) LocalPhotoPickerSheet(recent, null, onDismiss = { open = false },
                onBrowseDevice = { browse++ }, onSelectPhoto = {}, onClearRecent = { recent = emptyList() })
        }
        composeRule.onNodeWithContentDescription("Photo picker options").performClick()
        composeRule.onNodeWithText("Choose from device").performClick()
        composeRule.runOnIdle { assertEquals(1, browse) }
        composeRule.onNodeWithContentDescription("Photo picker options").performClick()
        composeRule.onNodeWithText("Clear recent selection history").performClick()
        composeRule.onNodeWithText("No photos selected yet. Tap + to choose from your device.").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Close photo picker").performClick()
        composeRule.runOnIdle { assertEquals(false, open); assertEquals(emptyList<String>(), recent) }
    }
}
