package com.clipflow.ui.devices

import com.clipflow.dto.sync.SyncInfo
import com.clipflow.platform.Platform

class DesktopSyncScope(
    override val syncInfo: SyncInfo,
) : SyncScope {

    override val platform: Platform
        get() = syncInfo.endpointInfo.platform

    override fun getDeviceDisplayName(): String = syncInfo.endpointInfo.deviceName
}
