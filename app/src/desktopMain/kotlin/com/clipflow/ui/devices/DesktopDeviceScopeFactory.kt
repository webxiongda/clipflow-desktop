package com.clipflow.ui.devices

import com.clipflow.db.sync.SyncRuntimeInfo

class DesktopDeviceScopeFactory : DeviceScopeFactory {
    override fun createDeviceScope(syncRuntimeInfo: SyncRuntimeInfo): DeviceScope = DesktopDeviceScope(syncRuntimeInfo)
}
