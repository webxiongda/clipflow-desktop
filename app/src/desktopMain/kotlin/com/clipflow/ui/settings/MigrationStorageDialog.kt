package com.clipflow.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import com.clipflow.app.AppExitService
import com.clipflow.app.AppRestartService
import com.clipflow.app.ExitMode
import com.clipflow.i18n.GlobalCopywriter
import com.clipflow.path.DesktopMigration
import com.clipflow.ui.LocalAppSizeValueState
import com.clipflow.ui.LocalExitApplication
import com.clipflow.ui.base.DialogActionButton
import com.clipflow.ui.base.DialogButtonType
import com.clipflow.ui.theme.AppUISize.medium
import com.clipflow.ui.theme.AppUISize.tiny
import com.clipflow.ui.theme.AppUISize.tiny3X
import com.clipflow.ui.theme.AppUISize.xLarge
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Storage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okio.Path
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun MigrationStorageDialog(
    path: Path,
    onDismiss: () -> Unit,
) {
    val appExitService = koinInject<AppExitService>()
    val appRestartService = koinInject<AppRestartService>()
    val copywriter = koinInject<GlobalCopywriter>()
    val desktopMigration = koinInject<DesktopMigration>()

    val appSizeValue = LocalAppSizeValueState.current
    val exitApplication = LocalExitApplication.current

    var isMigration by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0.0f) }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        modifier = Modifier.width(appSizeValue.dialogWidth),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = {
            if (!isMigration) {
                onDismiss()
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = tiny),
            ) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.Storage,
                    contentDescription = null,
                    modifier = Modifier.size(xLarge),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(medium))
                Text(
                    text = copywriter.getText("storage_migration_title"),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        },
        text = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = tiny),
                verticalArrangement = Arrangement.spacedBy(xLarge),
            ) {
                Text(
                    text = copywriter.getText("storage_migration_desc"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f,
                )
                OutlinedTextField(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                    value = path.toString(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(copywriter.getText("storage_migration_path")) },
                    textStyle = MaterialTheme.typography.bodySmall,
                    singleLine = false,
                    minLines = 1,
                    maxLines = 5,
                    shape = MaterialTheme.shapes.medium,
                )
                if (isMigration) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(tiny3X),
                        progress = { progress },
                    )
                }
            }
        },
        confirmButton = {
            DialogActionButton(
                text = copywriter.getText("confirm"),
                type = DialogButtonType.TONAL,
                enabled = !isMigration,
            ) {
                appExitService.beforeExitList.clear()
                appExitService.beforeReleaseLockList.clear()

                appExitService.beforeExitList.add {
                    isMigration = true
                    coroutineScope.launch {
                        while (progress < 0.99f && isMigration) {
                            progress += 0.01f
                            delay(100.milliseconds)
                        }
                    }
                }

                appExitService.beforeReleaseLockList.add {
                    runCatching {
                        desktopMigration.migration(path)
                        coroutineScope.launch {
                            progress = 1f
                            delay(500.milliseconds)
                            isMigration = false
                        }
                    }.onFailure {
                        coroutineScope.launch {
                            isMigration = false
                        }
                    }
                }
                appRestartService.restart { exitApplication(ExitMode.MIGRATION) }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isMigration,
            ) {
                Text(copywriter.getText("cancel"))
            }
        },
    )
}
