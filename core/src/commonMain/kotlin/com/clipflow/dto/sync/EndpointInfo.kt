package com.clipflow.dto.sync

import com.clipflow.db.sync.HostInfo
import com.clipflow.platform.Platform
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class EndpointInfo(
    val deviceId: String,
    val deviceName: String,
    val platform: Platform,
    val hostInfoList: List<HostInfo>,
    val port: Int,
) {
    override fun toString(): String = Json.encodeToString(this)
}
