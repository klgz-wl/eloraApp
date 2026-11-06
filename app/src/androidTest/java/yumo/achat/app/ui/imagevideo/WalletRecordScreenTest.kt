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
import yumo.achat.app.ui.theme.AchatTheme
import yumo.achat.core.backend.WalletTransaction
import java.io.IOException

class WalletRecordScreenTest {
    @get:Rule val compose = createComposeRule()
    private fun record(id: String, income: Boolean = false) = WalletTransaction(id, if (income) "income" else "expense",
        if (income) 40 else 14, null, if (income) "recharge" else "video_generation", "2026-04-09T17:55:00", null, null, null)

    @Test fun recordsAndHelpSheetFollowPrototypeAndBackOnlyDismissesTheSheet() {
        var backs = 0
        compose.setContent { AchatTheme { WalletRecordRoute("user", { _, _ -> listOf(record("1"), record("2"), record("3"), record("4", true), record("5")) }, { backs++ }) } }
        compose.onNodeWithText("Record").assertIsDisplayed()
        compose.onNodeWithText("+40.00").assertIsDisplayed()
        compose.onAllNodesWithText("-14.00").assertCountEquals(4)
        screenshot("elora-record.png")
        compose.onNodeWithContentDescription("Record information").performClick()
        compose.onNodeWithText("I Understand").assertIsDisplayed()
        compose.onNodeWithText("If your payment was successful", substring = true).assertIsDisplayed()
        screenshot("elora-record-help.png")
        compose.onNodeWithText("I Understand").performClick()
        compose.onNodeWithText("I Understand").assertDoesNotExist()
        compose.onNodeWithContentDescription("Record information").performClick()
        compose.onNodeWithText("I Understand").assertIsDisplayed()
        compose.waitForIdle()
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        compose.waitUntil(5_000) { compose.onAllNodesWithText("I Understand").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithText("I Understand").assertDoesNotExist()
        compose.runOnIdle { assertEquals(0, backs) }
        compose.onNodeWithContentDescription("Record information").performClick()
        compose.onNodeWithContentDescription("Close").performClick()
        compose.onNodeWithContentDescription("Back from records").performClick()
        compose.runOnIdle { assertEquals(1, backs) }
    }

    @Test fun firstPageFailureRetriesAndPaginationKeepsRowsWithExplicitRetry() {
        var firstFailures = true
        var moreFailures = true
        val pages = mutableListOf<Int>()
        compose.setContent { AchatTheme { WalletRecordRoute("user", { page, size ->
            assertEquals(20, size); pages += page
            if (page == 1 && firstFailures) throw IOException("offline")
            if (page == 2 && moreFailures) throw IOException("offline")
            if (page == 1) (1..20).map { record("$it") } else listOf(record("20"), record("21", true))
        }, {}) } }
        compose.onNodeWithText("Unable to load records (E-501)").assertIsDisplayed()
        compose.onNodeWithText("No transactions yet").assertDoesNotExist()
        firstFailures = false
        compose.onNodeWithText("Retry").performClick()
        scrollTo("Load more records")
        compose.onNodeWithText("Load more records").performClick()
        scrollTo("Unable to load records (E-501)")
        compose.onNodeWithText("Unable to load records (E-501)").assertIsDisplayed()
        moreFailures = false
        scrollTo("Retry loading more")
        compose.onNodeWithText("Retry loading more").performClick()
        scrollTo("+40.00")
        compose.onNodeWithText("+40.00").assertIsDisplayed()
        compose.onNodeWithText("Load more records").assertDoesNotExist()
        compose.runOnIdle { assertEquals(listOf(1, 1, 2, 2), pages) }
    }

    @Test fun changedOwnerIgnoresLatePageAndEmptyIsDistinctFromLoading() {
        val old = CompletableDeferred<List<WalletTransaction>>()
        val fresh = CompletableDeferred<List<WalletTransaction>>()
        var owner by mutableStateOf("old")
        compose.setContent { AchatTheme { WalletRecordRoute(owner, if (owner == "old") { _, _ -> withContext(NonCancellable) { old.await() } }
            else { _, _ -> fresh.await() }, {}) } }
        compose.onNodeWithText("Loading records…").assertIsDisplayed()
        compose.onNodeWithText("No transactions yet").assertDoesNotExist()
        compose.runOnIdle { owner = "new"; fresh.complete(emptyList()) }
        compose.onNodeWithText("No transactions yet").assertIsDisplayed()
        compose.runOnIdle { old.complete(listOf(record("late", true))) }
        compose.onNodeWithText("+40.00").assertDoesNotExist()
        compose.onNodeWithText("No transactions yet").assertIsDisplayed()
    }

    @Test fun homeVideoAndImageBalanceOpenRecordsWhileMembershipStillOpensRecharge() {
        var section by mutableStateOf(4)
        var records by mutableStateOf(false)
        var purchases = 0
        compose.setContent { AchatTheme {
            if (records) WalletRecordRoute("user", { _, _ -> emptyList() }, { records = false })
            else if (section == 4) HomeVideoFeedScreen(emptyList(), 2, false, null, false, false, null,
                {}, {}, {}, { _, _ -> }, { purchases++ }, { section = it }, onRecord = { records = true })
            else TemplateCatalogScreen(emptyList(), emptyList(), 0, 2, false, null, false, false, null,
                {}, { _, _ -> }, { _, _ -> }, {}, {}, { purchases++ }, { section = it }, onRecord = { records = true },
                section = if (section == 1) TemplateSection.Image else TemplateSection.Video)
        } }
        for (destination in listOf(4, 0, 1)) {
            compose.runOnIdle { section = destination }
            compose.onNodeWithContentDescription("Balance: 2 diamonds").performClick()
            compose.onNodeWithText("Record").assertIsDisplayed()
            compose.onNodeWithContentDescription("Back from records").performClick()
            compose.runOnIdle { assertEquals(destination, section) }
            compose.onNodeWithContentDescription("Elora membership").performClick()
        }
        compose.runOnIdle { assertEquals(3, purchases) }
    }

    private fun scrollTo(text: String) { compose.onNodeWithTag("wallet-record-list").performScrollToNode(hasText(text)) }

    private fun screenshot(name: String) {
        compose.waitForIdle()
        val fd = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("screencap -p /sdcard/Download/$name")
        ParcelFileDescriptor.AutoCloseInputStream(fd).use { it.readBytes() }
    }
}
