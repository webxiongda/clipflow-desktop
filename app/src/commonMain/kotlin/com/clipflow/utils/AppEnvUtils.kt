package com.clipflow.utils

import com.clipflow.app.AppEnv
import com.clipflow.app.AppEnv.BETA
import com.clipflow.app.AppEnv.DEVELOPMENT
import com.clipflow.app.AppEnv.PRODUCTION
import com.clipflow.app.AppEnv.TEST

expect fun getAppEnvUtils(): AppEnvUtils

interface AppEnvUtils {

    fun getCurrentAppEnv(): AppEnv

    fun isProduction(): Boolean {
        val appEnv = getCurrentAppEnv()
        return appEnv == PRODUCTION || appEnv == BETA
    }

    // use in mobile app
    fun isBeta(): Boolean = getCurrentAppEnv() == BETA

    fun isDevelopment(): Boolean = getCurrentAppEnv() == DEVELOPMENT

    fun isTest(): Boolean = getCurrentAppEnv() == TEST
}
