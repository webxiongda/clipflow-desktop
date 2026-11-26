package com.clipflow.sync

import com.clipflow.app.AppInfo
import com.clipflow.dto.sync.EndpointInfo
import com.clipflow.dto.sync.SyncInfo
import com.clipflow.platform.Platform
import com.clipflow.platform.Platform.Companion.LINUX
import com.clipflow.utils.ioDispatcher
import com.clipflow.utils.namedScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MarketingNearbyDeviceManager : NearbyDeviceManager {

    override val nearbyDeviceScope: CoroutineScope = namedScope(ioDispatcher, "MarketingNearbyDeviceManager")

    override val searching: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override val nearbySyncInfos: StateFlow<List<SyncInfo>> =
        MutableStateFlow(
            listOf(
                SyncInfo(
                    appInfo =
                        AppInfo(
                            appInstanceId = "2",
                            appVersion = "1.1.1",
                            appRevision = "Unknown",
                            userName = "user",
                        ),
                    endpointInfo =
                        EndpointInfo(
                            deviceId = "3",
                            deviceName = "device3",
                            platform =
                                Platform(
                                    name = LINUX,
                                    arch = "arm",
                                    bitMode = 64,
                                    version = "Ubuntu 22.04 LTS",
                                ),
                            hostInfoList = listOf(),
                            port = 0,
                        ),
                ),
            ),
        )

    override fun addDevice(syncInfo: SyncInfo) {
    }

    override fun removeDevice(appInstanceId: String) {
    }

    override fun blockDevice(syncInfo: SyncInfo) {
    }

    override fun unblockDevice(appInstanceId: String): Boolean = false

    override fun startSearching() {
        searching.value = true
    }

    override fun stopSearching() {
        searching.value = false
    }
}
