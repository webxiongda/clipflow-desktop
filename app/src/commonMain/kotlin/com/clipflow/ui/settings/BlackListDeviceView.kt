package com.clipflow.ui.settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.clipflow.sync.NearbyDeviceManager
import com.clipflow.ui.base.GeneralIconButton
import com.clipflow.ui.devices.SyncDeviceView
import com.clipflow.ui.devices.SyncScope
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Remove
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScope.BlackListDeviceView() {
    val nearbyDeviceManager = koinInject<NearbyDeviceManager>()

    SyncDeviceView {
        GeneralIconButton(
            imageVector = MaterialSymbols.Rounded.Remove,
            desc = "remove_blacklist",
            colors =
                IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
        ) {
            nearbyDeviceManager.unblockDevice(syncInfo.appInfo.appInstanceId)
        }
    }
}
