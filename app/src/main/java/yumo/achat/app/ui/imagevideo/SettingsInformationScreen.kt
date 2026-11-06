package yumo.achat.app.ui.imagevideo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import yumo.achat.app.R
import yumo.achat.app.ui.theme.EloraAccountLabel
import yumo.achat.app.ui.theme.EloraWelcomeBackground
import androidx.compose.ui.graphics.Color

@Composable
internal fun SettingsInformationScreen(title: String, body: String, email: String?, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(EloraWelcomeBackground).safeDrawingPadding()) {
        val back = stringResource(R.string.settings_information_back)
        Box(Modifier.padding(start = 8.dp, top = 8.dp).size(48.dp)
            .clickable(role = Role.Button, onClick = onBack).semantics { contentDescription = back }, contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.elora_settings_back), null, Modifier.size(24.dp))
        }
        Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp).padding(top = 32.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Text(title, color = Color.White, fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold)
            Text(body, color = EloraAccountLabel, fontSize = 16.sp, lineHeight = 26.sp)
            if (!email.isNullOrBlank()) SelectionContainer {
                Text(email, color = EloraAccountLabel, fontSize = 16.sp, lineHeight = 26.sp)
            }
        }
    }
}
