package yumo.achat.app.ui.imagevideo

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class GenerationHomeRoutingTest {
    @get:Rule val compose = createComposeRule()

    @Test fun acceptedGenerationReturnsHomeAndTaskBadgesStayOffVideoAndImageMenus() {
        var navigation by mutableIntStateOf(1)
        var destination by mutableStateOf(ImageToVideoDestination.UploadPhoto)
        var submitting by mutableStateOf(false)
        var completed by mutableStateOf(false)
        compose.setContent {
            val badges = if (completed) listOf(HomeTaskStatusBadge.Completed) else listOf(HomeTaskStatusBadge.Generating)
            when {
                destination == ImageToVideoDestination.UploadPhoto -> GenerationModeScreen(null, TemplatePreviewMedia.Unavailable,
                    listOf(GenerationModeOption("fast", 2)), "fast", submitting, null, {}, { submitting = true }, {}, {})
                navigation == 4 -> HomeVideoFeedScreen(emptyList(), 2, false, null, false, false, null,
                    {}, {}, {}, { _, _ -> }, {}, { navigation = it }, statusContent = { TemplateTaskStatusBadges(navigation, badges) })
                else -> TemplateCatalogScreen(emptyList(), emptyList(), 0, 2, false, null, false, false, null,
                    {}, { _, _ -> }, { _, _ -> }, {}, {}, {}, { navigation = it },
                    statusContent = { TemplateTaskStatusBadges(navigation, badges) },
                    section = if (navigation == 1) TemplateSection.Image else TemplateSection.Video)
            }
        }
        compose.onNode(hasText("Generate") and hasClickAction()).performClick()
        compose.onNodeWithContentDescription("Generate").assertIsNotEnabled()
        compose.onNodeWithText("Trending Templates").assertDoesNotExist()
        // Simulate successful task acceptance; a click or an in-flight request alone does not navigate.
        compose.runOnIdle {
            val route = routeAfterGenerationTaskCreated(navigation)
            navigation = route.selectedNavigation
            destination = route.destination
            submitting = false
        }
        compose.onNodeWithText("Trending Templates").assertIsDisplayed()
        compose.onNodeWithContentDescription("HOME").assertIsSelected()
        compose.onNodeWithText("Generating...").assertIsDisplayed()
        compose.onNodeWithText("My Tasks").assertDoesNotExist()
        compose.onNodeWithText("Record").assertDoesNotExist()
        compose.runOnIdle { completed = true }
        compose.onNodeWithText("Completed").assertIsDisplayed()
        for (menu in listOf("VIDEO", "IMAGE")) {
            compose.onNodeWithContentDescription(menu).performClick().assertIsSelected()
            compose.onNodeWithText("Generating...").assertDoesNotExist()
            compose.onNodeWithText("Completed").assertDoesNotExist()
        }
        compose.onNodeWithContentDescription("HOME").performClick().assertIsSelected()
        compose.onNodeWithText("Completed").assertIsDisplayed()
    }
}
