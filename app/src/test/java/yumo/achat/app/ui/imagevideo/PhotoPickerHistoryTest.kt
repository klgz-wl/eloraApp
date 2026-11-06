package yumo.achat.app.ui.imagevideo

import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoPickerHistoryTest {
    @Test fun `recent history retains only distinct selected local references and stays bounded`() {
        assertEquals(listOf("content://selected/2", "content://selected/1"),
            recordPickerPhotoHistory(listOf("content://selected/1", "content://selected/2"), "content://selected/2"))
        assertEquals(emptyList<String>(), recordPickerPhotoHistory(emptyList(), ""))
        val history = (0..15).fold(emptyList<String>()) { list, id -> recordPickerPhotoHistory(list, "content://selected/$id") }
        assertEquals(12, history.size)
        assertEquals("content://selected/15", history.first())
    }
}
