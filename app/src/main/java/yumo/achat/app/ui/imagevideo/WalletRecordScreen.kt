package yumo.achat.app.ui.imagevideo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import yumo.achat.app.R
import yumo.achat.app.ui.error.withDiagnosticCode
import yumo.achat.app.ui.theme.*
import yumo.achat.core.backend.WalletTransaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.text.ParsePosition
import java.util.TimeZone
import java.util.Locale

internal fun formatWalletRecordAmount(amount: Int, type: String, locale: Locale = Locale.getDefault()): String {
    val signed = walletTransactionDisplayAmount(amount, type)
    return (if (signed >= 0) "+" else "") + NumberFormat.getNumberInstance(locale).apply {
        isGroupingUsed = false; minimumFractionDigits = 2; maximumFractionDigits = 2
    }.format(signed)
}

internal fun walletRecordDate(raw: String, zone: TimeZone = TimeZone.getDefault()): String {
    // SimpleDateFormat supports the app's API 24 minimum. Normalize sub-millisecond precision
    // before parsing so six-digit server fractions cannot accidentally become extra seconds.
    val source = raw.replace(Regex("\\.(\\d+)(?=Z|[+-]\\d{2}:\\d{2}|$)")) {
        "." + it.groupValues[1].take(3).padEnd(3, '0')
    }
    val patterns = listOf("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ss")
    for (pattern in patterns) {
        val position = ParsePosition(0)
        val date = SimpleDateFormat(pattern, Locale.ROOT).apply { isLenient = false; timeZone = zone }.parse(source, position)
        if (date != null && position.index == source.length) {
            return SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.ROOT).apply { timeZone = zone }.format(date)
        }
    }
    return raw
}

