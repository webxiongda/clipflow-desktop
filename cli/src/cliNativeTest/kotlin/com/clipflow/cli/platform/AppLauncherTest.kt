package com.clipflow.cli.platform

import okio.Path.Companion.toPath
import kotlin.experimental.ExperimentalNativeApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalNativeApi::class)
class AppLauncherTest {

    // Environment → launch-mode wiring (headless is decided inside
    // createAppLauncher, so these pin the whole decision, not just the string)

    @Test
    fun headlessEnvironmentOnLinuxProducesAHeadlessLauncher() {
        val launcher = createAppLauncher(CliAppPathProvider(), guiEnvironment = false, os = OsFamily.LINUX)
        assertTrue(assertIs<LinuxAppLauncher>(launcher).headless)
    }

    @Test
    fun guiEnvironmentOnLinuxProducesAGuiLauncher() {
        val launcher = createAppLauncher(CliAppPathProvider(), guiEnvironment = true, os = OsFamily.LINUX)
        assertFalse(assertIs<LinuxAppLauncher>(launcher).headless)
    }

    @Test
    fun linuxGuiLaunchCommandHasNoHeadlessFlag() {
        val command = buildLinuxLaunchCommand("/opt/clipflow/bin/clipflow".toPath(), headless = false)
        assertEquals("nohup \"/opt/clipflow/bin/clipflow\" > /dev/null 2>&1 &", command)
    }

    @Test
    fun linuxHeadlessLaunchCommandPassesHeadlessFlag() {
        val command = buildLinuxLaunchCommand("/opt/clipflow/bin/clipflow".toPath(), headless = true)
        assertEquals("nohup \"/opt/clipflow/bin/clipflow\" --headless > /dev/null 2>&1 &", command)
    }
}
