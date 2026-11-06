package yumo.achat.app.ui.imagevideo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import yumo.achat.app.BuildConfig
import yumo.achat.app.R
import yumo.achat.app.ui.login.PolicyWebView
import yumo.achat.app.ui.theme.*

internal data class SettingsConfiguration(
    val privacyUrl: String,
    val termsUrl: String,
    val versionName: String,
    val contactEmail: String,
) {
    companion object {
        fun current() = SettingsConfiguration(BuildConfig.PRIVACY_POLICY_URL.trim(), BuildConfig.TERMS_OF_SERVICE_URL.trim(),
            BuildConfig.VERSION_NAME, BuildConfig.CONTACT_EMAIL.trim())
    }
}

private enum class SettingsPage { Main, Privacy, Terms, About, Contact }

@Composable
internal fun SettingsRoute(
    accountActionBusy: Boolean, accountActionError: String?, onBack: () -> Unit,
    onLogout: () -> Unit, onDeleteAccount: () -> Unit,
    configuration: SettingsConfiguration = SettingsConfiguration.current(),
    policyContent: @Composable (String, () -> Unit) -> Unit = { url, back -> PolicyWebView(url, back) },
) {
    var page by rememberSaveable { mutableStateOf(SettingsPage.Main) }
    BackHandler { if (page == SettingsPage.Main) onBack() else page = SettingsPage.Main }
    if (page == SettingsPage.Privacy || page == SettingsPage.Terms) {
        policyContent(if (page == SettingsPage.Privacy) configuration.privacyUrl else configuration.termsUrl) { page = SettingsPage.Main }
        return
    }
    if (page == SettingsPage.About || page == SettingsPage.Contact) {
        val about = page == SettingsPage.About
        SettingsInformationScreen(
            title = stringResource(if (about) R.string.settings_about else R.string.settings_contact),
            body = if (about) stringResource(R.string.settings_about_body, stringResource(R.string.app_name))
                else stringResource(R.string.settings_contact_body),
            email = if (about) null else configuration.contactEmail.takeIf(String::isNotBlank),
            onBack = { page = SettingsPage.Main },
        )
        return
    }
    SettingsScreen(configuration, accountActionBusy, accountActionError, onBack, onLogout, onDeleteAccount,
        onPrivacy = { page = SettingsPage.Privacy }, onTerms = { page = SettingsPage.Terms },
        onAbout = { page = SettingsPage.About }, onContact = { page = SettingsPage.Contact })
}

@Composable
internal fun SettingsScreen(
    configuration: SettingsConfiguration, busy: Boolean, error: String?, onBack: () -> Unit,
    onLogout: () -> Unit, onDelete: () -> Unit, onPrivacy: () -> Unit, onTerms: () -> Unit,
    onAbout: () -> Unit, onContact: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(EloraWelcomeBackground).drawBehind {
        drawRect(Brush.radialGradient(listOf(EloraAccountButtonStart.copy(alpha = .24f), Color.Transparent),
            center = Offset(size.width * .2f, 0f), radius = size.width * 1.3f))
        drawRect(Brush.radialGradient(listOf(EloraAccountButtonMiddle.copy(alpha = .10f), Color.Transparent),
            center = Offset(size.width * 1.2f, size.height * .6f), radius = size.width * .9f))
    }.safeDrawingPadding()) {
        Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            val back = stringResource(R.string.me_settings_back)
            Box(Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = .05f))
                .border(1.dp, Color.White.copy(alpha = .09f), CircleShape)
                .clickable(role = Role.Button, onClick = onBack).semantics { contentDescription = back }, contentAlignment = Alignment.Center) {
                Image(painterResource(R.drawable.elora_settings_back), null, Modifier.size(20.dp))
            }
            Text(stringResource(R.string.me_settings), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.size(48.dp))
        }
        Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)) {
            if (configuration.privacyUrl.isNotBlank() || configuration.termsUrl.isNotBlank()) SettingsGroup(R.string.settings_legal) {
                if (configuration.privacyUrl.isNotBlank()) SettingsItem(R.drawable.elora_settings_privacy, R.string.login_privacy,
                    EloraSettingsPrivacy, onPrivacy)
                if (configuration.privacyUrl.isNotBlank() && configuration.termsUrl.isNotBlank()) SettingsDivider()
                if (configuration.termsUrl.isNotBlank()) SettingsItem(R.drawable.elora_settings_terms, R.string.settings_terms,
                    EloraSettingsTerms, onTerms)
            }
            SettingsGroup(R.string.settings_application) {
                SettingsItem(R.drawable.elora_settings_about, R.string.settings_about, EloraWelcomePurple, onAbout,
                    trailing = { Text(stringResource(R.string.settings_version, configuration.versionName), color = EloraAccountMuted, fontSize = 12.sp,
                        modifier = Modifier.clip(CircleShape).background(EloraSettingsSurface).border(1.dp, Color.White.copy(alpha = .05f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 5.dp)) })
                if (configuration.contactEmail.isNotBlank()) {
                    SettingsDivider()
                    SettingsItem(R.drawable.elora_settings_contact, R.string.settings_contact, EloraWelcomeCyan, onContact,
                        trailing = { Text(configuration.contactEmail, color = EloraAccountMuted, fontSize = 12.sp,
                            maxLines = 1, overflow = TextOverflow.Ellipsis) })
                }
            }
        }
        Column(Modifier.fillMaxWidth().heightIn(max = 240.dp).verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            error?.let { Text(it, color = EloraWelcomeError, fontSize = 12.sp) }
            Text(stringResource(R.string.account_delete), color = EloraSettingsDanger, fontSize = 12.sp, textDecoration = TextDecoration.Underline,
                modifier = Modifier.heightIn(min = 48.dp).clickable(enabled = !busy, role = Role.Button, onClick = onDelete).padding(14.dp))
            Row(Modifier.fillMaxWidth().heightIn(min = 54.dp)
                .shadow(16.dp, CircleShape, ambientColor = EloraAccountButtonStart, spotColor = EloraWelcomeMagenta).clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(EloraAccountButtonStart, EloraAccountButtonMiddle, EloraWelcomeMagenta)))
                .border(1.dp, Color.White.copy(alpha = .2f), CircleShape).clickable(enabled = !busy, role = Role.Button, onClick = onLogout)
                .padding(horizontal = 24.dp, vertical = 17.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.elora_settings_signout), null, Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(if (busy) R.string.account_logout_busy else R.string.settings_sign_out), color = Color.White,
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SettingsGroup(title: Int, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(stringResource(title), color = EloraAccountMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(EloraSettingsSurface)
            .border(1.dp, Color.White.copy(alpha = .07f), RoundedCornerShape(16.dp)), content = content)
    }
}

@Composable
private fun SettingsItem(icon: Int, label: Int, color: Color, onClick: () -> Unit, trailing: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().heightIn(min = 60.dp).clickable(role = Role.Button, onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(32.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = .16f)), contentAlignment = Alignment.Center) {
            Image(painterResource(icon), null, Modifier.size(16.dp))
        }
        // Weighted labels and trailing content wrap on narrow screens instead of overflowing the card.
        Text(stringResource(label), color = EloraAccountLabel, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Box(Modifier.widthIn(max = 155.dp)) {
            if (trailing != null) trailing() else Image(painterResource(R.drawable.elora_settings_chevron), null, Modifier.size(16.dp))
        }
    }
}

@Composable private fun SettingsDivider() = Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = .05f)))
