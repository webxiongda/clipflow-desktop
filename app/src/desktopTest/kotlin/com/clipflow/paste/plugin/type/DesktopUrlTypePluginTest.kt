package com.clipflow.paste.plugin.type

import com.clipflow.paste.PasteDataFlavor
import com.clipflow.paste.PasteDataFlavors.URL_FLAVOR
import com.clipflow.paste.PasteWriteScope
import com.clipflow.paste.item.CreatePasteItemHelper.createUrlPasteItem
import com.clipflow.paste.toPasteDataFlavor
import com.clipflow.platform.Platform
import com.clipflow.utils.getJsonUtils
import kotlinx.coroutines.test.runTest
import java.awt.datatransfer.DataFlavor
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals

class DesktopUrlTypePluginTest {

    // Guard against PasteItem/JsonUtils circular class initialization
    private val jsonUtils = getJsonUtils()

    private val plugin = DesktopUrlTypePlugin(Platform(Platform.LINUX, "x86_64", 64, "6"))

    private val stringFlavor = DataFlavor.stringFlavor.toPasteDataFlavor()

    @Test
    fun `offers the url as plain text next to the java url flavor`() =
        runTest {
            val item = createUrlPasteItem(url = "https://example.com/a?b=1")
            val map = linkedMapOf<PasteDataFlavor, Any>()

            plugin.buildTransferable(item, scope = PasteWriteScope.PRIMARY_CATEGORY, map)

            @Suppress("DEPRECATION")
            assertEquals(URL(item.url), map[URL_FLAVOR.toPasteDataFlavor()])
            assertEquals(item.url, map[stringFlavor])
        }

    @Test
    fun `keeps the text a text item already put on the transferable`() =
        runTest {
            val item = createUrlPasteItem(url = "https://example.com")
            val map = linkedMapOf<PasteDataFlavor, Any>(stringFlavor to "shared text")

            plugin.buildTransferable(item, scope = PasteWriteScope.ALL, map)

            assertEquals("shared text", map[stringFlavor])
        }
}
