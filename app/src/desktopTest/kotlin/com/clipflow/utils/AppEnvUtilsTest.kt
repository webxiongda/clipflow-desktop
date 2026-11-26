package com.clipflow.utils

import com.clipflow.app.AppEnv
import kotlin.test.Test
import kotlin.test.assertTrue

class AppEnvUtilsTest {

    @Test
    fun testGetAppEnv() {
        assertTrue { getAppEnvUtils().getCurrentAppEnv() == AppEnv.TEST }
    }
}
