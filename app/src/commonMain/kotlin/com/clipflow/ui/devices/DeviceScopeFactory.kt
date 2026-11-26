package com.clipflow.ui.devices

import com.clipflow.db.sync.SyncRuntimeInfo

interface DeviceScopeFactory {

    fun createDeviceScope(syncRuntimeInfo: SyncRuntimeInfo): DeviceScope
}
