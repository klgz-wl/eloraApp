package yumo.achat.app.ui.imagevideo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import yumo.achat.app.ui.report.MediaReportIconButton
import yumo.achat.app.R
import yumo.achat.app.ui.theme.*

@Composable
internal fun UploadTemplatePreviewScreen(
    title: String, media: TemplatePreviewMedia, durationSeconds: Int,
    enabled: Boolean, onChoosePhoto: () -> Unit, onBack: () -> Unit,
    onReport: (() -> Unit)? = null, modifier: Modifier = Modifier, playPreview: Boolean = true,
) {
    var informationOpen by rememberSaveable(title) { mutableStateOf(false) }
    val isVideo = media is TemplatePreviewMedia.RemoteVideo
    Column(modifier.fillMaxSize().background(EloraPreviewBackground).drawBehind {
        drawRect(Brush.radialGradient(listOf(EloraWelcomeViolet.copy(alpha = .18f), Color.Transparent),
            center = Offset(size.width * .5f, 0f), radius = size.width * 1.3f))
        drawRect(Brush.radialGradient(listOf(EloraWelcomeViolet.copy(alpha = .12f), Color.Transparent),
            center = Offset(size.width * .5f, size.height), radius = size.width))
    }.safeDrawingPadding()) {
        val back = stringResource(R.string.back_to_templates_description)
        Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).heightIn(min = 48.dp)) {
            Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                letterSpacing = .45.sp, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = 52.dp))
            Box(Modifier.size(48.dp).clip(CircleShape).background(EloraPreviewSurface.copy(alpha = .65f))
                .border(1.dp, Color.White.copy(alpha = .12f), CircleShape)
                .clickable(role = Role.Button, onClick = onBack).semantics { contentDescription = back },
                contentAlignment = Alignment.Center) {
                Image(painterResource(R.drawable.elora_account_back), null, Modifier.size(20.dp))
            }
        }
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val stageHeight = (maxHeight - 16.dp).coerceIn(240.dp, 580.dp)
            // Preserve a useful media stage on short screens, with scroll available before the fixed CTA.
            Column(Modifier.align(Alignment.Center).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxWidth().height(stageHeight)
                    .clip(RoundedCornerShape(34.dp)).background(EloraPreviewSurface)
                    .border(1.dp, Color.White.copy(alpha = .1f), RoundedCornerShape(34.dp)).padding(11.dp)) {
                    val preview = stringResource(R.string.selected_template_description)
                    Box(Modifier.fillMaxSize().clip(RoundedCornerShape(26.dp)).background(Color.Black.copy(alpha = .4f))
                        .semantics { contentDescription = preview }) {
                        TemplatePreviewImage(media = media, isPlaying = isVideo && playPreview,
                            contentDescription = null, contentScale = uploadPreviewContentScale(),
                            videoResizeMode = uploadPreviewVideoResizeMode(), modifier = Modifier.fillMaxSize())
                        if (onReport != null) MediaReportIconButton(onReport, Modifier.align(Alignment.BottomEnd).padding(12.dp))
                        else {
                            val information = stringResource(R.string.preview_information)
                            Box(Modifier.align(Alignment.BottomEnd).padding(12.dp).size(48.dp).clip(CircleShape)
                                .background(Color.Black.copy(alpha = .55f)).border(1.dp, Color.White.copy(alpha = .15f), CircleShape)
                                .clickable(role = Role.Button) { informationOpen = true }.semantics { contentDescription = information },
                                contentAlignment = Alignment.Center) {
                                Image(painterResource(R.drawable.elora_preview_info), null, Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp)
            .heightIn(min = 56.dp).clip(CircleShape)
            .background(Brush.linearGradient(listOf(EloraAccountButtonStart, EloraWelcomePurple, EloraPreviewButtonEnd)))
            .border(1.dp, EloraWelcomeLilac.copy(alpha = .3f), CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onChoosePhoto).padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Image(painterResource(R.drawable.elora_preview_upload), null, Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.preview_upload_photo), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
    if (informationOpen && onReport == null) {
        AlertDialog(onDismissRequest = { informationOpen = false }, title = { Text(title) },
            text = { Text(if (isVideo) stringResource(R.string.preview_video_information, durationSeconds)
                else stringResource(R.string.preview_image_information)) },
            confirmButton = { TextButton(onClick = { informationOpen = false }) { Text(stringResource(R.string.preview_close)) } })
    }
}
