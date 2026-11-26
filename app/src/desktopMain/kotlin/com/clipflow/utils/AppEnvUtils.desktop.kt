package com.clipflow.utils

import com.clipflow.app.AppEnv
import com.clipflow.app.AppEnv.PRODUCTION

actual fun getAppEnvUtils(): AppEnvUtils = DesktopAppEnvUtils

object DesktopAppEnvUtils : AppEnvUtils {

    private val SYSTEM_PROPERTY = getSystemProperty()

    private val CURRENT_APP_ENV = getAppEnv()

    private fun getAppEnv(): AppEnv =
        SYSTEM_PROPERTY.getOption("appEnv")?.let {
            runCatching {
                AppEnv.valueOf(it)
            }.getOrElse {
                PRODUCTION
            }
        } ?: PRODUCTION

    override fun getCurrentAppEnv(): AppEnv = CURRENT_APP_ENV
}
