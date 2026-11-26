package com.clipflow.macos

import com.clipflow.platform.macos.MacosKeychainHelper
import com.clipflow.utils.getPlatformUtils
import org.junit.jupiter.api.Assumptions.assumeTrue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MacosKeychainHelperTest {

    @Test
    fun testKeychain() {
        val platform = getPlatformUtils().platform
        assumeTrue(platform.isMacos(), "Test requires macOS")

        MacosKeychainHelper.setPassword("com.clipflow", "test", "test")
        var password = MacosKeychainHelper.getPassword("com.clipflow", "test")
        assertEquals("test", password)
        MacosKeychainHelper.updatePassword("com.clipflow", "test", "test1")
        password = MacosKeychainHelper.getPassword("com.clipflow", "test")
        assertEquals("test1", password)
        assertTrue(MacosKeychainHelper.deletePassword("com.clipflow", "test"))
        val password1 = MacosKeychainHelper.getPassword("com.clipflow", "test")
        assertEquals(null, password1)
    }
}
