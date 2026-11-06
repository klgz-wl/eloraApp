package yumo.achat.app.ui.imagevideo

import android.os.ParcelFileDescriptor
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
import org.junit.Assert.assertTrue
import kotlinx.coroutines.launch
import yumo.achat.app.R
import org.junit.Rule
import org.junit.Test
import java.io.File

class GenerationModeScreenTest {
    @get:Rule val composeRule = createComposeRule()
    private fun image(): String {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val file = File(instrumentation.targetContext.cacheDir, "mode-portrait.png")
        instrumentation.context.assets.open("elora-preview/portrait.png").use { input -> file.outputStream().use(input::copyTo) }
        return file.toURI().toString()
    }
    @Test fun qualityDefaultsToRealQuoteAndFastSelectionChangesGenerateContext() {
        val photo = image()
        val modes = listOf(GenerationModeOption("fast", 2), GenerationModeOption("quality", 7))
        var selected by mutableStateOf(defaultGenerationMode(modes))
        var generated = ""
        var change = 0
        var back = 0
        composeRule.setContent {
            GenerationModeScreen(photo, TemplatePreviewMedia.RemoteImage(photo), modes, selected, false, null,
                onModeSelect = { selected = it }, onGenerate = { generated = selected.orEmpty() }, onBack = { back++ }, onChangePhoto = { change++ })
        }
        composeRule.onNodeWithText("Rendering Engine").assertIsDisplayed()
        composeRule.onNodeWithText("Ultra Studio Quality").assertIsSelected()
        composeRule.onNodeWithContentDescription("Cost: 7 diamonds").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Change photo").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Template preview").assertIsDisplayed()
        composeRule.waitForIdle()
        val screenshot = InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("screencap -p /sdcard/Download/elora-generation-mode.png")
        ParcelFileDescriptor.AutoCloseInputStream(screenshot).use { it.readBytes() }
        composeRule.onNodeWithText("Standard Fast").performClick().assertIsSelected()
        composeRule.onNodeWithContentDescription("Cost: 2 diamonds").assertIsDisplayed()
        composeRule.onNode(hasText("Generate") and hasClickAction()).performClick()
        composeRule.runOnIdle { assertEquals("fast", generated) }
        composeRule.onNodeWithContentDescription("Change photo").performClick()
        composeRule.onNodeWithContentDescription("Back to template preview").performClick()
        composeRule.runOnIdle { assertEquals(1, change); assertEquals(1, back) }
    }

    @Test fun busyPreventsModePhotoBackAndDuplicateGenerationActions() {
        val photo = image()
        composeRule.setContent {
            GenerationModeScreen(photo, TemplatePreviewMedia.RemoteImage(photo), listOf(GenerationModeOption("quality", 4)),
                "quality", true, null, onModeSelect = {}, onGenerate = {}, onBack = {}, onChangePhoto = {})
        }
        composeRule.onNodeWithText("Ultra Studio Quality").assertIsNotEnabled()
        composeRule.onNodeWithContentDescription("Generate").assertIsNotEnabled()
        composeRule.onNodeWithContentDescription("Change photo").assertIsNotEnabled()
        composeRule.onNodeWithContentDescription("Back to template preview").assertIsNotEnabled()
    }

    @Test fun insufficientNoticeStaysOnGenerationPageBeforeDelayedRecharge() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val notice = context.getString(R.string.generation_insufficient_balance)
        var error by mutableStateOf<String?>(null)
        var busy by mutableStateOf(false)
        var routes = 0
        var started = 0L
        var routedAt = 0L
        composeRule.setContent {
            val scope = rememberCoroutineScope()
            GenerationModeScreen(null, TemplatePreviewMedia.Unavailable, listOf(GenerationModeOption("quality", 7)),
                "quality", busy, error, {}, {
                    if (generationNeedsTopUp(7, 2) && !busy) {
                        busy = true
                        error = notice
                        started = composeRule.mainClock.currentTime
                        scope.launch {
                            awaitGenerationTopUpRoute {
                                routedAt = composeRule.mainClock.currentTime
                                routes++
                            }
                        }
                    }
                }, {}, {})
        }
        composeRule.waitForIdle()
        composeRule.mainClock.autoAdvance = false
        composeRule.onNode(hasText("Generate") and hasClickAction()).performClick()
        composeRule.mainClock.advanceTimeBy(100)
        composeRule.onNodeWithText(notice).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Generate").assertIsNotEnabled()
        composeRule.mainClock.advanceTimeBy(1_700)
        composeRule.runOnIdle { assertEquals(0, routes) }
        composeRule.mainClock.advanceTimeBy(300)
        composeRule.runOnIdle {
            assertEquals(1, routes)
            assertTrue(routedAt - started >= 2_000)
        }
        composeRule.mainClock.autoAdvance = true
    }

    @Test fun noQuoteDisablesGenerationAndShortScreenKeepsCtaVisible() {
        val photo = image()
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 1.5f)) {
                Box(Modifier.requiredSize(320.dp, 480.dp)) {
                    GenerationModeScreen(photo, TemplatePreviewMedia.RemoteImage(photo), emptyList(), null,
                        false, "Could not generate (E-402)", onModeSelect = {}, onGenerate = {}, onBack = {}, onChangePhoto = {})
                }
            }
        }
        composeRule.onNode(hasText("Generate") and hasClickAction()).assertIsDisplayed().assertIsNotEnabled()
        composeRule.onNodeWithText("No rendering modes available").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Could not generate (E-402)").performScrollTo().assertIsDisplayed()
    }
}
