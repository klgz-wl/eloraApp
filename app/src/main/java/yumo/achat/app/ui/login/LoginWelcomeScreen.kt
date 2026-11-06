package yumo.achat.app.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import yumo.achat.app.R
import yumo.achat.app.ui.theme.*

@Composable
internal fun LoginWelcomeScreen(
    agreed: Boolean,
    submitting: Boolean,
    error: String?,
    onAgreementChange: (Boolean) -> Unit,
    onDeviceLogin: () -> Unit,
    onAccount: () -> Unit,
    onTerms: () -> Unit,
    onPrivacy: () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(EloraWelcomeBackground).drawBehind {
        drawRect(Brush.radialGradient(
            listOf(EloraWelcomeViolet.copy(alpha = .28f), Color.Transparent),
            center = Offset(size.width * .5f, size.height * .20f), radius = size.width * 1.15f,
        ))
        drawRect(Brush.radialGradient(
            listOf(EloraWelcomeCyan.copy(alpha = .09f), Color.Transparent),
            center = Offset(0f, size.height * .48f), radius = size.width * .8f,
        ))
        drawRect(Brush.radialGradient(
            listOf(EloraWelcomeMagenta.copy(alpha = .19f), Color.Transparent),
            center = Offset(size.width, size.height * .88f), radius = size.width * 1.05f,
        ))
    }) {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            // Keep the hero and footer separated on tall screens; scroll on short screens.
            val freeSpace = (maxHeight - 549.dp).coerceAtLeast(0.dp)
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height((freeSpace * .52f).coerceAtLeast(24.dp)))
                WelcomeHero()
                Spacer(Modifier.height((freeSpace * .48f).coerceAtLeast(32.dp)))
                WelcomeAction(
                    label = stringResource(R.string.login_device), icon = R.drawable.elora_login_device,
                    primary = true, busy = submitting, enabled = !submitting, onClick = onDeviceLogin,
                )
                Spacer(Modifier.height(16.dp))
                WelcomeAction(
                    label = stringResource(R.string.login_account_option), icon = R.drawable.elora_login_account,
                    primary = false, busy = false, enabled = !submitting, onClick = onAccount,
                )
                Spacer(Modifier.height(24.dp))
                WelcomeConsent(agreed, onAgreementChange, onTerms, onPrivacy)
                error?.let {
                    Text(it, color = EloraWelcomeError, fontSize = 12.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun WelcomeHero() {
    EloraLoginOrb()
    Spacer(Modifier.height(28.dp))
    Text(stringResource(R.string.login_slogan_first), color = EloraWelcomeText,
        fontSize = 21.sp, lineHeight = 29.sp, letterSpacing = (-.525).sp, textAlign = TextAlign.Center)
    Text(stringResource(R.string.login_slogan_second), color = Color.White, fontWeight = FontWeight.Bold,
        fontSize = 21.sp, lineHeight = 29.sp, letterSpacing = (-.525).sp, textAlign = TextAlign.Center)
    Spacer(Modifier.height(12.dp))
    Row(
        Modifier.clip(CircleShape).background(Color.White.copy(alpha = .05f))
            .border(1.dp, Color.White.copy(alpha = .1f), CircleShape)
            .padding(horizontal = 16.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(8.dp).background(EloraWelcomeCyan, CircleShape))
        Text(stringResource(R.string.login_welcome_studio), color = EloraWelcomeText,
            fontSize = 10.sp, letterSpacing = 1.25.sp, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center, modifier = Modifier.weight(1f, fill = false))
    }
    Spacer(Modifier.height(24.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        FeatureChip(R.string.login_feature_swap, R.string.login_feature_swap_symbol, EloraWelcomeLilac)
        FeatureChip(R.string.login_feature_render, R.string.login_feature_render_symbol, EloraWelcomeCyan)
    }
    Spacer(Modifier.height(8.dp))
    FeatureChip(R.string.login_feature_private, R.string.login_feature_private_symbol, EloraWelcomeLilac)
}

@Composable
private fun FeatureChip(label: Int, symbol: Int, accent: Color) {
    Row(Modifier.clip(CircleShape).background(EloraWelcomeSurface.copy(alpha = .7f))
        .border(1.dp, accent.copy(alpha = .2f), CircleShape)
        .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(stringResource(symbol), color = accent, fontSize = 11.sp)
        Text(stringResource(label), color = EloraWelcomeMuted, fontSize = 11.sp, lineHeight = 16.sp)
    }
}

@Composable
private fun WelcomeAction(label: String, icon: Int, primary: Boolean, busy: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    val background = if (primary) Brush.horizontalGradient(listOf(EloraWelcomeViolet, EloraWelcomePurple, EloraWelcomeMagenta))
        else Brush.horizontalGradient(listOf(EloraWelcomeSurface, EloraWelcomeSurface))
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).clip(shape).background(background)
            .border(1.dp, if (primary) Color.White.copy(alpha = .18f) else EloraWelcomePurple.copy(alpha = .35f), shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { if (busy) contentDescription = label }
            .padding(horizontal = 20.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
    ) {
        if (busy) {
            CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Image(painterResource(icon), contentDescription = null, Modifier.size(if (primary) 20.dp else 16.dp))
            Spacer(Modifier.width(if (primary) 12.dp else 10.dp))
            Text(label, color = if (primary) Color.White else EloraWelcomeSoftLilac,
                fontSize = if (primary) 16.sp else 15.5.sp, letterSpacing = .4.sp,
                fontWeight = if (primary) FontWeight.Bold else FontWeight.Normal, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun WelcomeConsent(agreed: Boolean, onChange: (Boolean) -> Unit, onTerms: () -> Unit, onPrivacy: () -> Unit) {
    val description = stringResource(R.string.login_agreement_checkbox)
    val linkStyle = TextLinkStyles(SpanStyle(color = EloraWelcomeText, textDecoration = TextDecoration.Underline))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Checkbox(checked = agreed, onCheckedChange = onChange,
            modifier = Modifier.size(20.dp).semantics { contentDescription = description },
            colors = CheckboxDefaults.colors(checkedColor = EloraWelcomePurple, checkmarkColor = Color.White,
                uncheckedColor = EloraWelcomeMuted))
        Text(buildAnnotatedString {
            append(stringResource(R.string.login_agreement_intro)); append(" ")
            withLink(LinkAnnotation.Clickable("terms", linkStyle) { onTerms() }) { append(stringResource(R.string.login_terms)) }
            append(" "); append(stringResource(R.string.login_agreement_join)); append(" ")
            withLink(LinkAnnotation.Clickable("privacy", linkStyle) { onPrivacy() }) { append(stringResource(R.string.login_privacy)) }
            append(stringResource(R.string.login_agreement_period))
        }, color = EloraWelcomeMuted, fontSize = 11.5.sp, lineHeight = 16.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
internal fun EloraLoginOrb() {
    Box(Modifier.size(128.dp).drawBehind {
        drawCircle(Brush.radialGradient(
            listOf(EloraWelcomeViolet.copy(alpha = .55f), Color.Transparent),
            radius = size.width * .8f,
        ), radius = size.width * .8f)
    }) {
        Image(
            painterResource(R.drawable.elora_logo), stringResource(R.string.login_brand),
            Modifier.fillMaxSize().clip(CircleShape),
        )
        Box(Modifier.fillMaxSize().clip(CircleShape).background(Brush.verticalGradient(
            listOf(Color.White.copy(alpha = .30f), Color.Transparent, Color.Black.copy(alpha = .40f)),
        )))
    }
}
