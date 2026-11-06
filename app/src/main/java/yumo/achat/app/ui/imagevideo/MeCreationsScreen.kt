package yumo.achat.app.ui.imagevideo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import yumo.achat.app.R
import yumo.achat.app.ui.theme.*

@Composable
internal fun MeCreationsScreen(
    profileName: String, avatar: Any?, balance: Int, avatarSaving: Boolean, filter: MeCreationFilter, allCount: Int?,
    items: List<TrackedGenerationTask>, loading: Boolean, error: String?, hasMore: Boolean, loadingMore: Boolean, pageError: String?,
    onFilter: (MeCreationFilter) -> Unit, onRetry: () -> Unit, onLoadMore: () -> Unit,
    onEditName: () -> Unit, onEditAvatar: () -> Unit, onSettings: () -> Unit,
    onOpen: (TrackedGenerationTask) -> Unit, onNavigation: (Int) -> Unit, onPurchase: () -> Unit = {},
) {
    val visible = visibleMeCreations(items, filter)
    Column(Modifier.fillMaxSize().background(EloraMeBackground).drawBehind {
        drawRect(Brush.radialGradient(listOf(EloraWelcomePurple.copy(alpha = .22f), Color.Transparent),
            center = Offset(size.width, 0f), radius = size.width * 1.1f))
    }.statusBarsPadding().navigationBarsPadding()) {
        LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth().weight(1f)) {
            item(span = { GridItemSpan(2) }, key = "header") {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.me_title), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    val settings = stringResource(R.string.me_settings)
                    Box(Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = .06f))
                        .border(1.dp, Color.White.copy(alpha = .12f), CircleShape)
                        .clickable(role = Role.Button, onClick = onSettings).semantics { contentDescription = settings }, contentAlignment = Alignment.Center) {
                        Image(painterResource(R.drawable.elora_me_settings), null, Modifier.size(20.dp))
                    }
                }
            }
            item(span = { GridItemSpan(2) }, key = "profile") {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(EloraMeSurface)
                    .border(1.dp, Color.White.copy(alpha = .1f), RoundedCornerShape(24.dp)).padding(horizontal = 14.dp, vertical = 21.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val editAvatar = stringResource(R.string.profile_avatar_edit_description)
                    Box(Modifier.size(84.dp).clip(CircleShape).background(Brush.linearGradient(listOf(EloraWelcomeMagenta, EloraWelcomeViolet, EloraWelcomeCyan)))
                        .clickable(enabled = !avatarSaving, role = Role.Button, onClick = onEditAvatar)
                        .semantics { contentDescription = editAvatar }.padding(3.dp)) {
                        AsyncImage(avatar ?: R.drawable.elora_logo, null, Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                        if (avatarSaving) CircularProgressIndicator(Modifier.align(Alignment.Center).size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val editName = stringResource(R.string.profile_edit_description)
                        Text(profileName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 2,
                            overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth().clickable(enabled = !avatarSaving, onClick = onEditName)
                                .semantics { contentDescription = editName })
                        Row(Modifier.clip(CircleShape).background(EloraPickerIconSurface)
                            .border(1.dp, EloraWelcomePurple.copy(alpha = .3f), CircleShape).padding(horizontal = 11.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Image(painterResource(R.drawable.elora_catalog_star), null, Modifier.size(14.dp))
                            Text(stringResource(R.string.me_credits_left, balance), color = EloraWelcomeSoftLilac, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(Modifier.fillMaxWidth().heightIn(min = 40.dp).clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(EloraWelcomeViolet, EloraWelcomeMagenta)))
                            .clickable(role = Role.Button, onClick = onPurchase).padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Image(painterResource(R.drawable.elora_me_purchase), null, Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.me_purchase_credits), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
            item(span = { GridItemSpan(2) }, key = "filters") {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MeCreationFilter.entries.forEach { item ->
                        val selected = item == filter
                        Row(Modifier.clip(CircleShape).background(if (selected) Color.White else Color.White.copy(alpha = .05f))
                            .border(1.dp, Color.White.copy(alpha = .1f), CircleShape)
                            .selectable(selected, role = Role.Tab, onClick = { onFilter(item) }).padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(stringResource(when (item) { MeCreationFilter.Creations -> R.string.me_creations; MeCreationFilter.Videos -> R.string.me_videos; MeCreationFilter.Images -> R.string.me_images }),
                                color = if (selected) EloraMeText else EloraAccountMuted, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                            if (item == MeCreationFilter.Creations && allCount != null) Text(allCount.toString(), color = if (selected) EloraMeText else EloraAccountMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
            item(span = { GridItemSpan(2) }, key = "privacy-notice") { CreationsPrivacyNotice() }
            when {
                loading -> item(span = { GridItemSpan(2) }) { MeStatus(stringResource(R.string.task_history_loading)) }
                error != null -> item(span = { GridItemSpan(2) }) { MeRetry(error, onRetry) }
                visible.isEmpty() -> item(span = { GridItemSpan(2) }) {
                    Column(Modifier.fillMaxWidth().heightIn(min = 240.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text(stringResource(R.string.me_no_creations), color = Color.White, fontSize = 20.sp)
                        Spacer(Modifier.height(24.dp))
                        Text(stringResource(R.string.me_create_now), color = EloraMeText, fontSize = 18.sp,
                            modifier = Modifier.clip(CircleShape).background(Color.White).clickable(role = Role.Button) { onNavigation(4) }
                                .padding(horizontal = 28.dp, vertical = 16.dp))
                    }
                }
                else -> items(visible, key = { it.taskId }) { task -> CreationCard(task) { onOpen(task) } }
            }
            if (!loading && error == null) {
                when {
                    loadingMore -> item(span = { GridItemSpan(2) }) { MeStatus(stringResource(R.string.task_history_loading)) }
                    pageError != null -> item(span = { GridItemSpan(2) }) { MeRetry(pageError, onLoadMore) }
                    hasMore -> item(span = { GridItemSpan(2) }) {
                        Text(stringResource(R.string.video_catalog_more), color = EloraWelcomeLilac,
                            modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onLoadMore).padding(12.dp))
                    }
                }
            }
        }
        CatalogNavigation(3, onNavigation)
    }
}

@Composable
private fun CreationsPrivacyNotice() {
    val label = stringResource(R.string.me_retention_label)
    val duration = stringResource(R.string.me_retention_duration)
    val message = stringResource(R.string.me_retention_notice, label, duration)
    val text = buildAnnotatedString {
        append(message)
        val labelStart = message.indexOf(label)
        if (labelStart >= 0) addStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold), labelStart, labelStart + label.length)
        val durationStart = message.indexOf(duration)
        if (durationStart >= 0) addStyle(SpanStyle(color = EloraMeNoticeHighlight, fontWeight = FontWeight.Bold), durationStart, durationStart + duration.length)
    }
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(EloraMeSurface)
        .background(Brush.horizontalGradient(listOf(EloraMeNoticeStart, EloraMeNoticeMiddle, EloraMeNoticeEnd)))
        .border(1.dp, EloraWelcomeViolet.copy(alpha = .2f), RoundedCornerShape(16.dp)).padding(15.dp),
        verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.padding(top = 2.dp).size(28.dp).clip(RoundedCornerShape(12.dp))
            .background(EloraWelcomeViolet.copy(alpha = .2f))
            .border(1.dp, EloraWelcomeLilac.copy(alpha = .3f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.elora_me_privacy_clock), null, Modifier.size(16.dp))
        }
        Text(text, color = EloraAccountLabel, fontSize = 12.sp, lineHeight = 20.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MeStatus(label: String) {
    Row(Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(Modifier.size(20.dp), color = EloraWelcomeLilac, strokeWidth = 2.dp)
        Spacer(Modifier.width(12.dp)); Text(label, color = EloraWelcomeMuted, fontSize = 12.sp)
    }
}

@Composable
private fun MeRetry(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(message, color = EloraWelcomeError, fontSize = 12.sp)
        Text(stringResource(R.string.video_catalog_retry), color = EloraWelcomeLilac,
            modifier = Modifier.clickable(role = Role.Button, onClick = onRetry).padding(12.dp))
    }
}

@Composable
private fun CreationCard(task: TrackedGenerationTask, onOpen: () -> Unit) {
    val video = task.modality == "video"
    val label = stringResource(R.string.me_open_creation, task.title)
    Box(Modifier.fillMaxWidth().height(228.dp).clip(RoundedCornerShape(16.dp)).background(EloraCatalogSurface)
        .border(1.dp, Color.White.copy(alpha = .1f), RoundedCornerShape(16.dp))
        .clickable(role = Role.Button, onClick = onOpen).semantics { contentDescription = label }) {
        Text(task.title, color = EloraWelcomeMuted, fontSize = 12.sp, modifier = Modifier.align(Alignment.Center).padding(8.dp))
        val thumbnail = if (video) task.thumbnailUrl else task.thumbnailUrl?.takeIf(String::isNotBlank) ?: task.resultUrl
        if (!thumbnail.isNullOrBlank()) AsyncImage(thumbnail, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        if (video) Text(stringResource(R.string.me_motion), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopStart).padding(10.dp).clip(RoundedCornerShape(6.dp)).background(EloraWelcomePurple).padding(horizontal = 9.dp, vertical = 3.dp))
        Box(Modifier.align(if (video) Alignment.TopEnd else Alignment.Center).padding(8.dp)
            .size(if (video) 32.dp else 56.dp).clip(if (video) CircleShape else RoundedCornerShape(16.dp))
            .background(if (video) EloraWelcomeViolet else Color.White.copy(alpha = .2f))
            .border(1.dp, Color.White.copy(alpha = .3f), if (video) CircleShape else RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Image(painterResource(if (video) R.drawable.elora_me_play else R.drawable.elora_me_download), null, Modifier.size(if (video) 16.dp else 28.dp))
        }
    }
}
