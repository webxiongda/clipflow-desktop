package com.clipflow.net.routing

import com.clipflow.dto.sync.SyncInfo
import com.clipflow.sync.SyncHandler
import com.clipflow.utils.ioDispatcher
import kotlinx.coroutines.CoroutineScope

class TestSyncRoutingApi : SyncRoutingApi {

    var syncInfo: SyncInfo? = null

    val innerSyncHandlers = mutableMapOf<String, SyncHandler>()

    override val realTimeSyncScope: CoroutineScope = CoroutineScope(ioDispatcher)

    override fun getSyncHandlers(): Map<String, SyncHandler> = innerSyncHandlers

    override fun updateSyncInfo(syncInfo: SyncInfo) {
        this.syncInfo = syncInfo
    }

    override fun trustSyncInfo(
        syncInfo: SyncInfo,
        host: String?,
    ) {
        this.syncInfo = syncInfo
    }

    override fun removeSyncHandler(appInstanceId: String) {
        innerSyncHandlers.remove(appInstanceId)
    }
}
