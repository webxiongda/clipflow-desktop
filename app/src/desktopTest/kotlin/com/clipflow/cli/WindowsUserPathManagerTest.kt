package com.clipflow.cli

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WindowsUserPathManagerTest {

    private val binDir = "C:\\Tools\\ClipFlow\\app\\bin"

    @Test
    fun `contains matches entries case-insensitively and ignoring trailing separators`() {
        assertTrue(pathValueContainsDir("C:\\Windows;c:\\tools\\clipflow\\APP\\BIN", binDir))
        assertTrue(pathValueContainsDir("C:\\Tools\\ClipFlow\\app\\bin\\;C:\\Windows", binDir))
        assertFalse(pathValueContainsDir("C:\\Windows;C:\\Tools\\Other\\bin", binDir))
    }

    @Test
    fun `contains matches quoted entries`() {
        assertTrue(pathValueContainsDir("\"C:\\Tools\\ClipFlow\\app\\bin\";C:\\Windows", binDir))
    }

    @Test
    fun `contains ignores empty entries and empty values`() {
        assertFalse(pathValueContainsDir(null, binDir))
        assertFalse(pathValueContainsDir("", binDir))
        assertFalse(pathValueContainsDir(";;;", binDir))
        // An empty target must never match the empty entries a stray
        // double-semicolon produces
        assertFalse(pathValueContainsDir("C:\\Windows;;C:\\Other", ""))
    }

    @Test
    fun `contains matches entries through environment variable expansion`() {
        val expand = { s: String -> s.replace("%CLIPFLOW%", "C:\\Tools\\ClipFlow") }
        assertTrue(pathValueContainsDir("%CLIPFLOW%\\app\\bin;C:\\Windows", binDir, expand))
        assertFalse(pathValueContainsDir("%OTHER%\\app\\bin", binDir, expand))
    }

    @Test
    fun `append handles absent and trailing-separator values`() {
        assertEquals(binDir, appendToPathValue(null, binDir))
        assertEquals(binDir, appendToPathValue("", binDir))
        assertEquals("C:\\Windows;$binDir", appendToPathValue("C:\\Windows", binDir))
        assertEquals("C:\\Windows;$binDir", appendToPathValue("C:\\Windows;", binDir))
    }

    @Test
    fun `promote puts the dir first and removes duplicates of it only`() {
        assertEquals(
            "$binDir;C:\\Windows;C:\\Other",
            promoteInPathValue("C:\\Windows;c:\\tools\\clipflow\\app\\bin\\;C:\\Other", binDir),
        )
        assertEquals(binDir, promoteInPathValue(null, binDir))
        assertEquals("$binDir;C:\\Windows", promoteInPathValue("C:\\Windows", binDir))
    }

    @Test
    fun `promote removes entries matching through expansion`() {
        val expand = { s: String -> s.replace("%CLIPFLOW%", "C:\\Tools\\ClipFlow") }
        assertEquals(
            "$binDir;C:\\Windows",
            promoteInPathValue("%CLIPFLOW%\\app\\bin;C:\\Windows", binDir, expand),
        )
    }

    @Test
    fun `msix payload detection matches WindowsApps with either separator`() {
        assertTrue(isMsixPayloadPath("C:\\Program Files\\WindowsApps\\pkg_2.2.0_x64\\app\\bin\\clipflow-cli.exe"))
        assertTrue(isMsixPayloadPath("C:/Program Files/WindowsApps/pkg/app/bin/clipflow-cli.exe"))
        assertFalse(isMsixPayloadPath("C:\\Tools\\ClipFlow\\app\\bin\\clipflow-cli.exe"))
        // The user PATH alias directory is not a payload path but does end in
        // WindowsApps — must not match without a trailing separator
        assertFalse(isMsixPayloadPath("C:\\Users\\me\\AppData\\Local\\Microsoft\\WindowsApps"))
    }
}
