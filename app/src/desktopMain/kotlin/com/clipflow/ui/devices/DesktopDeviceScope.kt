package com.clipflow.ui.devices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.clipflow.db.sync.SyncRuntimeInfo
import com.clipflow.ui.DeviceDetail
import com.clipflow.ui.NavigationManager
import com.clipflow.ui.theme.AppUISize.tiny
import org.koin.compose.koinInject

class DesktopDeviceScope(
    override var syncRuntimeInfo: SyncRuntimeInfo,
) : DeviceScope {

    @Composable
    override fun TrustDeviceView() {
        TrustDeviceDialog()
    }

    @Composable
    override fun DeviceConnectView() {
        val navigationManager = koinInject<NavigationManager>()

        val indicator = rememberConnectingIndicator()

        DeviceRowContent(
            onClick = {
                navigationManager.navigate(DeviceDetail(syncRuntimeInfo.appInstanceId))
            },
            style = myDeviceStyle,
            iconTint = syncStateVisual(indicator.visible).iconColor,
            trailingContent = { DeviceActions(indicator) },
        )
    }
}

/**
 * Status tag + refresh/pair/upgrade + menu, shared by the list row and the detail
 * header. The [indicator] is owned by the caller so the row's platform icon is
 * recolored from the same flag.
 */
@Composable
fun DeviceScope.DeviceActions(indicator: ConnectingIndicator) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(tiny),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SyncStateTag(indicator.visible)
        DeviceActionButton(indicator.visible, indicator.onRefreshingChange)
        MyDeviceMenuButton()
    }
}
