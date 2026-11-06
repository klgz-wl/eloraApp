package yumo.achat.app.ui.imagevideo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
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
import yumo.achat.core.backend.VisualCategory
import yumo.achat.core.backend.VisualTemplate

internal fun templateCatalogCategoryId(categories: List<VisualCategory>, tab: Int): String? =
    categories.getOrNull(tab - 1)?.id

@Composable
internal fun TemplateCatalogScreen(
    templates: List<VisualTemplate>, categories: List<VisualCategory>, selectedTab: Int, balance: Int,
    loading: Boolean, error: String?, hasMore: Boolean, paginationLoading: Boolean, paginationError: String?,
    onTabSelect: (Int) -> Unit, onTemplateSelect: (VisualTemplate, Int) -> Unit,
    onExpose: (VisualTemplate, Int) -> Unit, onLoadMore: () -> Unit, onRetry: () -> Unit,
    onBalance: () -> Unit, onNavigation: (Int) -> Unit,
    onRecord: () -> Unit = onBalance,
    statusContent: @Composable () -> Unit = {},
    section: TemplateSection = TemplateSection.Video,
) {
    val description = stringResource(if (section == TemplateSection.Image) R.string.image_catalog_title else R.string.video_catalog_title)
    Column(Modifier.fillMaxSize().background(EloraCatalogBackground).drawBehind {
        drawRect(Brush.radialGradient(listOf(EloraCatalogPurple.copy(alpha = .35f),
            EloraCatalogPink.copy(alpha = .10f), Color.Transparent),
            center = Offset(size.width * .55f, 0f), radius = size.width * .9f))
        drawRect(Brush.radialGradient(listOf(EloraWelcomeCyan.copy(alpha = .13f), Color.Transparent),
            center = Offset(size.width, size.height * .8f), radius = size.width * .9f))
    }.statusBarsPadding().navigationBarsPadding().semantics { contentDescription = description }) {
        CatalogHeader(balance, onBalance, onRecord)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CatalogFilter(stringResource(R.string.catalog_all), null, selectedTab == 0) { onTabSelect(0) }
            categories.forEachIndexed { index, category ->
                CatalogFilter(category.name, null, selectedTab == index + 1) { onTabSelect(index + 1) }
            }
        }
        statusContent()
        Box(Modifier.fillMaxWidth().weight(1f)) {
            if (loading) {
                CatalogLoadingPlaceholder(section)
            } else if (templates.isEmpty()) {
                if (error != null) LiveTemplatePlaceholderCard(false, error, onRetry, Modifier.fillMaxSize().padding(16.dp))
            } else key(section, selectedTab) {
                CatalogGrid(templates, selectedTab, hasMore, paginationLoading, paginationError, error,
                    onTemplateSelect, onExpose, onLoadMore, onRetry, section)
            }
        }
        CatalogNavigation(if (section == TemplateSection.Image) 1 else 0, onNavigation)
    }
}

@Composable
private fun CatalogLoadingPlaceholder(section: TemplateSection) {
    val loading = stringResource(R.string.catalog_loading_description)
    LazyVerticalStaggeredGrid(columns = StaggeredGridCells.Fixed(2), userScrollEnabled = false,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp), verticalItemSpacing = 12.dp,
        modifier = Modifier.fillMaxSize().clearAndSetSemantics { contentDescription = loading }) {
        items(6) { index ->
            val height = if (section == TemplateSection.Image) { if (index == 1) 211.dp else 240.dp }
                else if (index % 3 == 0) 250.dp else 220.dp
            Box(Modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(16.dp))
                .background(EloraCatalogSurface.copy(alpha = .65f))
                .border(1.dp, Color.White.copy(alpha = .04f), RoundedCornerShape(16.dp)))
        }
    }
}

