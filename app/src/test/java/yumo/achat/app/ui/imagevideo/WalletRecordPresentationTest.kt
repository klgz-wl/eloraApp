package yumo.achat.app.ui.imagevideo

import java.util.TimeZone
import java.util.Locale
import org.junit.Assert.*
import org.junit.Test

class WalletRecordPresentationTest {
    @Test fun `record amount preserves income expense and minimum integer without overflow`() {
        assertEquals("+40.00", formatWalletRecordAmount(40, "income", Locale.US))
        assertEquals("-14.00", formatWalletRecordAmount(14, "expense", Locale.US))
        assertEquals("-2147483648.00", formatWalletRecordAmount(Int.MIN_VALUE, "expense", Locale.US))
        assertEquals("-3.00", formatWalletRecordAmount(-3, "unknown", Locale.US))
    }
    @Test fun `server timestamp renders date and time without inventing missing dates`() {
        assertEquals("17:55 09/04/2026", walletRecordDate("2026-04-09T09:55:00Z", TimeZone.getTimeZone("Asia/Shanghai")))
        assertEquals("17:55 09/04/2026", walletRecordDate("2026-04-09T17:55:00", TimeZone.getTimeZone("Asia/Shanghai")))
        assertEquals("17:55 09/04/2026", walletRecordDate("2026-04-09T09:55:00.123456Z", TimeZone.getTimeZone("Asia/Shanghai")))
        assertEquals("", walletRecordDate("", TimeZone.getTimeZone("UTC")))
        assertEquals("unknown", walletRecordDate("unknown", TimeZone.getTimeZone("UTC")))
    }
}