@Composable
internal fun WalletRecordRoute(profileId: String, loadPage: suspend (Int, Int) -> List<WalletTransaction>, onBack: () -> Unit) {
    val loader by rememberUpdatedState(loadPage)
    val fallback by rememberUpdatedState(stringResource(R.string.record_load_error))
    val diagnosticFormat by rememberUpdatedState(stringResource(R.string.error_with_code))
    val scope = rememberCoroutineScope()
    var rows by remember(profileId) { mutableStateOf<List<WalletTransaction>>(emptyList()) }
    var loading by remember(profileId) { mutableStateOf(true) }
    var error by remember(profileId) { mutableStateOf<String?>(null) }
    var moreError by remember(profileId) { mutableStateOf<String?>(null) }
    var page by remember(profileId) { mutableIntStateOf(1) }
    var more by remember(profileId) { mutableStateOf(false) }
    var loadingMore by remember(profileId) { mutableStateOf(false) }
    var revision by remember(profileId) { mutableIntStateOf(0) }
    var generation by remember(profileId) { mutableIntStateOf(0) }
    var moreJob by remember(profileId) { mutableStateOf<Job?>(null) }
    fun message(failure: Exception) = withDiagnosticCode(operationUserMessage(failure, fallback),
        businessOperationDiagnostic(failure, "E-501"), diagnosticFormat)
    DisposableEffect(profileId) { onDispose { moreJob?.cancel() } }
    LaunchedEffect(profileId, revision) {
        val owned = ++generation
        moreJob?.cancel(); rows = emptyList(); loading = true; error = null; moreError = null; loadingMore = false; more = false
        try {
            val result = loader(1, 20)
            if (owned != generation) return@LaunchedEffect
            rows = result.distinctBy { it.id }; page = 1; more = result.size == 20
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (failure: Exception) { if (owned == generation) error = message(failure) }
        finally { if (owned == generation) loading = false }
    }
    WalletRecordScreen(rows, loading, error, more, loadingMore, moreError, onBack,
        onRefresh = { generation++; moreJob?.cancel(); revision++ },
        onLoadMore = {
            if (!loading && !loadingMore && more) {
                val owned = generation
                val next = page + 1
                loadingMore = true; moreError = null
                moreJob = scope.launch {
                    try {
                        val result = loader(next, 20)
                        if (owned != generation) return@launch
                        val newRows = result.filter { item -> rows.none { it.id == item.id } }
                        rows = (rows + newRows).distinctBy { it.id }; page = next
                        more = result.size == 20 && newRows.isNotEmpty()
                    } catch (cancelled: CancellationException) { throw cancelled }
                    catch (failure: Exception) { if (owned == generation) moreError = message(failure) }
                    finally { if (owned == generation) loadingMore = false }
                }
            }
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WalletRecordScreen(
    rows: List<WalletTransaction>, loading: Boolean, error: String?, hasMore: Boolean, loadingMore: Boolean,
    moreError: String?, onBack: () -> Unit, onRefresh: () -> Unit, onLoadMore: () -> Unit,
) {
    var help by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = !help, onBack = onBack)
    Column(Modifier.fillMaxSize().background(EloraRecordBackground).drawBehind {
        drawRect(Brush.radialGradient(listOf(EloraWelcomePurple.copy(alpha = .22f), Color.Transparent),
            center = Offset(0f, 0f), radius = size.width * 1.2f))
        drawRect(Brush.radialGradient(listOf(EloraWelcomeMagenta.copy(alpha = .13f), Color.Transparent),
            center = Offset(size.width, size.height * .55f), radius = size.width))
    }.safeDrawingPadding()) {
        Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            RecordIcon(R.drawable.elora_record_back, stringResource(R.string.record_back), onBack)
            Text(stringResource(R.string.record_title), color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            RecordIcon(R.drawable.elora_record_help, stringResource(R.string.record_help)) { help = true }
        }
        LazyColumn(Modifier.fillMaxSize().testTag("wallet-record-list"), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when {
                loading -> item { Text(stringResource(R.string.record_loading), color = EloraAccountMuted) }
                error != null -> item { Column { Text(error, color = EloraWelcomeError); TextButton(onClick = onRefresh) { Text(stringResource(R.string.top_up_retry)) } } }
                rows.isEmpty() -> item { Text(stringResource(R.string.record_empty), color = EloraAccountMuted) }
                else -> items(rows, key = { it.id }) { WalletRecordItem(it) }
            }
            if (!loading && error == null) item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    moreError?.let { Text(it, color = EloraWelcomeError, fontSize = 12.sp) }
                    if (loadingMore) Text(stringResource(R.string.record_loading), color = EloraAccountMuted)
                    else if (hasMore) TextButton(onClick = onLoadMore) {
                        Text(stringResource(if (moreError != null) R.string.record_retry_more else R.string.record_load_more))
                    }
                    TextButton(onClick = onRefresh, enabled = !loadingMore) { Text(stringResource(R.string.record_refresh)) }
                }
            }
        }
    }
    if (help) ModalBottomSheet(onDismissRequest = { help = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp), containerColor = EloraRecordSheet, dragHandle = {
            Box(Modifier.padding(top = 12.dp).size(48.dp, 6.dp).clip(CircleShape).background(EloraAccountHint.copy(alpha = .7f)))
        }) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                RecordIcon(R.drawable.elora_record_close, stringResource(R.string.preview_close)) { help = false }
            }
            Text(stringResource(R.string.record_help_message), color = EloraAccountLabel, fontSize = 12.sp, lineHeight = 21.sp,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = .03f))
                    .border(1.dp, Color.White.copy(alpha = .12f), RoundedCornerShape(16.dp)).padding(15.dp))
            Row(Modifier.fillMaxWidth().clip(CircleShape).background(Brush.horizontalGradient(listOf(EloraAccountButtonStart, EloraAccountButtonMiddle, EloraWelcomeMagenta)))
                .border(1.dp, Color.White.copy(alpha = .2f), CircleShape).clickable(role = Role.Button) { help = false }.padding(17.dp),
                horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.record_understand), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(8.dp)); Image(painterResource(R.drawable.elora_record_check), null, Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun WalletRecordItem(item: WalletTransaction) {
    val amount = walletTransactionDisplayAmount(item.amount, item.type)
    val positive = amount > 0
    val category = item.category.orEmpty().lowercase(Locale.ROOT)
    val video = category in setOf("video", "video_generation", "generation_video")
    val image = category in setOf("image", "image_generation", "generation_image")
    val title = item.description?.takeIf { it.isNotBlank() } ?: stringResource(when {
        video -> R.string.record_video
        image -> R.string.record_image
        category in setOf("recharge", "refund", "top_up", "purchase") -> R.string.record_credit
        else -> R.string.credits_wallet_transaction
    })
    val accent = if (positive) EloraSettingsPrivacy else EloraWelcomePurple
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
        .background(Brush.horizontalGradient(if (positive) listOf(EloraRecordIncomeStart, EloraRecordIncomeEnd) else listOf(EloraRecordExpenseStart, EloraRecordExpenseEnd)))
        .border(1.dp, if (positive) accent.copy(alpha = .35f) else Color.White.copy(alpha = .10f), RoundedCornerShape(16.dp))
        .padding(15.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = .16f))
            .border(1.dp, accent.copy(alpha = .3f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Image(painterResource(when { video -> R.drawable.elora_record_video; image -> R.drawable.elora_catalog_image; else -> R.drawable.elora_record_credit }), null, Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            if (item.createdAt.isNotBlank()) Text(walletRecordDate(item.createdAt), color = EloraAccountMuted,
                fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
        Text(formatWalletRecordAmount(item.amount, item.type), color = if (positive) EloraSettingsPrivacy else Color.White,
            fontFamily = FontFamily.Monospace, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.widthIn(max = 140.dp))
    }
}

@Composable private fun RecordIcon(icon: Int, label: String, onClick: () -> Unit) {
    Box(Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = .05f))
        .border(1.dp, Color.White.copy(alpha = .1f), CircleShape).clickable(role = Role.Button, onClick = onClick)
        .semantics { contentDescription = label }, contentAlignment = Alignment.Center) {
        Image(painterResource(icon), null, Modifier.size(20.dp))
    }
}
