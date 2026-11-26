package com.clipflow.net

import com.clipflow.app.AppInfo
import com.clipflow.app.EndpointInfoFactory
import com.clipflow.db.sync.HostInfo
import com.clipflow.dto.sync.SyncInfo

class SyncInfoFactory(
    private val appInfo: AppInfo,
    private val endpointInfoFactory: EndpointInfoFactory,
) {

    suspend fun createSyncInfo(hostInfoList: List<HostInfo>): SyncInfo =
        SyncInfo(
            appInfo = appInfo,
            endpointInfo = endpointInfoFactory.createEndpointInfo(hostInfoList),
        )
}
