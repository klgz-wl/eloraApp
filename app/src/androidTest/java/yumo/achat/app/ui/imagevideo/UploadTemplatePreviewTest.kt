package yumo.achat.app.ui.imagevideo

import android.os.ParcelFileDescriptor
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.KeyEvent
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.core.app.ActivityOptionsCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.File
import yumo.achat.core.analytics.NoOpEventTracker

class UploadTemplatePreviewTest {
    @get:Rule val composeRule = createComposeRule()
    private fun image(): TemplatePreviewMedia.RemoteImage {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val photo = File(instrumentation.targetContext.cacheDir, "preview-portrait.png")
        instrumentation.context.assets.open("elora-preview/portrait.png").use { input -> photo.outputStream().use(input::copyTo) }
        return TemplatePreviewMedia.RemoteImage(photo.toURI().toString())
    }

    @Test fun realUploadRouteKeepsCancelledPickerAndHandlesSystemBackAndSelectedUri() {
        val media = image()
        var request = -1
        var pickerType: String? = null
        var backs = 0
        val registry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(requestCode: Int, contract: ActivityResultContract<I, O>, input: I, options: ActivityOptionsCompat?) {
                request = requestCode
                pickerType = (input as Intent).type
            }
        }
        val owner = object : ActivityResultRegistryOwner { override val activityResultRegistry = registry }
        composeRule.setContent {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides owner) {
                UploadPhotoScreen(0, onBack = { backs++ }, onNavigationSelect = {}, onInsufficientBalanceTopUp = {},
                    diamondBalance = 0,
                    selectedTemplate = SelectedGenerationTemplate("template", "image", "fast", "Portrait style", media, 0, "portrait", "image", 10),
                    trackedTasks = emptyList(), onTaskCreated = { _, _ -> }, onBalanceClick = {},
                    analytics = NoOpEventTracker, analyticsUserId = null)
            }
        }
        composeRule.onNodeWithText("Upload Photo").performClick()
        composeRule.onNodeWithContentDescription("Add photo").performClick()
        composeRule.runOnIdle {
            assertEquals("image/*", pickerType)
            registry.dispatchResult(request, Activity.RESULT_CANCELED, null)
        }
        composeRule.onNodeWithText("Photos").assertIsSelected()
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        composeRule.onNodeWithText("Upload Photo").assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(0, backs) }
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        composeRule.runOnIdle { assertEquals(1, backs) }
        composeRule.onNodeWithText("Upload Photo").performClick()
        composeRule.onNodeWithContentDescription("Add photo").performClick()
        composeRule.runOnIdle {
            // An unavailable local provider fails before network upload, while exercising the real picker callback and form transition.
            registry.dispatchResult(request, Activity.RESULT_OK, Intent().setData(Uri.parse("content://elora-preview-test/missing-image")))
        }
        composeRule.onNodeWithText("YOUR PHOTO").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("CONTINUE").performScrollTo().assertIsNotEnabled()
    }

    @Test fun imagePreviewUploadCancellationDirectReportAndBackKeepTheirOwnActions() {
        val media = image()
        var pickerRequests = 0
        var reports = 0
        var back = 0
        composeRule.setContent {
            UploadTemplatePreviewScreen("Portrait style", media, 0, true,
                onChoosePhoto = { pickerRequests++ }, onBack = { back++ }, onReport = { reports++ })
        }
        composeRule.onNodeWithText("Portrait style").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Selected template preview").assertIsDisplayed()
        composeRule.onNodeWithText("Upload Photo").assertIsDisplayed().performClick()
        composeRule.runOnIdle { assertEquals(1, pickerRequests) }
        // No picker result means no local URI: the preview must remain available for another attempt.
        composeRule.onNodeWithText("Upload Photo").assertIsDisplayed()
        composeRule.waitForIdle()
        val screenshot = InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("screencap -p /sdcard/Download/elora-template-preview.png")
        ParcelFileDescriptor.AutoCloseInputStream(screenshot).use { it.readBytes() }
        composeRule.onNodeWithContentDescription("Report content").performClick()
        composeRule.onNodeWithText("Image template").assertDoesNotExist()
        composeRule.runOnIdle { assertEquals(1, reports); assertEquals(1, pickerRequests) }
        composeRule.onNodeWithContentDescription("Back to templates").performClick()
        composeRule.runOnIdle { assertEquals(1, back) }
    }

    @Test fun videoInformationUsesDurationAndDisabledUploadDoesNotRequestPicker() {
        val poster = image()
        var requested = false
        composeRule.setContent {
            UploadTemplatePreviewScreen("Video style", TemplatePreviewMedia.RemoteVideo("file:///missing-preview.mp4", poster.url),
                12, false, onChoosePhoto = { requested = true }, onBack = {})
        }
        composeRule.onNodeWithText("Upload Photo").assertIsNotEnabled()
        composeRule.onNodeWithContentDescription("Template information").performClick()
        composeRule.onNodeWithText("Video template · 12 seconds").assertIsDisplayed()
        composeRule.onNodeWithText("Close").performClick()
        composeRule.runOnIdle { assertEquals(false, requested) }
    }

    @Test fun uploadEntryRemainsVisibleOnShortScreenWithLargeText() {
        val media = image()
        var requested = false
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 1.5f)) {
                Box(Modifier.requiredSize(320.dp, 480.dp)) {
                    UploadTemplatePreviewScreen("Portrait style", media, 0, true, onChoosePhoto = { requested = true }, onBack = {})
                }
            }
        }
        composeRule.onNodeWithText("Upload Photo").assertIsDisplayed().performClick()
        composeRule.runOnIdle { assertEquals(true, requested) }
    }
}
