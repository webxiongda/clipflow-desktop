package com.clipflow.ui.paste.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.dp
import com.clipflow.app.DesktopAppWindowManager
import com.clipflow.notification.NotificationManager
import com.clipflow.paste.PasteContentEditor
import com.clipflow.paste.item.TextPasteItem
import com.clipflow.platform.Platform
import com.clipflow.ui.base.CustomTextField
import com.clipflow.ui.base.InnerScaffold
import com.clipflow.ui.paste.PasteDataScope
import com.clipflow.ui.theme.AppUIFont.pasteTextStyle
import com.clipflow.ui.theme.AppUISize.mediumRoundedCornerShape
import com.clipflow.ui.theme.AppUISize.small2X
import com.clipflow.ui.theme.AppUISize.tiny
import com.clipflow.ui.theme.AppUISize.tinyRoundedCornerShape
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Close
import com.composables.icons.materialsymbols.rounded.Redo
import com.composables.icons.materialsymbols.rounded.Undo
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun PasteDataScope.PasteTextEditContentView() {
    val appWindowManager = koinInject<DesktopAppWindowManager>()
    val notificationManager = koinInject<NotificationManager>()
    val pasteContentEditor = koinInject<PasteContentEditor>()
    val platform = koinInject<Platform>()

    val scope = rememberCoroutineScope()
    val isMac = remember { platform.isMacos() }

    val originalText = remember(pasteData.id, pasteData.hash) { getPasteItem(TextPasteItem::class).text }
    val history = remember(pasteData.id, pasteData.hash) { TextEditHistory(originalText) }
    val hasChanges = history.text != originalText && history.text.isNotEmpty()

    fun save() {
        if (!hasChanges) return
        scope.launch {
            val outcome = pasteContentEditor.updateContent(pasteData, history.text, pasteData.hash)
            if (notificationManager.notifyEditOutcome(outcome)) {
                appWindowManager.hideBubbleWindow()
            }
        }
    }

    InnerScaffold(
        modifier =
            Modifier
                .fillMaxSize()
                .clip(tinyRoundedCornerShape)
                .onPreviewKeyEvent { keyEvent ->
                    keyEvent.isSaveShortcut(isMac).also { if (it) save() }
                },
        containerColor = MaterialTheme.colorScheme.surface,
        floatingActionButton = {
            TextEditFloatingToolbar(
                hasChanges = hasChanges,
                canUndo = history.canUndo,
                canRedo = history.canRedo,
                onSave = { save() },
                onUndo = { history.undo() },
                onRedo = { history.redo() },
                onClose = { appWindowManager.hideBubbleWindow() },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = small2X)
                    .padding(top = small2X, bottom = 85.dp)
                    .clip(mediumRoundedCornerShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            CustomTextField(
                modifier =
                    Modifier
                        .fillMaxSize(),
                shape = RoundedCornerShape(tiny),
                value = history.text,
                onValueChange = { history.push(it) },
                textStyle = pasteTextStyle,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TextEditFloatingToolbar(
    hasChanges: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onSave: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClose: () -> Unit,
) {
    HorizontalFloatingToolbar(
        modifier = Modifier.offset(y = 20.dp),
        expanded = true,
        floatingActionButton = { EditSaveButton(hasChanges = hasChanges, onSave = onSave) },
        colors =
            FloatingToolbarDefaults.standardFloatingToolbarColors(
                toolbarContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
    ) {
        IconButton(onClick = onUndo, enabled = canUndo) {
            Icon(imageVector = MaterialSymbols.Rounded.Undo, contentDescription = "undo")
        }
        IconButton(onClick = onRedo, enabled = canRedo) {
            Icon(imageVector = MaterialSymbols.Rounded.Redo, contentDescription = "redo")
        }
        IconButton(onClick = onClose) {
            Icon(imageVector = MaterialSymbols.Rounded.Close, contentDescription = "cancel")
        }
    }
}
