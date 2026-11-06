package yumo.achat.app.ui.imagevideo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import yumo.achat.app.R
import yumo.achat.app.ui.theme.*
import java.text.NumberFormat

@Composable
internal fun CreditsPurchaseScreen(
    balance: Int, packs: List<CreditPack>, selectedId: String?, selectionEnabled: Boolean,
    loading: Boolean, error: String?, actionLabel: String, actionEnabled: Boolean,
    onSelect: (String) -> Unit, onPurchase: () -> Unit, onRetryCatalog: () -> Unit, onBack: () -> Unit,
    status: @Composable () -> Unit, onHistory: () -> Unit = {},
) {
    BackHandler(onBack = onBack)
    Column(Modifier.fillMaxSize().background(EloraCatalogBackground).drawBehind {
        drawRect(Brush.radialGradient(listOf(EloraWelcomeViolet.copy(alpha = .28f), Color.Transparent),
            center = Offset(size.width * .5f, 0f), radius = size.width * 1.2f))
        drawRect(Brush.radialGradient(listOf(EloraWelcomeCyan.copy(alpha = .10f), Color.Transparent),
            center = Offset(size.width, size.height * .6f), radius = size.width))
    }.safeDrawingPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            CreditsIcon(R.drawable.elora_credits_back, stringResource(R.string.credits_back), onBack)
            Row(Modifier.clip(CircleShape).background(Brush.horizontalGradient(listOf(EloraWelcomeViolet.copy(alpha = .3f), EloraWelcomeMagenta.copy(alpha = .3f))))
                .border(1.dp, EloraWelcomeLilac.copy(alpha = .3f), CircleShape).padding(horizontal = 17.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Image(painterResource(R.drawable.elora_credits_token), null, Modifier.size(20.dp))
                Text(balance.toString(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            CreditsIcon(R.drawable.elora_credits_history, stringResource(R.string.credits_history), onHistory)
        }
        LazyVerticalGrid(columns = GridCells.Fixed(3), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth().weight(1f)) {
            item(span = { GridItemSpan(3) }, key = "hero") {
                Column(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.credits_title), color = EloraWelcomeSoftLilac, fontSize = 30.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text(stringResource(R.string.credits_styles), color = EloraAccountLabel, fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp))
                    Text(stringResource(R.string.credits_speed), color = EloraAccountLabel, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                }
            }
            when {
                loading -> item(span = { GridItemSpan(3) }) { Text(stringResource(R.string.top_up_loading), color = EloraWelcomeMuted) }
                error != null -> item(span = { GridItemSpan(3) }) {
                    Column { Text(error, color = EloraWelcomeError, fontSize = 12.sp)
                        Text(stringResource(R.string.top_up_retry), color = EloraWelcomeLilac,
                            modifier = Modifier.clickable(role = Role.Button, onClick = onRetryCatalog).padding(12.dp)) }
                }
                packs.isEmpty() -> item(span = { GridItemSpan(3) }) { Text(stringResource(R.string.top_up_empty), color = EloraWelcomeMuted) }
                else -> items(packs, key = { it.id }) { pack -> CreditsPack(pack, pack.id == selectedId, selectionEnabled) { onSelect(pack.id) } }
            }
        }
        // Recovery remains outside the catalog so a missing product never hides an owned order.
        Column(Modifier.fillMaxWidth().heightIn(max = 180.dp).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) { status() }
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp).heightIn(min = 56.dp).clip(CircleShape)
            .background(Brush.horizontalGradient(listOf(EloraAccountButtonStart, EloraPreviewButtonEnd, EloraWelcomeMagenta)))
            .border(1.dp, Color.White.copy(alpha = .2f), CircleShape)
            .clickable(enabled = actionEnabled, role = Role.Button, onClick = onPurchase).padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(actionLabel, color = Color.White.copy(alpha = if (actionEnabled) 1f else .65f), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            Spacer(Modifier.width(8.dp)); Image(painterResource(R.drawable.elora_credits_arrow), null, Modifier.size(20.dp))
        }
    }

}

@Composable
private fun CreditsIcon(icon: Int, description: String, onClick: () -> Unit) {
    Box(Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = .05f))
        .border(1.dp, Color.White.copy(alpha = .1f), CircleShape).clickable(role = Role.Button, onClick = onClick)
        .semantics { contentDescription = description }, contentAlignment = Alignment.Center) {
        Image(painterResource(icon), null, Modifier.size(20.dp))
    }
}

@Composable
private fun CreditsPack(pack: CreditPack, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val accent = if (selected) EloraWelcomeMagenta else pack.accentColor
    Box(Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Column(Modifier.fillMaxWidth().heightIn(min = 166.dp).clip(RoundedCornerShape(16.dp))
            .background(if (selected) Brush.verticalGradient(listOf(EloraCreditsSelected, EloraPreviewSurface))
                else Brush.verticalGradient(listOf(EloraCreditsSurface, EloraCreditsSurface)))
            .border(if (selected) 2.dp else 1.dp, if (selected) accent else Color.White.copy(alpha = .1f), RoundedCornerShape(16.dp))
            .selectable(selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.fillMaxWidth().height(16.dp), contentAlignment = Alignment.TopEnd) {
                if (selected) Image(painterResource(R.drawable.elora_credits_check), null,
                    Modifier.size(16.dp).clip(CircleShape).background(accent).padding(3.dp))
            }
            Box(Modifier.size(32.dp).clip(CircleShape).background(accent.copy(alpha = .13f))
                .border(1.dp, accent.copy(alpha = .3f), CircleShape), contentAlignment = Alignment.Center) {
                Image(painterResource(if (selected) R.drawable.elora_credits_selected_token else R.drawable.elora_credits_standard_token), null, Modifier.size(16.dp))
            }
            Text(NumberFormat.getIntegerInstance().format(pack.credits), color = Color.White, fontSize = 16.sp,
                fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
            pack.bonus?.let { Text(stringResource(R.string.credits_bonus, it), color = EloraWelcomeLilac, fontSize = 10.sp, textAlign = TextAlign.Center) }
            if (pack.description.isNotBlank()) Text(pack.description, color = EloraAccountMuted, fontSize = 10.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(accent.copy(alpha = .15f)))
            pack.originalPrice?.let { Text(it, color = EloraAccountHint, fontSize = 12.sp,
                textDecoration = TextDecoration.LineThrough, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp)) }
            Text(pack.price, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
        }
        val badge = pack.badgeText ?: pack.discountPercent?.let { stringResource(R.string.credits_discount, it) }
        if (!badge.isNullOrBlank()) Text(badge, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.TopCenter).offset(y = (-10).dp).clip(CircleShape)
                .background(if (selected) EloraWelcomeMagenta else EloraWelcomeViolet).padding(horizontal = 8.dp, vertical = 4.dp))
    }
}
