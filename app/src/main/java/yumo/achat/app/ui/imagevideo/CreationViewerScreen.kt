package yumo.achat.app.ui.imagevideo

import androidx.annotation.OptIn

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import coil.compose.AsyncImage
import yumo.achat.app.R
import yumo.achat.app.ui.error.diagnosticString
import yumo.achat.app.ui.theme.EloraWelcomeError

@OptIn(UnstableApi::class)
@Composable
internal fun CreationViewerScreen(task: TrackedGenerationTask, onBack: () -> Unit, initialVideoPlaybackFailed: Boolean = false) {
    BackHandler(onBack = onBack)
    var playing by rememberSaveable(task.taskId, task.resultUrl) { mutableStateOf(false) }
    var failed by remember(task.taskId, task.resultUrl) { mutableStateOf(initialVideoPlaybackFailed) }
    var retry by remember(task.taskId, task.resultUrl) { mutableIntStateOf(0) }
    var firstFrame by remember(task.taskId, task.resultUrl, retry) { mutableStateOf(false) }
    val video = task.canOpenResult && !task.canPreviewAsImage && (task.mimeType.startsWith("video/") || task.modality == "video")
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        when {
            task.canPreviewAsImage -> MyTaskProgressiveImage(
                imageUrl = task.resultUrl.orEmpty(), thumbnailUrl = task.thumbnailUrl,
                contentDescription = stringResource(R.string.creation_viewer_image), contentScale = ContentScale.Fit,
                widthPx = 1080, heightPx = 1920, modifier = Modifier.fillMaxSize())
            video && !failed -> {
                key(task.taskId, task.resultUrl, retry) {
                    TemplatePreviewVideo(task.resultUrl.orEmpty(), playing, resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT,
                        onFirstFrame = { firstFrame = true }, onPlaybackError = { failed = true; playing = false }, modifier = Modifier.fillMaxSize())
                }
                if (!firstFrame && !task.thumbnailUrl.isNullOrBlank()) AsyncImage(task.thumbnailUrl, null,
                    Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                val action = stringResource(if (playing) R.string.creation_viewer_pause else R.string.creation_viewer_play)
                Box(Modifier.fillMaxSize().semantics { contentDescription = action }
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, role = Role.Button) { playing = !playing })
                if (!playing) Box(Modifier.align(Alignment.Center).size(86.dp), contentAlignment = Alignment.Center) {
                    Image(painterResource(R.drawable.elora_viewer_play_circle), null, Modifier.fillMaxSize())
                    Image(painterResource(R.drawable.elora_viewer_play), null, Modifier.size(33.dp, 40.dp).offset(x = 3.dp))
                }
            }
            else -> Column(Modifier.align(Alignment.Center).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(diagnosticString(stringResource(if (video) R.string.task_result_file_unavailable else R.string.task_result_preview_unavailable),
                    if (video) "E-302" else "E-404"), color = EloraWelcomeError, fontSize = 12.sp)
                if (video) TextButton(onClick = { retry++; failed = false; playing = false }) { Text(stringResource(R.string.template_feed_retry), color = Color.White) }
            }
        }
        val back = stringResource(R.string.me_settings_back)
        Box(Modifier.align(Alignment.TopStart).safeDrawingPadding().padding(start = 24.dp, top = 8.dp)
            .size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = .05f))
            .border(1.dp, Color.White.copy(alpha = .1f), CircleShape).clickable(role = Role.Button, onClick = onBack)
            .semantics { contentDescription = back }, contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.elora_viewer_back), null, Modifier.size(20.dp))
        }
    }
}
