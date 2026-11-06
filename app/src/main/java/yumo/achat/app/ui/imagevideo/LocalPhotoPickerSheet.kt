package yumo.achat.app.ui.imagevideo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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

internal fun recordPickerPhotoHistory(history: List<String>, uri: String): List<String> =
    (listOf(uri) + history).filter(String::isNotBlank).distinct().take(12)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LocalPhotoPickerSheet(
    recentPhotos: List<String>, selectedPhoto: String?, onDismiss: () -> Unit,
    onBrowseDevice: () -> Unit, onSelectPhoto: (String) -> Unit, onClearRecent: () -> Unit,
) {
    var albums by rememberSaveable { mutableStateOf(false) }
    var optionsOpen by remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp), containerColor = EloraPickerBackground,
        dragHandle = { Box(Modifier.padding(top = 13.dp, bottom = 12.dp).size(40.dp, 4.dp).background(EloraPickerHandle, CircleShape)) }) {
        Column(Modifier.fillMaxWidth().heightIn(max = 520.dp).verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.picker_access_caption), color = EloraPickerMuted, fontSize = 12.sp,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                PickerIcon(R.drawable.elora_picker_close, stringResource(R.string.picker_close), onDismiss)
                Row(Modifier.clip(CircleShape).background(EloraPickerTabs)
                    .border(1.dp, EloraWelcomePurple.copy(alpha = .25f), CircleShape).padding(4.dp)) {
                    PickerTab(stringResource(R.string.picker_photos), !albums) { albums = false }
                    PickerTab(stringResource(R.string.picker_albums), albums) { albums = true }
                }
                Box {
                    PickerIcon(R.drawable.elora_picker_options, stringResource(R.string.picker_options)) { optionsOpen = true }
                    DropdownMenu(expanded = optionsOpen, onDismissRequest = { optionsOpen = false }) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.picker_browse_device)) },
                            onClick = { optionsOpen = false; onBrowseDevice() })
                        DropdownMenuItem(text = { Text(stringResource(R.string.picker_clear_recent)) }, enabled = recentPhotos.isNotEmpty(),
                            onClick = { optionsOpen = false; onClearRecent() })
                    }
                }
            }
            if (albums) {
                Text(stringResource(R.string.picker_albums), color = androidx.compose.ui.graphics.Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                if (recentPhotos.isNotEmpty()) {
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(EloraPickerTabs)
                        .clickable(role = Role.Button) { albums = false }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(recentPhotos.first(), null, Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop)
                        Text(stringResource(R.string.picker_selected_album, recentPhotos.size), color = EloraWelcomeSoftLilac,
                            fontSize = 13.sp, modifier = Modifier.padding(start = 12.dp))
                    }
                }
                Text(stringResource(R.string.picker_browse_albums), color = EloraWelcomeLilac,
                    modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onBrowseDevice).padding(vertical = 12.dp))
            } else {
                Text(stringResource(R.string.picker_recent), color = androidx.compose.ui.graphics.Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().height(108.dp)) {
                    item(key = "add-photo") {
                        val add = stringResource(R.string.picker_add_photo)
                        Box(Modifier.size(108.dp).clip(RoundedCornerShape(16.dp)).background(EloraPickerTabs)
                            .drawBehind {
                                drawRoundRect(EloraWelcomePurple.copy(alpha = .4f),
                                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                                    style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))))
                            }
                            .clickable(role = Role.Button, onClick = onBrowseDevice).semantics { contentDescription = add },
                            contentAlignment = Alignment.Center) {
                            Image(painterResource(R.drawable.elora_picker_add), null, Modifier.size(28.dp))
                        }
                    }
                    itemsIndexed(recentPhotos, key = { _, uri -> uri }) { index, uri ->
                        val selected = uri == selectedPhoto
                        val label = stringResource(R.string.picker_recent_photo, index + 1)
                        Box(Modifier.size(108.dp).clip(RoundedCornerShape(16.dp)).background(EloraPickerTabs)
                            .border(if (selected) 2.dp else 1.dp, if (selected) EloraWelcomePurple else EloraWelcomePurple.copy(alpha = .3f), RoundedCornerShape(16.dp))
                            .selectable(selected = selected, role = Role.RadioButton, onClick = { onSelectPhoto(uri) })
                            .semantics { contentDescription = label }.padding(2.dp)) {
                            Text(label, color = EloraPickerMuted, fontSize = 11.sp, modifier = Modifier.align(Alignment.Center))
                            AsyncImage(uri, null, Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)), contentScale = ContentScale.Crop)
                            Box(Modifier.align(Alignment.TopEnd).padding(6.dp).size(20.dp).clip(CircleShape)
                                .background(if (selected) EloraPickerSelected else EloraPickerHandle)
                                .border(1.dp, EloraPickerCheckBorder, CircleShape), contentAlignment = Alignment.Center) {
                                if (selected) Image(painterResource(R.drawable.elora_picker_check), null, Modifier.size(12.dp))
                            }
                        }
                    }
                }
                if (recentPhotos.isEmpty()) Text(stringResource(R.string.picker_empty), color = EloraPickerMuted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun PickerIcon(icon: Int, description: String, onClick: () -> Unit) {
    Box(Modifier.size(48.dp).clip(CircleShape).background(EloraPickerIconSurface)
        .border(1.dp, EloraWelcomeLilac.copy(alpha = .2f), CircleShape)
        .clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = description }, contentAlignment = Alignment.Center) {
        Image(painterResource(icon), null, Modifier.size(16.dp))
    }
}

@Composable
private fun PickerTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(Modifier.clip(CircleShape).then(if (selected) Modifier.background(
        androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(EloraWelcomeViolet, EloraPreviewButtonEnd))) else Modifier)
        .selectable(selected, role = Role.Tab, onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
        Text(label, color = if (selected) androidx.compose.ui.graphics.Color.White else EloraPickerMuted,
            fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}
