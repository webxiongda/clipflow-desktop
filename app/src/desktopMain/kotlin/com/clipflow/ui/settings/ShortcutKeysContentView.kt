package com.clipflow.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import com.clipflow.app.DesktopAppLaunchState
import com.clipflow.i18n.GlobalCopywriter
import com.clipflow.listener.DesktopShortcutKeys.Companion.HIDE_WINDOW
import com.clipflow.listener.DesktopShortcutKeys.Companion.PASTE_LOCAL_LAST
import com.clipflow.listener.DesktopShortcutKeys.Companion.PASTE_PLAIN_TEXT
import com.clipflow.listener.DesktopShortcutKeys.Companion.PASTE_PRIMARY_TYPE
import com.clipflow.listener.DesktopShortcutKeys.Companion.PASTE_REMOTE_LAST
import com.clipflow.listener.DesktopShortcutKeys.Companion.SHOW_MAIN
import com.clipflow.listener.DesktopShortcutKeys.Companion.SHOW_PASTE_PANEL
import com.clipflow.listener.DesktopShortcutKeys.Companion.SHOW_SCREENSHOT_PREVIEW
import com.clipflow.listener.DesktopShortcutKeys.Companion.SHOW_SEARCH
import com.clipflow.listener.DesktopShortcutKeys.Companion.TOGGLE_ENCRYPT
import com.clipflow.listener.DesktopShortcutKeys.Companion.TOGGLE_PASTEBOARD_MONITORING
import com.clipflow.listener.KeyboardKey
import com.clipflow.listener.ShortcutKeys
import com.clipflow.platform.Platform
import com.clipflow.ui.base.KeyboardView
import com.clipflow.ui.base.SectionHeader
import com.clipflow.ui.theme.AppUISize.medium
import com.clipflow.ui.theme.AppUISize.tiny
import org.koin.compose.koinInject

@Composable
fun ShortcutKeysContentView() {
    val appLaunchState = koinInject<DesktopAppLaunchState>()
    val platform = koinInject<Platform>()

    var showGrantAccessibilityDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (platform.isMacos() && !appLaunchState.accessibilityPermissions) {
            showGrantAccessibilityDialog = true
        }
    }

    if (showGrantAccessibilityDialog) {
        GrantAccessibilityDialog {
            showGrantAccessibilityDialog = false
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(tiny),
    ) {
        item {
            SectionHeader("paste_action")
        }

        item {
            SettingSectionCard {
                ShortcutKeyRow(PASTE_PLAIN_TEXT)
                HorizontalDivider()
                ShortcutKeyRow(PASTE_PRIMARY_TYPE)
                HorizontalDivider()
                ShortcutKeyRow(PASTE_LOCAL_LAST)
                HorizontalDivider()
                ShortcutKeyRow(PASTE_REMOTE_LAST)
            }
        }

        item {
            SectionHeader("toggle_window", topPadding = medium)
        }

        item {
            SettingSectionCard {
                ShortcutKeyRow(SHOW_MAIN)
                HorizontalDivider()
                ShortcutKeyRow(SHOW_SEARCH)
                HorizontalDivider()
                ShortcutKeyRow(SHOW_PASTE_PANEL)
                HorizontalDivider()
                ShortcutKeyRow(SHOW_SCREENSHOT_PREVIEW)
                HorizontalDivider()
                ShortcutKeyRow(HIDE_WINDOW)
            }
        }

        item {
            SectionHeader("quick_action", topPadding = medium)
        }

        item {
            SettingSectionCard {
                ShortcutKeyRow(TOGGLE_PASTEBOARD_MONITORING)
                HorizontalDivider()
                ShortcutKeyRow(TOGGLE_ENCRYPT)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShortcutKeyRow(name: String) {
    val copywriter = koinInject<GlobalCopywriter>()
    val shortcutKeys = koinInject<ShortcutKeys>()
    val shortcutKeysCore by shortcutKeys.shortcutKeysCore.collectAsState()

    var showSetShortcutKeysDialog by remember { mutableStateOf(false) }

    if (showSetShortcutKeysDialog) {
        SetShortcutKeysDialog(name) {
            showSetShortcutKeysDialog = false
        }
    }

    SettingListItem(
        title = name,
        icon = null,
        trailingContent = {
            shortcutKeysCore.keys[name]?.let { keys ->
                ShortcutKeyItemView(keys)
            } ?: run {
                Text(
                    text = copywriter.getText("unassigned"),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        },
        onClick = {
            showSetShortcutKeysDialog = true
        },
    )
}

@Composable
fun ShortcutKeyItemView(keys: List<KeyboardKey>) {
    Row(
        modifier = Modifier.wrapContentSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        keys.forEachIndexed { index, info ->
            KeyboardView(key = info.name)
            if (index != keys.size - 1) {
                Spacer(modifier = Modifier.width(tiny))
            }
        }
    }
}
