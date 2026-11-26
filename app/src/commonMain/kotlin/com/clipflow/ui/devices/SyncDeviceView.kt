package com.clipflow.ui.devices

import androidx.compose.runtime.Composable

@Composable
fun SyncScope.SyncDeviceView(content: @Composable SyncScope.() -> Unit) {
    DeviceRowContent(
        style = nearbyDeviceStyle,
        trailingContent = {
            content()
        },
    )
}
