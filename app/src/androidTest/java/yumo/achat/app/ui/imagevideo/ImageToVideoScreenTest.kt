package yumo.achat.app.ui.imagevideo

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import yumo.achat.app.MainActivity

@RunWith(AndroidJUnit4::class)
class ImageToVideoScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun enterAuthenticatedHome() {
        composeRule.waitUntil(30_000) {
            composeRule.onAllNodesWithText("Login with device").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("Trending Templates").fetchSemanticsNodes().isNotEmpty()
        }
        if (composeRule.onAllNodesWithText("Login with device").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithText("Login with device").performClick()
        }
        composeRule.waitUntil(60_000) { composeRule.onAllNodesWithText("Trending Templates").fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithContentDescription("VIDEO").performClick()
        composeRule.onNodeWithContentDescription("Video templates").assertIsDisplayed()
    }

    private fun assumeUsableLiveTemplate() {
        val available = runCatching {
            composeRule.waitUntil(20_000) {
                composeRule.onAllNodesWithTag("video-template-card").fetchSemanticsNodes().isNotEmpty() ||
                    composeRule.onAllNodesWithTag("image-template-card").fetchSemanticsNodes().isNotEmpty()
            }
        }.isSuccess
        assumeTrue("Requires a usable live template from the development catalog", available)
    }

    private fun visibleControlIndex(description: String): Int {
        val height = composeRule.activity.window.decorView.height.toFloat()
        return composeRule.onAllNodesWithContentDescription(description).fetchSemanticsNodes()
            .indexOfFirst { node -> node.boundsInRoot.top >= 0f && node.boundsInRoot.bottom <= height }
    }

    @Test
    fun imageToVideoDesignIsDisplayed() {
        composeRule.onNodeWithContentDescription("Video templates").assertIsDisplayed()
        composeRule.onNodeWithText("All").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("VIDEO").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("IMAGE").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("HOME").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("ME").assertIsDisplayed()
    }

    @Test
    fun allCategoryIsSelectedWhenTapped() {
        composeRule.onNodeWithText("All").performClick()
        composeRule.onNodeWithText("All").assertIsSelected()
    }

    @Test
    fun templateCardOpensUploadPhoto() {
        assumeUsableLiveTemplate()
        composeRule.onAllNodesWithTag("video-template-card")[0].performClick()
        composeRule.onNodeWithText("Upload Photo").assertIsDisplayed()
    }

    @Test
    fun videoCatalogScrollKeepsNavigationAccessible() {
        assumeUsableLiveTemplate()
        composeRule.onNodeWithTag("video-catalog-grid").performTouchInput { swipeUp() }
        composeRule.onNodeWithContentDescription("VIDEO").assertIsSelected()
        composeRule.onNodeWithContentDescription("IMAGE").assertIsDisplayed()
    }

    @Test
    fun imageNavigationBecomesSelectedWhenTapped() {
        composeRule.onNode(hasText("IMAGE") or hasContentDescription("IMAGE")).performClick()
        composeRule.onNode(hasText("IMAGE") or hasContentDescription("IMAGE")).assertIsSelected()
    }

    @Test
    fun imageNavigationShowsImageToImageDesign() {
        composeRule.onNode(hasText("IMAGE") or hasContentDescription("IMAGE")).performClick()

        composeRule.onNodeWithContentDescription("Image templates").assertIsDisplayed()
        composeRule.onNodeWithText("All").assertIsDisplayed()
    }

    @Test
    fun catalogBalanceOpensWalletRecord() {
        composeRule.onNodeWithContentDescription("Balance:", substring = true).performClick()

        composeRule.onNodeWithText("Record").assertIsDisplayed()
    }

