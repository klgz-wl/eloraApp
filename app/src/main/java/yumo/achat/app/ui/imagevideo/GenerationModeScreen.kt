package yumo.achat.app.ui.imagevideo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import yumo.achat.app.R
import yumo.achat.app.ui.theme.*

@Composable
internal fun GenerationModeScreen(
    localPhoto: Any?, templateMedia: TemplatePreviewMedia, modes: List<GenerationModeOption>, selectedMode: String?,
    busy: Boolean, error: String?, onModeSelect: (String) -> Unit, onGenerate: () -> Unit, onBack: () -> Unit,
    onChangePhoto: () -> Unit, modifier: Modifier = Modifier, playPreview: Boolean = true,
) {
    val selected = modes.firstOrNull { it.quality == selectedMode }
    Column(modifier.fillMaxSize().background(EloraModeBackground).drawBehind {
        drawRect(Brush.radialGradient(listOf(EloraCatalogPurple.copy(alpha = .35f), Color.Transparent),
            center = Offset(size.width * .5f, size.height * .18f), radius = size.width))
    }.safeDrawingPadding()) {
        val back = stringResource(R.string.mode_back)
        Box(Modifier.padding(start = 20.dp, top = 8.dp, bottom = 8.dp).size(48.dp).clip(CircleShape)
            .background(EloraPreviewSurface).border(1.dp, Color.White.copy(alpha = .1f), CircleShape)
            .clickable(enabled = !busy, role = Role.Button, onClick = onBack).semantics { contentDescription = back }, contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.elora_account_back), null, Modifier.size(20.dp))
        }
        ModeProgress(busy)
        Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val change = stringResource(R.string.mode_change_photo)
                Box(Modifier.weight(1f).height(210.dp).clip(RoundedCornerShape(16.dp)).background(EloraPreviewSurface)
                    .border(1.dp, Color.White.copy(alpha = .15f), RoundedCornerShape(16.dp))
                    .clickable(enabled = !busy, role = Role.Button, onClick = onChangePhoto).semantics { contentDescription = change }) {
                    AsyncImage(localPhoto, null, Modifier.fillMaxSize(), contentScale = uploadPreviewContentScale())
                    PreviewCaption(stringResource(R.string.mode_your_photo), Modifier.align(Alignment.BottomCenter))
                }
                val preview = stringResource(R.string.mode_template_preview)
                Box(Modifier.weight(1f).height(210.dp).clip(RoundedCornerShape(16.dp)).background(EloraPreviewSurface)
                    .border(1.dp, EloraModePink.copy(alpha = .3f), RoundedCornerShape(16.dp)).semantics { contentDescription = preview }) {
                    TemplatePreviewImage(templateMedia, !busy && playPreview && templateMedia is TemplatePreviewMedia.RemoteVideo,
                        contentDescription = null, contentScale = uploadPreviewContentScale(),
                        videoResizeMode = uploadPreviewVideoResizeMode(), modifier = Modifier.fillMaxSize())
                    PreviewCaption(preview, Modifier.align(Alignment.BottomCenter))
                }
            }
            Text(stringResource(R.string.mode_engine), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 18.dp, bottom = 12.dp))
            if (modes.isEmpty()) Text(stringResource(R.string.mode_unavailable), color = EloraWelcomeMuted, fontSize = 13.sp)
            modes.forEach { mode ->
                ModeCard(mode, selectedMode == mode.quality, busy) { onModeSelect(mode.quality) }
                Spacer(Modifier.height(10.dp))
            }
            error?.let { Text(it, color = EloraWelcomeError, fontSize = 12.sp, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) }
        }
        val generate = stringResource(R.string.mode_generate)
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp)
            .heightIn(min = 56.dp).clip(CircleShape)
            .background(Brush.horizontalGradient(listOf(EloraCatalogPurple, EloraWelcomePurple, EloraModePink)))
            .clickable(enabled = !busy && selected != null, role = Role.Button, onClick = onGenerate)
            .semantics { if (busy) contentDescription = generate }
            .padding(horizontal = 24.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                if (busy) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                else {
                    Text(generate, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp)); Image(painterResource(R.drawable.elora_mode_sparkle), null, Modifier.size(16.dp))
                }
            }
            Row(Modifier.clip(CircleShape).background(Color.Black.copy(alpha = .35f))
                .border(1.dp, Color.White.copy(alpha = .25f), CircleShape).padding(horizontal = 13.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Image(painterResource(R.drawable.elora_mode_token), null, Modifier.size(14.dp))
                val cost = selected?.let { stringResource(R.string.mode_cost_description, it.price) } ?: stringResource(R.string.mode_unavailable)
                Text(selected?.price?.toString() ?: stringResource(R.string.mode_unknown_cost), color = Color.White,
                    fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { contentDescription = cost })
            }
        }
    }
}

