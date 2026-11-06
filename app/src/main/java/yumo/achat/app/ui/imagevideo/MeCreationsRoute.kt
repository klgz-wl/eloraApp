package yumo.achat.app.ui.imagevideo

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import yumo.achat.app.R
import yumo.achat.app.ui.error.diagnosticMessage
import yumo.achat.core.backend.hasMore
import yumo.achat.core.backend.PagedResult
import yumo.achat.core.backend.VisualResource
import coil.request.ImageRequest
import coil.request.CachePolicy

internal enum class MeCreationFilter(val modality: String?) { Creations(null), Videos("video"), Images("image") }

internal fun visibleMeCreations(tasks: List<TrackedGenerationTask>, filter: MeCreationFilter): List<TrackedGenerationTask> =
    tasks.filter { it.canOpenResult && (filter.modality == null || it.modality == filter.modality) }.distinctBy { it.taskId }

internal data class MeVisibleCreationPage(val items: List<TrackedGenerationTask>, val page: Int, val hasMore: Boolean)

internal suspend fun readMeCreationPage(
    filter: MeCreationFilter, startPage: Int, defaultTitle: String,
    loader: suspend (String?, Int) -> PagedResult<VisualResource>,
): MeVisibleCreationPage {
    var requested = startPage
    while (true) {
        val response = loader(filter.modality, requested)
        if (response.page != requested) throw IllegalStateException()
        val visible = visibleMeCreations(response.items.map { it.toTrackedGenerationTask(defaultTitle) }, filter)
        if (visible.isNotEmpty() || !response.hasMore) return MeVisibleCreationPage(visible, response.page, response.hasMore)
        requested++
    }
}

@Composable
internal fun MeCreationsRoute(
    profileId: String, profileName: String, avatar: Any?, balance: Int, avatarSaving: Boolean,
    completionRevision: String, loadPage: suspend (String?, Int) -> PagedResult<VisualResource>,
    onEditName: () -> Unit, onEditAvatar: () -> Unit, onSettings: () -> Unit,
    onOpen: (TrackedGenerationTask) -> Unit, onNavigation: (Int) -> Unit,
    onPurchase: () -> Unit = {},
    initialFilter: MeCreationFilter = MeCreationFilter.Creations, onFilterChanged: (MeCreationFilter) -> Unit = {},
) {
    val context = LocalContext.current
    val loadError by rememberUpdatedState(stringResource(R.string.task_history_load_error))
    val defaultTaskTitle by rememberUpdatedState(stringResource(R.string.default_task_title))
    val scope = rememberCoroutineScope()
    var filter by rememberSaveable(profileId) { mutableStateOf(initialFilter) }
    var items by remember(profileId) { mutableStateOf<List<TrackedGenerationTask>>(emptyList()) }
    var loading by remember(profileId) { mutableStateOf(true) }
    var error by remember(profileId) { mutableStateOf<String?>(null) }
    var page by remember(profileId) { mutableIntStateOf(1) }
    var hasMore by remember(profileId) { mutableStateOf(false) }
    var loadingMore by remember(profileId) { mutableStateOf(false) }
    var pageError by remember(profileId) { mutableStateOf<String?>(null) }
    var allCount by remember(profileId) { mutableStateOf<Int?>(null) }
    var refresh by remember(profileId) { mutableIntStateOf(0) }
    var generation by remember(profileId) { mutableIntStateOf(0) }
    var moreJob by remember(profileId) { mutableStateOf<Job?>(null) }
    val loader by rememberUpdatedState(loadPage)
    fun message(failure: Exception) = context.diagnosticMessage(
        operationUserMessage(failure, loadError), businessOperationDiagnostic(failure, "E-501"))
    DisposableEffect(profileId) { onDispose { moreJob?.cancel() } }
    LaunchedEffect(profileId, filter, refresh, completionRevision) {
        val owned = ++generation
        moreJob?.cancel()
        items = emptyList(); loading = true; error = null; loadingMore = false; pageError = null; hasMore = false
        try {
            val result = readMeCreationPage(filter, 1, defaultTaskTitle, loader)
            if (owned != generation) return@LaunchedEffect
            items = result.items
            page = result.page; hasMore = result.hasMore
            if (filter == MeCreationFilter.Creations) allCount = if (result.hasMore) null else items.size
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (failure: Exception) { if (owned == generation) error = message(failure) }
        finally { if (owned == generation) loading = false }
    }
    val avatarModel = remember(avatar, context) {
        if (avatar is String) ImageRequest.Builder(context).data(avatar).memoryCachePolicy(CachePolicy.DISABLED)
            .diskCachePolicy(CachePolicy.DISABLED).build() else avatar
    }
    MeCreationsScreen(profileName, avatarModel, balance, avatarSaving, filter, allCount, items, loading, error,
        hasMore, loadingMore, pageError, onFilter = { if (it != filter) { generation++; moreJob?.cancel(); items = emptyList(); loading = true; error = null; hasMore = false; filter = it; onFilterChanged(it) } },
        onRetry = { refresh++ }, onLoadMore = {
            if (!loading && !loadingMore && hasMore) {
                val owned = generation
                val queryFilter = filter
                val nextPage = page + 1
                loadingMore = true; pageError = null
                moreJob = scope.launch {
                    try {
                        val result = readMeCreationPage(queryFilter, nextPage, defaultTaskTitle, loader)
                        if (owned != generation || queryFilter != filter) return@launch
                        items = visibleMeCreations(items + result.items, queryFilter)
                        page = result.page; hasMore = result.hasMore
                        if (filter == MeCreationFilter.Creations) allCount = if (result.hasMore) null else items.size
                    } catch (cancelled: CancellationException) { throw cancelled }
                    catch (failure: Exception) { if (owned == generation) pageError = message(failure) }
                    finally { if (owned == generation) loadingMore = false }
                }
            }
        }, onEditName = onEditName, onEditAvatar = onEditAvatar, onSettings = onSettings, onOpen = onOpen, onNavigation = onNavigation, onPurchase = onPurchase)
}
