package yumo.achat.app.ui.imagevideo

import androidx.compose.ui.layout.ContentScale
import androidx.media3.ui.AspectRatioFrameLayout
import org.junit.Assert.assertEquals
import org.junit.Test

class UploadPreviewDisplayTest {
    @org.junit.Test
    fun `preview remains until picker returns a local image`() {
        org.junit.Assert.assertTrue(shouldShowUploadTemplatePreview(null))
        org.junit.Assert.assertTrue(shouldShowUploadTemplatePreview(""))
        org.junit.Assert.assertFalse(shouldShowUploadTemplatePreview("content://media/images/123"))
    }
    @Test
    fun `upload previews fit full media instead of cropping`() {
        assertEquals(ContentScale.Fit, uploadPreviewContentScale())
        assertEquals(AspectRatioFrameLayout.RESIZE_MODE_FIT, uploadPreviewVideoResizeMode())
    }

    @Test
    fun `upload screen content is scrollable because square previews exceed viewport`() {
        assertEquals(true, uploadScreenContentShouldScroll())
    }
}
