package yumo.achat.app.ui.imagevideo

import android.os.ParcelFileDescriptor
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class CreditsPurchaseScreenTest {
    @get:Rule val compose = createComposeRule()
    private val packs = listOf(200, 500, 900, 1500, 2800, 6000).mapIndexed { index, amount ->
        CreditPack("pack-$index", amount, "", listOf("$19.99", "$34.99", "$54.99", "$79.99", "$119.99", "$199.99")[index],
            "$249.99", index + 1, Color.Magenta, badgeText = if (index == 3) "BEST VALUE" else null, discountPercent = 10 + index * 10)
    }
    @Test fun livePackageSelectionAndHistoryBackUseTheirOwnCallbacks() {
        var selected by mutableStateOf("pack-3")
        var purchased = ""
        var back = false
        var records by mutableStateOf(false)
        compose.setContent {
            if (records) WalletRecordRoute("user", { _, _ -> emptyList() }, { records = false }) else
            CreditsPurchaseScreen(2, packs, selected, true, false, null, "Purchase", true,
                onSelect = { selected = it }, onPurchase = { purchased = selected }, onRetryCatalog = {}, onBack = { back = true },
                status = {}, onHistory = { records = true })
        }
        compose.onNodeWithText("Elora Tokens").assertIsDisplayed()
        compose.onNodeWithText("1,500").assertIsSelected()
        compose.onNodeWithText("Purchase").assertIsEnabled()
        compose.waitForIdle()
        val image = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("screencap -p /sdcard/Download/elora-credits.png")
        ParcelFileDescriptor.AutoCloseInputStream(image).use { it.readBytes() }
        compose.onNodeWithText("200").performClick().assertIsSelected()
        compose.onNodeWithText("Purchase").performClick()
        compose.runOnIdle { assertEquals("pack-0", purchased) }
        compose.onNodeWithContentDescription("Wallet history").performClick()
        compose.onNodeWithText("Record").assertIsDisplayed()
        compose.onNodeWithText("No transactions yet").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back from records").performClick()
        compose.onNodeWithText("200").assertIsSelected()
        compose.onNodeWithContentDescription("Back from recharge").performClick()
        compose.runOnIdle { assertTrue(back) }
    }

    @Test fun generationSelectionSurvivesRechargeAndSavedStateRestoration() {
        val restore = StateRestorationTester(compose)
        restore.setContent {
            val holder = rememberSaveableStateHolder()
            var topUp by rememberSaveable { mutableStateOf(false) }
            if (topUp) CreditsPurchaseScreen(0, packs, "pack-0", true, false, null, "Purchase", true,
                onSelect = {}, onPurchase = {}, onRetryCatalog = {}, onBack = { topUp = false }, status = {}, onHistory = {})
            else holder.SaveableStateProvider("owner-generation") {
                var mode by rememberSaveable { mutableStateOf("quality") }
                GenerationModeScreen(null, TemplatePreviewMedia.Unavailable,
                    listOf(GenerationModeOption("fast", 2), GenerationModeOption("quality", 7)), mode,
                    false, null, onModeSelect = { mode = it }, onGenerate = { topUp = true }, onBack = {}, onChangePhoto = {})
            }
        }
        compose.onNodeWithText("Standard Fast").performClick()
        compose.onNode(hasText("Generate") and hasClickAction()).performClick()
        compose.onNodeWithText("Elora Tokens").assertIsDisplayed()
        restore.emulateSavedInstanceStateRestore()
        compose.onNodeWithContentDescription("Back from recharge").performClick()
        compose.onNodeWithText("Standard Fast").assertIsSelected()
        compose.onNodeWithContentDescription("Cost: 2 diamonds").assertIsDisplayed()
    }
}
