package com.clipflow.net

import com.clipflow.db.sync.HostInfo

data class NetworkInterfaceInfo(
    val name: String,
    val networkPrefixLength: Short,
    val hostAddress: String,
    val isLikelyVirtual: Boolean = false,
) {
    fun toHostInfo(): HostInfo =
        HostInfo(
            networkPrefixLength = networkPrefixLength,
            hostAddress = hostAddress,
        )

    override fun toString(): String = "$name - $hostAddress/$networkPrefixLength"
}