@Composable
private fun ModeCard(mode: GenerationModeOption, selected: Boolean, busy: Boolean, onSelect: () -> Unit) {
    val fast = mode.quality == "fast"
    Row(Modifier.fillMaxWidth().heightIn(min = 72.dp).clip(RoundedCornerShape(16.dp))
        .background(if (selected) Brush.linearGradient(listOf(EloraWelcomePurple.copy(alpha = .16f), EloraModePink.copy(alpha = .08f)))
            else Brush.linearGradient(listOf(EloraModeSurface, EloraModeSurface)))
        .border(1.dp, if (selected) EloraWelcomeLilac.copy(alpha = .7f) else Color.White.copy(alpha = .07f), RoundedCornerShape(16.dp))
        .selectable(selected, enabled = !busy, role = Role.RadioButton, onClick = onSelect).padding(15.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(EloraWelcomePurple.copy(alpha = .18f))
            .border(1.dp, if (selected) EloraModePink.copy(alpha = .5f) else EloraWelcomePurple.copy(alpha = .3f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center) {
            Image(painterResource(if (fast) R.drawable.elora_mode_fast else R.drawable.elora_mode_quality), null, Modifier.size(20.dp))
        }
        Text(stringResource(if (fast) R.string.mode_standard_fast else R.string.mode_ultra_quality), color = Color.White,
            fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Box(Modifier.size(20.dp).clip(CircleShape).background(if (selected) EloraModePink else Color.Transparent)
            .border(2.dp, Color.White.copy(alpha = if (selected) .9f else .2f), CircleShape), contentAlignment = Alignment.Center) {
            if (selected) Image(painterResource(R.drawable.elora_mode_selected), null, Modifier.size(12.dp))
        }
    }
}

@Composable
private fun PreviewCaption(label: String, modifier: Modifier) {
    Text(label, color = Color.White, fontSize = 10.sp, textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .8f)))).padding(8.dp))
}

@Composable
private fun ModeProgress(busy: Boolean) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp).drawBehind {
        val y = 14.dp.toPx()
        drawLine(Color.White.copy(alpha = .1f), Offset(size.width / 6f, y), Offset(size.width * 5f / 6f, y), 2.dp.toPx())
        drawLine(Brush.horizontalGradient(listOf(EloraWelcomePurple, EloraModePink)), Offset(size.width / 6f, y),
            Offset(size.width * (if (busy) 5f / 6f else .5f), y), 2.5.dp.toPx())
    }, horizontalArrangement = Arrangement.SpaceBetween) {
        listOf(R.string.mode_upload, R.string.mode_mode, R.string.mode_generate).forEachIndexed { index, label ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                val active = if (busy) index == 2 else index == 1
                Box(Modifier.size(28.dp).clip(CircleShape).background(if (index == 0) EloraWelcomePurple else EloraPreviewSurface)
                    .border(2.dp, when { index == 0 -> EloraWelcomeLilac; active -> EloraModePink; else -> Color.White.copy(alpha = .2f) }, CircleShape),
                    contentAlignment = Alignment.Center) {
                    if (index == 0) Image(painterResource(R.drawable.elora_mode_check), null, Modifier.size(14.dp))
                    else Box(Modifier.size(if (active) 10.dp else 8.dp).background(if (active) EloraModePink else Color.White.copy(alpha = .2f), CircleShape))
                }
                Text(stringResource(label), color = if (active) EloraModePink else EloraWelcomeSoftLilac,
                    fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}