@Composable
private fun CatalogFilter(label: String, icon: Int?, selected: Boolean, onClick: () -> Unit) {
    Row(Modifier.clip(CircleShape)
        .background(if (selected) Brush.horizontalGradient(listOf(EloraCatalogPurple, EloraCatalogPink))
            else Brush.horizontalGradient(listOf(Color.White.copy(alpha = .06f), Color.White.copy(alpha = .06f))))
        .border(1.dp, Color.White.copy(alpha = if (selected) .2f else .11f), CircleShape)
        .selectable(selected = selected, role = Role.Tab, onClick = onClick)
        .padding(horizontal = 15.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (icon != null) Image(painterResource(icon), null, Modifier.size(14.dp))
        else Box(Modifier.size(8.dp).background(EloraWelcomeCyan, CircleShape))
        Text(label, color = Color.White.copy(alpha = if (selected) 1f else .75f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CatalogGrid(
    templates: List<VisualTemplate>, selectedTab: Int, hasMore: Boolean, loadingMore: Boolean,
    paginationError: String?, refreshError: String?, onSelect: (VisualTemplate, Int) -> Unit,
    onExpose: (VisualTemplate, Int) -> Unit, onLoadMore: () -> Unit, onRetry: () -> Unit, section: TemplateSection,
) {
    val grid = rememberLazyStaggeredGridState()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val exposed = remember(selectedTab) { mutableSetOf<String>() }
    DisposableEffect(lifecycle, exposed) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_PAUSE) exposed.clear() }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    val expose by rememberUpdatedState(onExpose)
    val loadMore by rememberUpdatedState(onLoadMore)
    LaunchedEffect(grid, templates, hasMore, loadingMore, paginationError, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            snapshotFlow { grid.layoutInfo.visibleItemsInfo.map { it.index } }.distinctUntilChanged().collect { indices ->
                indices.forEach { index -> templates.getOrNull(index)?.let { if (exposed.add(it.id)) expose(it, index + 1) } }
                if (hasMore && !loadingMore && paginationError == null && indices.any { it >= templates.lastIndex - 2 }) loadMore()
            }
        }
    }
    LazyVerticalStaggeredGrid(columns = StaggeredGridCells.Fixed(2), state = grid,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp), verticalItemSpacing = if (section == TemplateSection.Image) 10.dp else 14.dp,
        modifier = Modifier.fillMaxSize().testTag(if (section == TemplateSection.Image) "image-catalog-grid" else "video-catalog-grid")) {
        items(templates.size, key = { "template:${templates[it].id}" }) { index ->
            CatalogCard(templates[index], index, section, onClick = { onSelect(templates[index], index + 1) })
        }
        if (refreshError != null) item(key = "refresh-error", span = StaggeredGridItemSpan.FullLine) {
            CatalogRetry(refreshError, onRetry)
        }
        if (loadingMore || paginationError != null || hasMore) item(key = "pagination", span = StaggeredGridItemSpan.FullLine) {
            when {
                loadingMore -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = EloraWelcomeLilac)
                    Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.template_loading_more), color = EloraWelcomeMuted, fontSize = 12.sp)
                }
                paginationError != null -> CatalogRetry(paginationError, onLoadMore)
                else -> Text(stringResource(R.string.video_catalog_more), color = EloraWelcomeLilac,
                    modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onLoadMore).padding(12.dp))
            }
        }
    }
}

@Composable
private fun CatalogRetry(message: String, onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(message, color = EloraWelcomeError, fontSize = 12.sp)
        Text(stringResource(R.string.video_catalog_retry), color = EloraWelcomeLilac,
            modifier = Modifier.clickable(role = Role.Button, onClick = onRetry).padding(12.dp))
    }
}

