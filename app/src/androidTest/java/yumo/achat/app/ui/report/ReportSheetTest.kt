package yumo.achat.app.ui.report

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import android.os.ParcelFileDescriptor
import android.view.KeyEvent
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CompletableDeferred
import java.io.File
import yumo.achat.app.ui.imagevideo.UploadTemplatePreviewScreen
import yumo.achat.app.ui.imagevideo.TemplatePreviewMedia
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.performScrollTo

@RunWith(AndroidJUnit4::class)
class ReportSheetTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun closeButtonDismissesWithoutSubmitting() {
        var dismissed = false
        var submitted = false
        composeRule.setContent {
            MediaReportSheet("template", onDismiss = { dismissed = true }, onSuccess = {},
                repository = MediaReportRepository { submitted = true })
        }
        composeRule.onNodeWithContentDescription("Close report").performClick()
        composeRule.runOnIdle { assertTrue(dismissed); assertTrue(!submitted) }
    }

    @Test fun closingWhileSubmittingCancelsOwnedRequestAndDoesNotSucceed() {
        var open by mutableStateOf(true)
        var cancelled = false
        var successes = 0
        var attempts = 0
        val pending = CompletableDeferred<Unit>()
        composeRule.setContent {
            if (open) MediaReportSheet("template", onDismiss = { open = false }, onSuccess = { successes++ },
                repository = MediaReportRepository {
                    attempts++
                    try { pending.await() } finally { cancelled = true }
                })
        }
        composeRule.onNodeWithText("Report").performClick()
        composeRule.onNodeWithContentDescription("Reporting…").assertIsNotEnabled()
        composeRule.onNodeWithContentDescription("Close report").performClick()
        composeRule.waitUntil(5_000) { cancelled }
        composeRule.runOnIdle { assertTrue(!open); assertTrue(successes == 0); assertTrue(attempts == 1) }
    }

    @Test fun previewReportIconDirectlyShowsPrototypeAndReturnsOnClose() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val portrait = File(instrumentation.targetContext.cacheDir, "report-portrait.png")
        instrumentation.context.assets.open("elora-preview/portrait.png").use { input -> portrait.outputStream().use(input::copyTo) }
        composeRule.setContent {
            var open by remember { mutableStateOf(false) }
            UploadTemplatePreviewScreen("Portrait style", TemplatePreviewMedia.RemoteImage(portrait.toURI().toString()), 0,
                true, onChoosePhoto = {}, onBack = {}, onReport = { open = true }, playPreview = !open)
            if (open) MediaReportSheet("template", onDismiss = { open = false }, onSuccess = { open = false },
                repository = MediaReportRepository {})
        }
        composeRule.onNodeWithContentDescription("Report content").performClick()
        composeRule.onNodeWithText("Report").assertIsDisplayed()
        composeRule.onNodeWithText("Feel free to report this content if you think it’s inappropriate.").assertIsDisplayed()
        composeRule.waitForIdle()
        val screenshot = instrumentation.uiAutomation.executeShellCommand("screencap -p /sdcard/Download/elora-report-sheet.png")
        ParcelFileDescriptor.AutoCloseInputStream(screenshot).use { it.readBytes() }
        composeRule.onNodeWithContentDescription("Close report").performClick()
        composeRule.onNodeWithText("Upload Photo").assertIsDisplayed()
    }

    @Test fun systemBackDismissesReportWithoutSubmitting() {
        var open by mutableStateOf(true)
        var submitted = false
        composeRule.setContent {
            if (open) MediaReportSheet("template", onDismiss = { open = false }, onSuccess = {},
                repository = MediaReportRepository { submitted = true })
        }
        composeRule.onNodeWithText("Report").assertIsDisplayed()
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        composeRule.waitUntil(5_000) { !open }
        composeRule.runOnIdle { assertTrue(!submitted) }
    }

    @Test fun compactSheetWithLargeTextKeepsReportAndCloseReachable() {
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 1.5f)) {
                MediaReportSheet("template", onDismiss = {}, onSuccess = {}, modifier = Modifier.heightIn(max = 320.dp),
                    repository = MediaReportRepository {})
            }
        }
        composeRule.onNodeWithText("Report").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Close report").performScrollTo().assertIsDisplayed()
    }

    @Test fun reportFailureDisplaysE701AndKeepsExistingRetryAction() {
        var attempts = 0
        composeRule.setContent {
            MediaReportSheet(
                mediaId = "template-1",
                onDismiss = {},
                onSuccess = {},
                repository = MediaReportRepository {
                    attempts++
                    throw IllegalStateException("Report unavailable")
                },
            )
        }
        composeRule.onNodeWithText("Report").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Couldn’t report this content. Please try again. (E-701)")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Couldn’t report this content. Please try again. (E-701)").assertIsDisplayed()
        composeRule.onNodeWithText("Report").performClick()
        composeRule.waitUntil(5_000) { attempts == 2 }
    }

    @Test fun previewControlOpensReportSheetAndSubmitsOnce() {
        composeRule.setContent {
            var open by remember { mutableStateOf(false) }
            var success by remember { mutableStateOf(false) }
            Box {
                MediaReportIconButton(onClick = { open = true })
                if (open) MediaReportSheet(
                    mediaId = "template-1",
                    onDismiss = { open = false },
                    onSuccess = { success = true; open = false },
                    repository = MediaReportRepository { },
                )
                if (success) Text("Submitted")
            }
        }

        composeRule.onNodeWithContentDescription("Report content").performClick()
        composeRule.onNodeWithText("Feel free to report this content if you think it’s inappropriate.").assertIsDisplayed()
        val viewportBounds = composeRule.onAllNodes(isRoot())[1].getUnclippedBoundsInRoot()
        val viewportHeight = viewportBounds.bottom - viewportBounds.top
        val copyTop = composeRule.onNodeWithText("Feel free to report this content if you think it’s inappropriate.")
            .getUnclippedBoundsInRoot().top
        assertTrue("Report sheet must stay in the lower half", copyTop > viewportHeight * .35f)
        composeRule.onNodeWithText("Report").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Submitted").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
