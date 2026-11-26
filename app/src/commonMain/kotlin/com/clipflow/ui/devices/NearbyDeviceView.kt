package com.clipflow.ui.devices

import androidx.compose.runtime.Composable
import com.clipflow.ui.NavigationManager
import com.clipflow.ui.NearbyDeviceDetail
import org.koin.compose.koinInject

@Composable
fun SyncScope.NearbyDeviceView() {
    val navigationManager = koinInject<NavigationManager>()

    DeviceRowContent(
        style = nearbyDeviceStyle,
        onClick = {
            navigationManager.navigate(NearbyDeviceDetail(syncInfo.appInfo.appInstanceId))
        },
        trailingContent = {
            NearbyDeviceActions()
        },
    )
}
