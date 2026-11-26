package com.clipflow.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.clipflow.ui.LocalThemeExtState
import com.clipflow.ui.NavigationManager
import com.clipflow.ui.NetworkSettings
import com.clipflow.ui.PasteboardSettings
import com.clipflow.ui.StorageSettings
import com.clipflow.ui.base.IconData
import com.clipflow.ui.theme.AppUISize.xxxxLarge
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Content_paste
import com.composables.icons.materialsymbols.rounded.Storage
import com.composables.icons.materialsymbols.rounded.Sync
import org.koin.compose.koinInject

/**
 * The three stages a clipboard item goes through, in order: what is recorded,
 * what is synced to other devices, and how long it is kept.
 */
@Composable
fun ClipboardSettingsEntryView() {
    val navigationManager = koinInject<NavigationManager>()
    val themeExt = LocalThemeExtState.current
    SettingSectionCard {
        SettingListItem(
            title = "pasteboard_settings",
            subtitle = "pasteboard_settings_desc",
            icon = IconData(MaterialSymbols.Rounded.Content_paste, themeExt.indigoIconColor),
        ) {
            navigationManager.navigate(PasteboardSettings)
        }
        HorizontalDivider(modifier = Modifier.padding(start = xxxxLarge))
        SettingListItem(
            title = "network_settings",
            subtitle = "network_settings_desc",
            icon = IconData(MaterialSymbols.Rounded.Sync, themeExt.greenIconColor),
        ) {
            navigationManager.navigate(NetworkSettings)
        }
        HorizontalDivider(modifier = Modifier.padding(start = xxxxLarge))
        SettingListItem(
            title = "storage_settings",
            subtitle = "storage_settings_desc",
            icon = IconData(MaterialSymbols.Rounded.Storage, themeExt.amberIconColor),
        ) {
            navigationManager.navigate(StorageSettings)
        }
    }
}
