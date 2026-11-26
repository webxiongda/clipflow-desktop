package com.clipflow.utils

import com.clipflow.platform.Platform
import okio.Path

expect fun getPlatformUtils(): PlatformUtils

interface PlatformUtils {

    val platform: Platform

    fun getSystemDownloadDir(): Path
}
