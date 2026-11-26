package com.clipflow.cli.platform

import com.clipflow.cli.withEnv
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class UserDataDirOverrideTest {
    private val env = NativePlatformPathProvider.USER_DATA_DIR_ENV

    @Test
    fun envOverrideReplacesThePlatformDefault() {
        withEnv(env, "/tmp/clipflow-dev-user") {
            assertEquals(
                "/tmp/clipflow-dev-user".toPath(),
                createNativePlatformPathProvider().getDefaultUserDataPath(),
            )
        }
    }

    @Test
    fun blankOverrideIsIgnored() {
        withEnv(env, "  ") {
            assertNotEquals(
                "  ".toPath(),
                createNativePlatformPathProvider().getDefaultUserDataPath(),
            )
        }
    }

    @Test
    fun withoutTheEnvThePlatformDefaultIsUsed() {
        withEnv(env, null) {
            val path = createNativePlatformPathProvider().getDefaultUserDataPath().toString()
            assertEquals(true, path.contains("ClipFlow") || path.contains(".clipflow"))
        }
    }
}
