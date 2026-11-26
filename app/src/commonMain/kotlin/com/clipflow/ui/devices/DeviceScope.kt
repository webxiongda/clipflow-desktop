package com.clipflow.ui.devices

import androidx.compose.runtime.Composable
import com.clipflow.db.sync.SyncRuntimeInfo
import com.clipflow.platform.Platform

interface DeviceScope : PlatformScope {

    var syncRuntimeInfo: SyncRuntimeInfo

    override val platform: Platform
        get() = syncRuntimeInfo.platform

    override fun getDeviceDisplayName(): String = syncRuntimeInfo.getDeviceDisplayName()

    @Composable
    fun DeviceConnectView()

    @Composable
    fun TrustDeviceView()
}
