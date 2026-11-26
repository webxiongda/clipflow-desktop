package com.clipflow.ui.devices

import com.clipflow.dto.sync.SyncInfo

interface SyncScopeFactory {

    fun createSyncScope(syncInfo: SyncInfo): SyncScope
}
