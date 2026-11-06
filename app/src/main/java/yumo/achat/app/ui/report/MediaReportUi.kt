package yumo.achat.app.ui.report

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import yumo.achat.app.R
import yumo.achat.app.ui.theme.EloraReportSurface
import yumo.achat.app.ui.theme.EloraReportButtonStart
import yumo.achat.app.ui.theme.EloraReportButtonMiddle
import yumo.achat.app.ui.theme.EloraReportButtonEnd
import yumo.achat.app.ui.theme.EloraWelcomeError

/** Shared information glyph that opens the report confirmation. */
@Composable
fun MediaReportIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(R.string.report_content_description)
    Box(
        modifier = modifier.size(48.dp).clip(CircleShape)
            .background(Color.Black.copy(alpha = .55f))
            .border(1.dp, Color.White.copy(alpha = .15f), CircleShape)
            .semantics { contentDescription = label }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(painterResource(R.drawable.elora_preview_info), null, Modifier.size(16.dp))
    }
}

/** Owns one report attempt; remove from composition to cancel its pending request. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaReportSheet(
    mediaId: String,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    repository: MediaReportRepository = remember { LocalMediaReportRepository() },
) {
    val controller = remember(mediaId, repository) { ReportSubmissionController(repository) }
    val submissionState by controller.state.collectAsState()
    val submittingLabel = stringResource(R.string.report_submitting)
    val scope = rememberCoroutineScope()
    var submissionJob by remember(mediaId, repository) { mutableStateOf<Job?>(null) }
    DisposableEffect(mediaId, repository) {
        onDispose { submissionJob?.cancel() }
    }
    val close = {
        submissionJob?.cancel()
        onDismiss()
    }
    ModalBottomSheet(
        onDismissRequest = close,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = EloraReportSurface,
        contentColor = Color.White,
        scrimColor = Color.Black.copy(alpha = 0.45f),
        shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
        dragHandle = {
            Box(
                Modifier.padding(top = 16.dp, bottom = 8.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
            )
        },
        modifier = modifier,
    ) {
        Column(
            Modifier.fillMaxWidth().heightIn(max = 560.dp).verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp).padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.fillMaxWidth().height(48.dp)) {
                val closeDescription = stringResource(R.string.report_close)
                Box(Modifier.align(Alignment.CenterEnd).size(48.dp).clickable(role = Role.Button, onClick = close)
                    .semantics { contentDescription = closeDescription }, contentAlignment = Alignment.Center) {
                    Box(Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = .05f))
                        .border(1.dp, Color.White.copy(alpha = .1f), CircleShape), contentAlignment = Alignment.Center) {
                        Image(painterResource(R.drawable.elora_report_close), null, Modifier.size(16.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.report_explanation),
                color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                lineHeight = 28.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 8.dp),
            )
            if (submissionState == ReportSubmissionState.Failure) {
                Spacer(Modifier.height(9.dp))
                Text(stringResource(R.string.report_failed), color = EloraWelcomeError, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(28.dp))
            Row(
                Modifier.fillMaxWidth().heightIn(min = 56.dp).clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(EloraReportButtonStart, EloraReportButtonMiddle, EloraReportButtonEnd)))
                    .border(1.dp, Color.White.copy(alpha = .25f), CircleShape)
                    .clickable(enabled = submissionState != ReportSubmissionState.Submitting, role = Role.Button) {
                        submissionJob = scope.launch {
                            if (controller.submit(MediaReport(mediaId))) onSuccess()
                        }
                    }
                    .semantics { if (submissionState == ReportSubmissionState.Submitting) contentDescription = submittingLabel }
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center,
            ) {
                if (submissionState == ReportSubmissionState.Submitting) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Image(painterResource(R.drawable.elora_report_flag), null, Modifier.size(16.dp))
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.report_submit), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
