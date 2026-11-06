package yumo.achat.app.ui.imagevideo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.distinctUntilChanged
import yumo.achat.app.R
import yumo.achat.app.ui.theme.*
import yumo.achat.core.backend.VisualTemplate

@Composable
internal fun HomeVideoFeedScreen(
    templates: List<VisualTemplate>, balance: Int, loading: Boolean, error: String?, hasMore: Boolean,
    paginationLoading: Boolean, paginationError: String?, onLoadMore: () -> Unit, onRetry: () -> Unit,
    onUse: (VisualTemplate) -> Unit, onExpose: (VisualTemplate, Int) -> Unit,
    onBalance: () -> Unit, onNavigation: (Int) -> Unit, onRecord: () -> Unit = onBalance, statusContent: @Composable () -> Unit = {},
) {
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(EloraCatalogSurface, EloraCatalogBackground)))
        .statusBarsPadding().navigationBarsPadding()) {
        CatalogHeader(balance, onBalance, onRecord)
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text(stringResource(R.string.home_title), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.home_swipe_hint), color = EloraWelcomeMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text(stringResource(R.string.home_studio), color = EloraWelcomeSoftLilac, fontSize = 12.sp,
                    modifier = Modifier.clip(CircleShape).background(EloraWelcomePurple.copy(alpha = .2f))
                        .padding(horizontal = 12.dp, vertical = 8.dp))
            }
        }
        if (loading || templates.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth().padding(16.dp)) {
                LiveTemplatePlaceholderCard(loading, error, onRetry, Modifier.fillMaxSize())
                Column(Modifier.align(Alignment.TopStart)) { statusContent() }
            }
        } else {
            val pager = rememberPagerState { templates.size }
            val lifecycle = LocalLifecycleOwner.current.lifecycle
            val exposed = remember { mutableSetOf<String>() }
            DisposableEffect(lifecycle) {
                val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_PAUSE) exposed.clear() }
                lifecycle.addObserver(observer)
                onDispose { lifecycle.removeObserver(observer) }
            }
            val expose by rememberUpdatedState(onExpose)
            LaunchedEffect(pager, templates, lifecycle) {
                lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    snapshotFlow { pager.settledPage }.distinctUntilChanged().collect { page ->
                        templates.getOrNull(page)?.let { if (exposed.add(it.id)) expose(it, page + 1) }
                    }
                }
            }
            LaunchedEffect(pager.currentPage, templates.size, hasMore, paginationLoading, paginationError) {
                if (pager.currentPage >= templates.lastIndex - 1 && hasMore && !paginationLoading && paginationError == null) onLoadMore()
            }
            VerticalPager(state = pager, key = { templates[it].id }, pageSpacing = 20.dp,
                modifier = Modifier.weight(1f).fillMaxWidth().testTag("home-video-pager")) { index ->
                val template = templates[index]
                Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(28.dp)).background(EloraCatalogSurface)
                    .border(1.dp, Color.White.copy(alpha = .1f), RoundedCornerShape(28.dp)).padding(13.dp)) {
                    val useDescription = stringResource(R.string.home_open_template, template.name)
                    Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(22.dp))
                        .testTag("home-video-media-${template.id}")
                        .then(if (index == pager.currentPage) Modifier.clickable(
                            enabled = template.toPreviewMedia().isUsableGenerationMedia(), role = Role.Button,
                        ) { onUse(template) }.semantics { contentDescription = useDescription } else Modifier)) {
                        if (index == pager.currentPage) TemplatePreviewImage(
                            template.toPreviewMedia(), true, template.name, modifier = Modifier.fillMaxSize())
                        else AsyncImage(model = template.previewUrl, contentDescription = template.name,
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        if (index == pager.currentPage) {
                            statusContent()
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                        Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                            Text(template.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(stringResource(R.string.home_upload_hint), color = EloraWelcomeMuted, fontSize = 12.sp)
                        }
                        val use = stringResource(R.string.home_use_template, template.name)
                        Box(Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = .12f))
                            .border(1.dp, Color.White.copy(alpha = .15f), CircleShape)
                            .clickable(enabled = template.toPreviewMedia().isUsableGenerationMedia(), role = Role.Button) { onUse(template) }
                            .semantics { contentDescription = use }, contentAlignment = Alignment.Center) {
                            Image(painterResource(R.drawable.elora_catalog_use_template), null, Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
        if (paginationLoading) Text(stringResource(R.string.template_loading_more), color = EloraWelcomeLilac, fontSize = 12.sp)
        if (paginationError != null) Text(paginationError, color = EloraWelcomeError,
            modifier = Modifier.clickable(onClick = onLoadMore).padding(8.dp), fontSize = 12.sp)
        CatalogNavigation(4, onNavigation)
    }
}