@Composable
private fun CatalogCard(template: VisualTemplate, index: Int, section: TemplateSection, onClick: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    val usable = template.toPreviewMedia().isUsableGenerationMedia()
    val poster = template.previewUrl.ifBlank { template.fileUrl.takeIf { template.mimeType.startsWith("image/") }.orEmpty() }
    // Stable varied heights reproduce the design's masonry rhythm without altering catalog order.
    val height = if (section == TemplateSection.Image) (if (index == 1) 211 else 240).dp
        else listOf(240, 215, 215, 290, 175, 125)[index % 6].dp
    Box(Modifier.fillMaxWidth().height(height).testTag(if (section == TemplateSection.Image) "image-template-card" else "video-template-card").clip(shape).background(EloraCatalogSurface)
        .border(1.dp, Color.White.copy(alpha = .1f), shape)
        .clickable(enabled = usable, role = Role.Button, onClick = onClick)
        .semantics { contentDescription = template.name }) {
        Text(template.name, color = EloraWelcomeMuted, fontSize = 12.sp,
            modifier = Modifier.align(Alignment.Center).padding(12.dp))
        if (poster.isNotBlank()) AsyncImage(model = poster, contentDescription = null,
            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top) {
            if (section == TemplateSection.Video && template.durationSeconds > 0) Row(Modifier.clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = .65f)).border(1.dp, Color.White.copy(alpha = .16f), RoundedCornerShape(6.dp))
                .padding(horizontal = 7.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.video_catalog_duration, template.durationSeconds), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(4.dp)); Image(painterResource(R.drawable.elora_catalog_play), null, Modifier.size(8.dp))
            } else Spacer(Modifier.size(1.dp))
            if (template.hotScore > 0) Text(stringResource(R.string.video_catalog_hot_badge),
                color = EloraCatalogBadgeText, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clip(CircleShape).background(EloraCatalogBadge).padding(horizontal = 8.dp, vertical = 3.dp))
        }
    }
}

@Composable
internal fun CatalogNavigation(selectedIndex: Int, onSelect: (Int) -> Unit) {
    val labels = listOf(R.string.nav_home, R.string.nav_video, R.string.nav_image, R.string.nav_me)
    val icons = listOf(if (selectedIndex == 4) R.drawable.elora_catalog_home_active else R.drawable.elora_catalog_planet, if (selectedIndex == 0) R.drawable.elora_catalog_video else R.drawable.elora_catalog_video_inactive,
        if (selectedIndex == 1) R.drawable.elora_catalog_image_active else R.drawable.elora_catalog_image, if (selectedIndex == 3) R.drawable.elora_me_profile_active else R.drawable.elora_catalog_profile)
    Row(Modifier.fillMaxWidth().background(EloraCatalogNavigation).padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        listOf(4, 0, 1, 3).forEachIndexed { position, destination ->
            val selected = destination == selectedIndex
            val label = stringResource(labels[position])
            Row(Modifier.clip(CircleShape).then(if (selected) Modifier.background(Brush.horizontalGradient(
                listOf(EloraCatalogGold.copy(alpha = .2f), EloraWelcomePurple.copy(alpha = .3f))))
                .border(1.dp, EloraWelcomeLilac.copy(alpha = .4f), CircleShape) else Modifier)
                .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(destination) })
                .semantics { contentDescription = label }.heightIn(min = 48.dp)
                .padding(horizontal = if (selected) 17.dp else 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(icons[position]), null, Modifier.size(24.dp))
                if (selected) { Spacer(Modifier.width(8.dp)); Text(stringResource(when (selectedIndex) { 4 -> R.string.home_label; 1 -> R.string.image_catalog_image; 3 -> R.string.me_title; else -> R.string.video_catalog_video }), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
internal fun CatalogHeader(balance: Int, onBalance: () -> Unit, onRecord: () -> Unit = onBalance) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            val vipLabel = stringResource(R.string.video_catalog_membership)
            Row(Modifier.clip(CircleShape).border(1.dp, Brush.horizontalGradient(listOf(EloraCatalogGold, EloraWelcomeViolet)), CircleShape)
                .background(EloraCatalogSurface).clickable(role = Role.Button, onClick = onBalance)
                .semantics { contentDescription = vipLabel }.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Image(painterResource(R.drawable.elora_catalog_star), null, Modifier.size(14.dp))
                Text(stringResource(R.string.login_brand), color = EloraCatalogGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            val balanceLabel = stringResource(R.string.video_catalog_balance, balance)
            Row(Modifier.clip(CircleShape).background(EloraCatalogPurple.copy(alpha = .25f))
                .border(1.dp, EloraWelcomePurple.copy(alpha = .3f), CircleShape)
                .clickable(role = Role.Button, onClick = onRecord).semantics { contentDescription = balanceLabel }
                .padding(horizontal = 13.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Image(painterResource(R.drawable.elora_catalog_token), null, Modifier.size(16.dp))
                Text(balance.toString(), color = EloraWelcomeSoftLilac, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
}
