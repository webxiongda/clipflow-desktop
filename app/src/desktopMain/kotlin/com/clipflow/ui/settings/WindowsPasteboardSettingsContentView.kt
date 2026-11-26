package com.clipflow.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.clipflow.config.DesktopConfigManager
import com.clipflow.ui.LocalThemeExtState
import com.clipflow.ui.base.IconData
import com.clipflow.ui.theme.AppUISize.xxxxLarge
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Desktop_windows
import org.koin.compose.koinInject

@Composable
fun WindowsPasteboardSettingsContentView() {
    val configManager = koinInject<DesktopConfigManager>()
    val themeExt = LocalThemeExtState.current

    val config by configManager.config.collectAsState()

    HorizontalDivider(modifier = Modifier.padding(start = xxxxLarge))

    SettingListSwitchItem(
        title = "legacy_app_compatibility",
        icon = IconData(MaterialSymbols.Rounded.Desktop_windows, themeExt.indigoIconColor),
        checked = config.legacySoftwareCompatibility,
    ) { newLegacySoftwareCompatibility ->
        configManager.updateConfig(
            "legacySoftwareCompatibility",
            newLegacySoftwareCompatibility,
        )
    }
}
