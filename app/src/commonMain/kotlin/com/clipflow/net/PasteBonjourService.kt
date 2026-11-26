package com.clipflow.net

import com.clipflow.db.sync.HostInfo

interface PasteBonjourService {

    fun refreshAll()

    fun refreshTarget(
        appInstanceId: String,
        hostInfoList: List<HostInfo>,
    )

    fun close()
}
