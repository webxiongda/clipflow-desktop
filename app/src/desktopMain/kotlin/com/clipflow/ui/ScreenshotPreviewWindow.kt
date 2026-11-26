package com.clipflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.clipflow.app.DesktopAppWindowManager
import com.clipflow.ui.DesktopContext.PastePanelWindowContext
import com.clipflow.ui.model.ScreenshotPreviewViewModel
import com.clipflow.ui.paste.createPasteDataScope
import com.clipflow.ui.paste.side.preview.ImageSidePreviewView
import com.clipflow.ui.theme.AppUISize.small
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Chevron_left
import com.composables.icons.materialsymbols.rounded.Chevron_right
import com.composables.icons.materialsymbols.rounded.Close
import org.koin.compose.koinInject

/**
 * A small always-on-top bubble that appears right after a screenshot is captured.
 *
 * It shows the newest image in the clipboard history and lets the user walk back
 * through earlier screenshots with the chevrons — no click needed to open the main
 * panel. The window never takes focus, so the screenshot tool keeps working.
 */
@Composable
fun ScreenshotPreviewWindow(windowIcon: androidx.compose.ui.graphics.painter.Painter?) {
    val appWindowManager = koinInject<DesktopAppWindowManager>()
    val viewModel = koinInject<ScreenshotPreviewViewModel>()

    val windowInfo by appWindowManager.screenshotPreviewInfo.collectAsState()
    val images by viewModel.images.collectAsState()
    val currentId by viewModel.currentId.collectAsState()

    NonActivatingWindow(
        visible = windowInfo.show,
        state = windowInfo.state,
        title = appWindowManager.screenshotPreviewWindowTitle,
        transparent = false,
        onClosing = appWindowManager::hideScreenshotPreviewWindow,
    ) {
        PastePanelWindowContext {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
                val current = images.firstOrNull { it.id == currentId } ?: images.firstOrNull()
                if (current != null) {
                    createPasteDataScope(current)?.let { scope ->
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 40.dp),
                        ) {
                            with(scope) { ImageSidePreviewView() }
                        }
                    }

                    val index = images.indexOfFirst { it.id == current.id }
                    Text(
                        text = "${index + 1} / ${images.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier =
                            Modifier
                                .align(Alignment.TopStart)
                                .padding(small),
                    )
                } else {
                    Text(
                        text = "No screenshot yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                Row(
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { viewModel.prev() },
                        enabled = images.firstOrNull()?.id != currentId,
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Transparent),
                    ) {
                        Icon(MaterialSymbols.Rounded.Chevron_left, contentDescription = "Newer screenshot")
                    }
                    Spacer(Modifier.width(small))
                    IconButton(
                        onClick = { viewModel.next() },
                        enabled = images.lastOrNull()?.id != currentId,
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Transparent),
                    ) {
                        Icon(MaterialSymbols.Rounded.Chevron_right, contentDescription = "Older screenshot")
                    }
                    Spacer(Modifier.width(small))
                    IconButton(
                        onClick = viewModel::hide,
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Transparent),
                    ) {
                        Icon(MaterialSymbols.Rounded.Close, contentDescription = "Close")
                    }
                }
            }
        }
    }
}
