package com.clipflow.ui.devices

import com.clipflow.dto.sync.SyncInfo

interface SyncScope : PlatformScope {

    val syncInfo: SyncInfo
}
