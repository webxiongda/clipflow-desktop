package com.clipflow.ui.devices

import com.clipflow.dto.sync.SyncInfo

class DesktopSyncScopeFactory : SyncScopeFactory {
    override fun createSyncScope(syncInfo: SyncInfo): SyncScope = DesktopSyncScope(syncInfo)
}
