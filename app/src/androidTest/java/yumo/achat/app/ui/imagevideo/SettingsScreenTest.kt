package yumo.achat.app.ui.imagevideo

import android.os.ParcelFileDescriptor
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import yumo.achat.app.BuildConfig
import yumo.achat.app.ui.theme.AchatTheme
import yumo.achat.core.backend.PagedResult

class SettingsScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun meGearOpensSettingsAndChildPagesReturnToTheirSource() {
        var open by mutableStateOf(false)
        var filter by mutableStateOf(MeCreationFilter.Creations)
        var policyUrl = ""
        compose.setContent {
            AchatTheme {
                if (!open) MeCreationsRoute("user", "Elora tester", null, 2, false, "",
                    loadPage = { _, page -> PagedResult(emptyList(), page, 20, 0) }, onEditName = {}, onEditAvatar = {},
                    onSettings = { open = true }, onOpen = {}, onNavigation = {}, initialFilter = filter, onFilterChanged = { filter = it })
                else SettingsRoute(false, null, onBack = { open = false }, onLogout = {}, onDeleteAccount = {},
                    policyContent = { url, back ->
                        policyUrl = url
                        BackHandler(onBack = back)
                        Text("Policy fixture")
                    })
            }
        }
        compose.onNodeWithText("Images").performClick()
        compose.onNodeWithContentDescription("Settings").performClick()
        compose.onNodeWithText("LEGAL & PRIVACY").assertIsDisplayed()
        compose.onNodeWithText("APPLICATION & SUPPORT").assertIsDisplayed()
        compose.onNodeWithText("Privacy Policy").performClick()
        compose.runOnIdle { assertEquals(BuildConfig.PRIVACY_POLICY_URL, policyUrl) }
        back()
        compose.onNodeWithText("Term of Use").performClick()
        compose.runOnIdle { assertEquals(BuildConfig.TERMS_OF_SERVICE_URL, policyUrl) }
        back()
        compose.onNodeWithText("About Elora").performClick()
        compose.onNodeWithText("Create images and videos with Elora. More information will be available before release.").assertIsDisplayed()
        compose.onNodeWithText("About Vexora").assertDoesNotExist()
        screenshot("elora-about-page.png")
        compose.onNodeWithContentDescription("Back to Settings").performClick()
        compose.onNodeWithText("Contact Us").performClick()
        compose.onNodeWithText("Contact support using the email address below.").assertIsDisplayed()
        compose.onNodeWithText(BuildConfig.CONTACT_EMAIL).assertIsDisplayed()
        screenshot("elora-contact-page.png")
        compose.onNodeWithText("Copy email").assertDoesNotExist()
        compose.onNodeWithText("Feedback").assertDoesNotExist()
        back()
        compose.onNodeWithContentDescription("Back to Me").performClick()
        compose.onNodeWithText("Images").assertIsSelected()
    }

    @Test fun signOutAndDeletionKeepTheirCallbacksAndBusyState() {
        var busy by mutableStateOf(false)
        var error by mutableStateOf<String?>(null)
        var logout = 0
        var deletion = 0
        compose.setContent { AchatTheme { SettingsRoute(busy, error, {}, { logout++ }, { deletion++ }) } }
        compose.onNodeWithText("Sign out").assertIsEnabled()
        compose.onNodeWithText("Delete Account").assertIsEnabled()
        val screenshot = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("screencap -p /sdcard/Download/elora-settings.png")
        ParcelFileDescriptor.AutoCloseInputStream(screenshot).use { it.readBytes() }
        compose.onNodeWithText("Sign out").performClick()
        compose.onNodeWithText("Delete Account").performClick()
        compose.runOnIdle { assertEquals(1, logout); assertEquals(1, deletion); busy = true }
        compose.onNodeWithText("Delete Account").assertIsNotEnabled().performClick()
        compose.onNodeWithText("Logging out…").assertIsNotEnabled().performClick()
        compose.runOnIdle { assertEquals(1, logout); assertEquals(1, deletion); busy = false; error = "Account operation unavailable (E-203)" }
        compose.onNodeWithText("Account operation unavailable (E-203)").assertIsDisplayed()
    }

    @Test fun emptyConfiguredAddressesAreHidden() {
        compose.setContent { AchatTheme {
            SettingsRoute(false, null, {}, {}, {}, configuration = SettingsConfiguration("", "", "test-version", ""))
        } }
        compose.onNodeWithText("Privacy Policy").assertDoesNotExist()
        compose.onNodeWithText("Term of Use").assertDoesNotExist()
        compose.onNodeWithText("Contact Us").assertDoesNotExist()
        compose.onNodeWithText("Version test-version").assertIsDisplayed()
    }

    @Test fun shortScreenAndLargeTextKeepSettingsAndAccountActionsReachable() {
        compose.setContent { AchatTheme {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(
                    androidx.compose.ui.platform.LocalDensity.current.density, 1.5f)) {
                androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.requiredSize(320.dp, 480.dp)) {
                    SettingsRoute(false, null, {}, {}, {})
                }
            }
        } }
        compose.onNodeWithText("Sign out").assertIsDisplayed()
        compose.onNodeWithText("Contact Us").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Privacy Policy").performScrollTo().assertIsDisplayed()
    }

    @Test fun contactUsesConfiguredEmailAndSurvivesStateRestoration() {
        val restore = androidx.compose.ui.test.junit4.StateRestorationTester(compose)
        restore.setContent { AchatTheme {
            SettingsRoute(false, null, {}, {}, {}, configuration = SettingsConfiguration("", "", "test-version", "support@elora.example"))
        } }
        compose.onNodeWithText("Contact Us").performClick()
        compose.onNodeWithText("support@elora.example").assertIsDisplayed()
        compose.onNodeWithText("dinhducfvuirut3756@gmail.com").assertDoesNotExist()
        restore.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("support@elora.example").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back to Settings").performClick()
        compose.onNodeWithText("APPLICATION & SUPPORT").assertIsDisplayed()
    }

    private fun screenshot(name: String) {
        compose.waitForIdle()
        val fd = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("screencap -p /sdcard/Download/$name")
        ParcelFileDescriptor.AutoCloseInputStream(fd).use { it.readBytes() }
    }

    private fun back() {
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK)
        compose.waitForIdle()
    }
}