    @Test
    fun meNavigationOpensConfiguredSettings() {
        composeRule.onNode(hasText("ME") or hasContentDescription("ME")).performClick()
        composeRule.onNodeWithContentDescription("Settings").performClick()
        composeRule.onNodeWithText("Settings").assertIsDisplayed()
        composeRule.onNodeWithText("LEGAL & PRIVACY").assertIsDisplayed()
        composeRule.onNodeWithText("APPLICATION & SUPPORT").assertIsDisplayed()
        composeRule.onNodeWithText("Delete Account").assertIsDisplayed()
        composeRule.onNodeWithText("Sign out").assertIsDisplayed()
        composeRule.onNodeWithText("About Elora").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Back to Me").performClick()
        composeRule.onNode(hasText("ME") or hasContentDescription("ME")).assertIsSelected()
    }

    @Test
    fun deleteAccountRequiresCountdownAndCanBeCancelled() {
        composeRule.onNode(hasText("ME") or hasContentDescription("ME")).performClick()
        composeRule.onNodeWithContentDescription("Settings").performClick()
        composeRule.onNodeWithText("Delete Account").performClick()

        composeRule.onNodeWithText("Tips").assertIsDisplayed()
        composeRule.onNodeWithText("Delete Account (3)").assertIsNotEnabled()
        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.onNodeWithText("Sign out").assertIsDisplayed()
    }

    @Test
    fun aboutOpensBrandedInformationPage() {
        composeRule.onNode(hasText("ME") or hasContentDescription("ME")).performClick()
        composeRule.onNodeWithContentDescription("Settings").performClick()
        composeRule.onNodeWithText("About Elora").performClick()
        composeRule.onNodeWithText("Create images and videos with Elora. More information will be available before release.").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Back to Settings").performClick()
        composeRule.onNodeWithText("APPLICATION & SUPPORT").assertIsDisplayed()
    }

    @Test
    fun contactOpensConfiguredEmailInformationPage() {
        composeRule.onNode(hasText("ME") or hasContentDescription("ME")).performClick()
        composeRule.onNodeWithContentDescription("Settings").performClick()
        composeRule.onNodeWithText("Contact Us").performClick()
        composeRule.onNodeWithText("Contact support using the email address below.").assertIsDisplayed()
        composeRule.onNodeWithText(yumo.achat.app.BuildConfig.CONTACT_EMAIL).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Back to Settings").performClick()
        composeRule.onNodeWithText("APPLICATION & SUPPORT").assertIsDisplayed()
    }

    @Test
    fun editNameOpensNamePickerSheet() {
        composeRule.onNode(hasText("ME") or hasContentDescription("ME")).performClick()
        composeRule.onNodeWithContentDescription(composeRule.activity.getString(yumo.achat.app.R.string.profile_edit_description)).performClick()

        composeRule.onNodeWithText("Edit display name").assertIsDisplayed()
        composeRule.onNodeWithText("Display name").assertIsDisplayed()
        composeRule.onNodeWithText("SAVE").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Close name picker").performClick()
        composeRule.onNodeWithText("Creations").assertIsDisplayed()
    }

    @Test
    fun useTemplateOpensUploadPhotoScreen() {
        assumeUsableLiveTemplate()
        composeRule.onAllNodesWithTag("video-template-card")[0].performClick()

        composeRule.onNodeWithText("Upload Photo").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Selected template preview").assertIsDisplayed()
    }

    @Test
    fun imageTemplateOpensUploadPhotoScreenWithImageNavigationSelected() {
        composeRule.onNode(hasText("IMAGE") or hasContentDescription("IMAGE")).performClick()
        assumeUsableLiveTemplate()
        composeRule.onAllNodesWithTag("image-template-card")[0].performClick()

        composeRule.onNodeWithText("Upload Photo").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Selected template preview").assertIsDisplayed()
    }

    @Test
    fun backFromUploadPhotoReturnsToTemplateBrowser() {
        assumeUsableLiveTemplate()
        composeRule.onAllNodesWithTag("video-template-card")[0].performClick()
        composeRule.onNodeWithContentDescription("Back to templates").performClick()

        composeRule.onNodeWithContentDescription("Video templates").assertIsDisplayed()
        composeRule.onNodeWithText("All").assertIsDisplayed()
    }

}
