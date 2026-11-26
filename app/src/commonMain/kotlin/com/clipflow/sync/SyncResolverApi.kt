package com.clipflow.sync

interface SyncResolverApi {
    suspend fun emitEvent(event: SyncEvent)
}
