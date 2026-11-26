package com.clipflow.paste.plugin.type

import com.clipflow.app.AppInfo
import com.clipflow.config.CommonConfigManager
import com.clipflow.paste.DesktopPasteDataFlavor
import com.clipflow.paste.PasteDataFlavor
import com.clipflow.paste.PasteDataFlavors
import com.clipflow.paste.PasteWriteScope
import com.clipflow.paste.item.FilesPasteItem
import com.clipflow.paste.toPasteDataFlavor
import com.clipflow.path.UserDataPathProvider
import com.clipflow.platform.Platform
import com.clipflow.utils.getJsonUtils
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import java.awt.datatransfer.DataFlavor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopFilesTypePluginTest {

    // Guard against PasteItem/JsonUtils circular class initialization
    private val jsonUtils = getJsonUtils()

    private val appInfo: AppInfo = mockk(relaxed = true)
    private val configManager: CommonConfigManager = mockk(relaxed = true)
    private val platform: Platform =
        mockk(relaxed = true) {
            every { isLinux() } returns false
        }
    private val userDataPathProvider: UserDataPathProvider =
        mockk {
            every { resolve(any(), any<String>(), any(), any()) } answers {
                firstArg<okio.Path>() / secondArg<String>()
            }
        }

    private val plugin =
        DesktopFilesTypePlugin(
            appInfo = appInfo,
            configManager = configManager,
            platform = platform,
            userDataPathProvider = userDataPathProvider,
        )

    private val baseDir = System.getProperty("java.io.tmpdir").toPath()
    private val singleFile =
        FilesPasteItem(
            identifiers = listOf("files"),
            count = 1,
            hash = "files-hash",
            size = 100,
            fileInfoTreeMap = emptyMap(),
            relativePathList = listOf("test.txt"),
            basePath = baseDir.toString(),
        )

    @Test
    fun `buildTransferable with PRIMARY_CATEGORY scope only writes file list`() =
        runTest {
            val map = mutableMapOf<PasteDataFlavor, Any>()
            plugin.buildTransferable(singleFile, PasteWriteScope.PRIMARY_CATEGORY, map)

            val flavors = map.keys.map { (it as DesktopPasteDataFlavor).dataFlavor }.toSet()

            assertTrue(flavors.contains(DataFlavor.javaFileListFlavor))
            assertFalse(flavors.contains(DataFlavor.stringFlavor))
            assertFalse(flavors.contains(PasteDataFlavors.URI_LIST_FLAVOR))
            assertFalse(flavors.contains(PasteDataFlavors.URL_FLAVOR))
        }

    @Test
    fun `buildTransferable with ALL scope writes file list and string and URI flavors`() =
        runTest {
            val map = mutableMapOf<PasteDataFlavor, Any>()
            plugin.buildTransferable(singleFile, PasteWriteScope.ALL, map)

            val flavors = map.keys.map { (it as DesktopPasteDataFlavor).dataFlavor }.toSet()

            assertTrue(flavors.contains(DataFlavor.javaFileListFlavor))
            assertTrue(flavors.contains(DataFlavor.stringFlavor))
            assertTrue(flavors.contains(PasteDataFlavors.URI_LIST_FLAVOR))
            assertTrue(flavors.contains(PasteDataFlavors.URL_FLAVOR))

            assertEquals("test.txt", map[DataFlavor.stringFlavor.toPasteDataFlavor()])
        }
}
