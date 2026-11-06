package yumo.achat.app.ui.imagevideo

import org.junit.Assert.*
import org.junit.Test

class GenerationModeSelectionTest {
    @Test fun `payment return snapshot can preserve mode quote and media through saved state`() {
        val original = generationSelectionForMode(template(), "quality")!!
        val bytes = java.io.ByteArrayOutputStream().use { output ->
            java.io.ObjectOutputStream(output).use { it.writeObject(original) }
            output.toByteArray()
        }
        val restored = java.io.ObjectInputStream(java.io.ByteArrayInputStream(bytes)).use { it.readObject() as SelectedGenerationTemplate }
        assertEquals(original, restored)
        assertEquals("quality", restored.quality)
        assertEquals(7, restored.quotedDiamondCost)
    }
    private fun template(fast: Int? = 2, quality: Int? = 7) = SelectedGenerationTemplate(
        "template", "image", "fast", "Portrait", TemplatePreviewMedia.RemoteImage("https://example.test/image.png"),
        0, "category", "image", 2, fastPrice = fast, qualityPrice = quality)

    @Test fun `only quoted modes are selectable and quality is preferred`() {
        assertEquals("quality", defaultGenerationMode(generationModes(template())))
        assertEquals("fast", defaultGenerationMode(generationModes(template(2, null))))
        assertTrue(generationModes(template(null, -1)).isEmpty())
        assertNull(generationSelectionForMode(template(2, null), "quality"))
    }

    @Test fun `selection freezes actual price and retains modality and source`() {
        val selected = generationSelectionForMode(template(), "quality")!!
        assertEquals("quality", selected.quality)
        assertEquals(7, selected.quotedDiamondCost)
        assertEquals("image", selected.modality)
        assertEquals("image", selected.source)
        assertEquals("template", selected.templateId)
        assertFalse(shouldShowGenerationMode(null, template()))
        assertFalse(shouldShowGenerationMode("", template()))
        assertTrue(shouldShowGenerationMode("resource-id", template()))
    }
}
