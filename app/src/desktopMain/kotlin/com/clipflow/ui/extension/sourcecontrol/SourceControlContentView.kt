package com.clipflow.ui.extension.sourcecontrol

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.clipflow.app.AppInfo
import com.clipflow.app.DesktopAppFileChooser
import com.clipflow.app.DesktopAppWindowManager
import com.clipflow.config.DesktopConfigManager
import com.clipflow.i18n.GlobalCopywriter
import com.clipflow.notification.MessageType
import com.clipflow.notification.NotificationManager
import com.clipflow.paste.DesktopSourceExclusionService
import com.clipflow.platform.Platform
import com.clipflow.ui.LocalThemeExtState
import com.clipflow.ui.base.AppSourceIcon
import com.clipflow.ui.base.GeneralIconButton
import com.clipflow.ui.base.IconData
import com.clipflow.ui.settings.SettingListItem
import com.clipflow.ui.settings.SettingSectionCard
import com.clipflow.ui.theme.AppUISize.medium
import com.clipflow.ui.theme.AppUISize.small2X
import com.clipflow.ui.theme.AppUISize.tiny
import com.clipflow.ui.theme.AppUISize.xxLarge
import com.clipflow.ui.theme.AppUISize.xxxLarge
import com.clipflow.ui.theme.AppUISize.xxxxLarge
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Add
import com.composables.icons.materialsymbols.rounded.Remove
import org.koin.compose.koinInject

/**
 * The clipboard source exclusion list: every app is recorded unless it is
 * listed here. The list is the persisted `sourceExclusions` config itself, so
 * entries saved by earlier versions show up unchanged.
 */
@Composable
fun SourceControlContentView() {
    val configManager = koinInject<DesktopConfigManager>()
    val copywriter = koinInject<GlobalCopywriter>()
    val sourceExclusionService = koinInject<DesktopSourceExclusionService>()
    val appWindowManager = koinInject<DesktopAppWindowManager>()
    val appFileChooser = koinInject<DesktopAppFileChooser>()
    val notificationManager = koinInject<NotificationManager>()
    val platform = koinInject<Platform>()
    val themeExt = LocalThemeExtState.current

    val config by configManager.config.collectAsState()

    val exclusions =
        remember(config.sourceExclusions) {
            sourceExclusionService.getExclusions()
        }

    val patterns =
        remember(config.sourceExclusionPatterns) {
            sourceExclusionService.getPatterns()
        }

    var showPicker by remember { mutableStateOf(false) }

    // Browses for an app on disk so apps that are neither running nor in the
    // history can be excluded too. Resolution runs on the IO dispatcher.
    val browseForApp = {
        appFileChooser.openAppChooser(
            extensions = appWindowManager.appPickerExtensions,
            initPath = appWindowManager.appPickerDirectory,
        ) { path ->
            appWindowManager.resolveAppSource(path)?.let { source ->
                sourceExclusionService.addExclusion(source)
            } ?: notificationManager.sendNotification(
                title = { it.getText("source_exclusion_add_app_invalid") },
                message = { it.getText("source_exclusion_add_app_invalid_desc") },
                messageType = MessageType.Error,
            )
        }
    }

    if (showPicker) {
        SourceExclusionPickerDialog(
            exclusions = exclusions,
            onPick = { source ->
                sourceExclusionService.addExclusion(source)
                showPicker = false
            },
            onBrowse = {
                showPicker = false
                browseForApp()
            },
            onDismiss = { showPicker = false },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(tiny),
    ) {
        item {
            SettingSectionCard {
                SettingListItem(
                    title = "source_exclusion_add_app",
                    subtitle = "source_exclusion_add_app_desc",
                    icon = IconData(MaterialSymbols.Rounded.Add, themeExt.greenIconColor),
                    trailingContent = {
                        FilledTonalButton(
                            onClick = { showPicker = true },
                            modifier = Modifier.height(xxLarge),
                            contentPadding = PaddingValues(horizontal = small2X),
                        ) {
                            Text(
                                text = copywriter.getText("add"),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    },
                )
            }
        }
        item {
            SourceExclusionPatternsSection(
                patterns = patterns,
                subtitle =
                    if (platform.isLinux()) {
                        "source_exclusion_linux_wmclass_note"
                    } else {
                        "source_exclusion_patterns_desc"
                    },
                onAdd = { sourceExclusionService.addPattern(it) },
                onRemove = { sourceExclusionService.removePattern(it) },
            )
        }
        item {
            if (exclusions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(medium),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = copywriter.getText("source_exclusion_empty"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                SettingSectionCard {
                    exclusions.forEachIndexed { index, source ->
                        ExcludedSourceItem(
                            source = source,
                            onRemove = { sourceExclusionService.removeExclusion(source) },
                        )
                        if (index < exclusions.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(start = xxxxLarge))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExcludedSourceItem(
    source: String,
    onRemove: () -> Unit,
) {
    val appInfo = koinInject<AppInfo>()
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = medium, vertical = tiny),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(medium),
    ) {
        AppSourceIcon(
            source = source,
            appInstanceId = appInfo.appInstanceId,
            size = xxxLarge,
        )
        Text(
            text = source,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        GeneralIconButton(
            imageVector = MaterialSymbols.Rounded.Remove,
            desc = "source_exclusion_remove",
            colors =
                IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            onClick = onRemove,
        )
    }
}
