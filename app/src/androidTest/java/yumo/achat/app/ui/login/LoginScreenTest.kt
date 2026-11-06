package yumo.achat.app.ui.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.performImeAction
import android.os.ParcelFileDescriptor
import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun accountDesignPreservesPasswordToggleAndSubmitsCredentials() {
        var submitted: Pair<String, String>? = null
        composeRule.setContent {
            LoginScreen(false, null, onDeviceLogin = {}, onAccountLogin = { account, password -> submitted = account to password })
        }
        composeRule.onNodeWithText("Sign in or create account").performClick()
        composeRule.onNodeWithText("Sign in to Elora").assertIsDisplayed()
        composeRule.onNodeWithText("ACCOUNT").assertIsDisplayed()
        composeRule.onNodeWithText("PASSWORD").assertIsDisplayed()
        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("validuser")
        composeRule.onAllNodes(hasSetTextAction())[0].performImeAction()
        composeRule.onAllNodes(hasSetTextAction())[1].assertIsFocused()
        composeRule.onAllNodes(hasSetTextAction())[1].performTextInput("secret123")
        composeRule.onNodeWithContentDescription("Show password").performClick()
        composeRule.onNodeWithText("secret123").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Hide password").performClick()
        composeRule.onNodeWithText("Sign In").performScrollTo().performClick()
        composeRule.runOnIdle { assertEquals("validuser" to "secret123", submitted) }
    }

    @Test fun accountOmitsRecoveryAndWorkspaceLinks() {
        composeRule.setContent {
            LoginScreen(false, null, onDeviceLogin = {}, onAccountLogin = { _, _ -> })
        }
        composeRule.onNodeWithText("Sign in or create account").performClick()
        composeRule.onNodeWithText("Sign in to Elora").assertIsDisplayed()
        composeRule.waitForIdle()
        val screenshot = InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("screencap -p /sdcard/Download/elora-account.png")
        ParcelFileDescriptor.AutoCloseInputStream(screenshot).use { it.readBytes() }
        composeRule.onNodeWithText("Forgot password?").assertDoesNotExist()
        composeRule.onNodeWithText("Don’t have an account?").assertDoesNotExist()
        composeRule.onNodeWithText("Don't have an account?").assertDoesNotExist()
        composeRule.onNodeWithText("Create workspace").assertDoesNotExist()
        composeRule.onNodeWithText("Sign In").assertIsDisplayed()
    }

    @Test fun accountDisablesFormAndSubmitWhileBusy() {
        composeRule.setContent {
            AccountLoginScreen("validuser", "secret123", false, true, null,
                onAccountChange = {}, onPasswordChange = {}, onTogglePassword = {}, onSubmit = {}, onBack = {})
        }
        composeRule.onNodeWithContentDescription("Sign In").assertIsNotEnabled()
        composeRule.onNodeWithContentDescription("Enter account or email").assertIsNotEnabled()
        composeRule.onNodeWithContentDescription("Enter password").assertIsNotEnabled()
        composeRule.onNodeWithContentDescription("Show password").assertIsNotEnabled()
    }

    @Test fun accountRemainsUsableWithKeyboardOnShortScreenAndLargeText() {
        var submitted: Pair<String, String>? = null
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 1.5f)) {
                Box(Modifier.requiredSize(320.dp, 480.dp)) {
                    LoginScreen(false, null, onDeviceLogin = {}, onAccountLogin = { account, password -> submitted = account to password })
                }
            }
        }
        composeRule.onNodeWithText("Sign in or create account").performScrollTo().performClick()
        composeRule.onAllNodes(hasSetTextAction())[0].performScrollTo().performTextInput("validuser")
        composeRule.onAllNodes(hasSetTextAction())[0].performImeAction()
        composeRule.onAllNodes(hasSetTextAction())[1].assertIsFocused().performTextInput("secret123")
        composeRule.onAllNodes(hasSetTextAction())[1].performImeAction()
        composeRule.runOnIdle { assertEquals("validuser" to "secret123", submitted) }
    }

    @Test fun accountSubmissionStillRequiresWelcomeConsent() {
        var attempts = 0
        composeRule.setContent {
            LoginScreen(false, null, onDeviceLogin = {}, onAccountLogin = { _, _ -> attempts++ })
        }
        composeRule.onNodeWithContentDescription("Agree to the Terms of Service and Privacy Policy").performClick()
        composeRule.onNodeWithText("Sign in or create account").performClick()
        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("validuser")
        composeRule.onAllNodes(hasSetTextAction())[1].performTextInput("secret123")
        composeRule.onAllNodes(hasSetTextAction())[1].performImeAction()
        composeRule.onNodeWithText("Please agree to the Terms of Service and Privacy Policy first. (E-201)")
            .performScrollTo().assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(0, attempts) }
    }

    @Test fun welcomeShowsEloraDesignAndReturnsFromAccount() {
        composeRule.setContent {
            LoginScreen(false, null, onDeviceLogin = {}, onAccountLogin = { _, _ -> })
        }
        composeRule.onNodeWithContentDescription("Elora").assertIsDisplayed()
        composeRule.onNodeWithText("morph your reality,").assertIsDisplayed()
        composeRule.onNodeWithText("redefine yourself").assertIsDisplayed()
        composeRule.onNodeWithText("4K Ultra-Real Swap").assertIsDisplayed()
        composeRule.onNodeWithText("Instant AI Render").assertIsDisplayed()
        composeRule.onNodeWithText("Private & Encrypted").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Agree to the Terms of Service and Privacy Policy").assertIsOn()
        val screenshot = InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("screencap -p /sdcard/Download/elora-welcome.png")
        ParcelFileDescriptor.AutoCloseInputStream(screenshot).use { it.readBytes() }
        composeRule.onNodeWithText("Sign in or create account").performClick()
        composeRule.onNodeWithContentDescription("Back").performClick()
        composeRule.onNodeWithText("Login with device").assertIsDisplayed()
        composeRule.onNodeWithText("4K Ultra-Real Swap").assertIsDisplayed()
    }

    @Test fun welcomeDisablesBothLoginActionsWhileSubmitting() {
        var attempts = 0
        composeRule.setContent {
            LoginScreen(true, null, onDeviceLogin = { attempts++ }, onAccountLogin = { _, _ -> })
        }
        composeRule.onNodeWithContentDescription("Login with device").assertIsNotEnabled()
        composeRule.onNodeWithText("Sign in or create account").assertIsNotEnabled()
        composeRule.runOnIdle { assertEquals(0, attempts) }
    }

    @Test fun welcomeKeepsActionsAndConsentReachableOnShortScreenWithLargeText() {
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 1.5f)) {
                Box(Modifier.requiredSize(320.dp, 480.dp)) {
                    LoginScreen(false, null, onDeviceLogin = {}, onAccountLogin = { _, _ -> })
                }
            }
        }
        composeRule.onNodeWithText("Login with device").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Sign in or create account").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Agree to the Terms of Service and Privacy Policy")
            .performScrollTo().assertIsDisplayed().assertIsOn()
    }

    @Test fun missingAgreementDisplaysE201AndBlocksDeviceAuthentication() {
        var attempts = 0
        composeRule.setContent {
            LoginScreen(false, null, onDeviceLogin = { attempts++ }, onAccountLogin = { _, _ -> })
        }
        composeRule.onNodeWithContentDescription("Agree to the Terms of Service and Privacy Policy").performClick()
        composeRule.onNodeWithText("Login with device").performClick()
        composeRule.onNodeWithText("Please agree to the Terms of Service and Privacy Policy first. (E-201)")
            .assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(0, attempts) }
    }

    @Test fun accountAndPasswordValidationKeepDistinctDiagnosticsAndBlockSubmission() {
        var attempts = 0
        composeRule.setContent {
            LoginScreen(false, null, onDeviceLogin = {}, onAccountLogin = { _, _ -> attempts++ })
        }
        composeRule.onNodeWithText("Sign in or create account").performClick()
        composeRule.onNodeWithText("Sign In").performClick()
        composeRule.onNodeWithText("Account must have 3–50 characters without spaces. (E-202)").assertIsDisplayed()
        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("validuser")
        composeRule.onNodeWithText("Sign In").performClick()
        composeRule.onNodeWithText("Password must have 6–18 characters without spaces. (E-203)").assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(0, attempts) }
    }

    @Test fun choiceOpensAccountFormAndReturns() {
        composeRule.setContent {
            LoginScreen(
                submitting = false,
                serverError = null,
                onDeviceLogin = {},
                onAccountLogin = { _, _ -> },
            )
        }
        composeRule.onNodeWithText("Login with device").assertIsDisplayed()
        composeRule.onNodeWithText("Sign in or create account").performClick()
        composeRule.onNodeWithText("Enter account or email").assertIsDisplayed()
        composeRule.onNodeWithText("Enter password").assertIsDisplayed()
        composeRule.onNodeWithText("Sign In").assertIsDisplayed()
    }
}
