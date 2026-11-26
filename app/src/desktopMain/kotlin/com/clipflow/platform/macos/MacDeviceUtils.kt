package com.clipflow.platform.macos

import com.clipflow.platform.macos.api.MacosApi
import com.clipflow.platform.macos.api.MacosApi.Companion.getString

object MacDeviceUtils {

    fun getComputerName(): String? = getString(MacosApi.INSTANCE.getComputerName())

    fun getHardwareUUID(): String? = getString(MacosApi.INSTANCE.getHardwareUUID())
}
