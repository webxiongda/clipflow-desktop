package com.clipflow.ui.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.clipflow.app.AppFileChooser
import com.clipflow.app.FileSelectionMode
import com.clipflow.config.CommonConfigManager
import com.clipflow.i18n.GlobalCopywriter
import com.clipflow.notification.MessageType
import com.clipflow.notification.NotificationManager
import com.clipflow.path.DesktopMigration
import com.clipflow.path.UserDataPathProvider
import com.clipflow.ui.LocalThemeExtState
import com.clipflow.ui.base.IconData
import com.clipflow.ui.base.SectionHeader
import com.clipflow.ui.theme.AppUISize.medium
import com.clipflow.ui.theme.AppUISize.small2X
import com.clipflow.ui.theme.AppUISize.xxLarge
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Folder
import okio.Path
import org.koin.compose.koinInject

class DesktopStoragePathManager : StoragePathManager {
    @Composable
    override fun StoragePathHeader() {
        SectionHeader("storage_directory", topPadding = medium)
    }

    /**
     * Shows where user data currently lives and offers an explicit "Change"
     * action that picks a new directory and hands it to [MigrationStorageDialog].
     *
     * Migration is one-way (the old directory keeps non-user files, so it can
     * never be selected again as an empty target), which is why this is an
     * action button rather than a switch that pretends the choice is reversible.
     */
    @Composable
    override fun StoragePathContentView() {
        val appFileChooser = koinInject<AppFileChooser>()
        val configManager = koinInject<CommonConfigManager>()
        val copywriter = koinInject<GlobalCopywriter>()
        val desktopMigration = koinInject<DesktopMigration>()
        val notificationManager = koinInject<NotificationManager>()
        val userDataPathProvider = koinInject<UserDataPathProvider>()
        val themeExt = LocalThemeExtState.current

        val config by configManager.config.collectAsState()

        var migrationPath by remember { mutableStateOf<Path?>(null) }

        val currentStoragePath =
            remember(config) {
                userDataPathProvider.getUserDataPath()
            }

        migrationPath?.let { path ->
            MigrationStorageDialog(path) {
                migrationPath = null
            }
        }

        val chooseMigrationPath = {
            appFileChooser.openFileChooser(
                FileSelectionMode.DIRECTORY_ONLY,
                currentStoragePath,
            ) { path ->
                desktopMigration.checkMigrationPath(path as Path)?.let { errorMessage ->
                    notificationManager.sendNotification(
                        title = { it.getText(errorMessage) },
                        messageType = MessageType.Error,
                        duration = null,
                    )
                } ?: run {
                    migrationPath = path
                }
            }
        }

        SettingSectionCard {
            SettingListItem(
                titleContent = {
                    Text(
                        text = currentStoragePath.toString(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                subtitle =
                    if (config.useDefaultStoragePath) {
                        "storage_path_default"
                    } else {
                        "storage_path_custom"
                    },
                icon = IconData(MaterialSymbols.Rounded.Folder, themeExt.amberIconColor),
                trailingContent = {
                    FilledTonalButton(
                        onClick = chooseMigrationPath,
                        modifier = Modifier.height(xxLarge),
                        contentPadding = PaddingValues(horizontal = small2X),
                    ) {
                        Text(
                            text = copywriter.getText("change"),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                },
            )
        }
    }
}
