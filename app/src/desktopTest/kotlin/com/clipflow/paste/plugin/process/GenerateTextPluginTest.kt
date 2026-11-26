package com.clipflow.paste.plugin.process

import com.clipflow.paste.item.CreatePasteItemHelper.createHtmlPasteItem
import com.clipflow.paste.item.CreatePasteItemHelper.createTextPasteItem
import com.clipflow.paste.item.DefaultPasteItemReader
import com.clipflow.paste.item.HtmlPasteItem
import com.clipflow.paste.item.PasteCoordinate
import com.clipflow.paste.item.PasteItem
import com.clipflow.paste.item.TextPasteItem
import com.clipflow.utils.getJsonUtils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenerateTextPluginTest {

    @Suppress("unused")
    private val jsonUtils = getJsonUtils()

    private val pasteItemReader = DefaultPasteItemReader()
    private val plugin = GenerateTextPlugin(pasteItemReader)

    private val coord = PasteCoordinate(id = 1L, appInstanceId = "test")

    @Test
    fun `empty list returns empty`() {
        val result = plugin.process(coord, emptyList(), null)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `list with text only returns unchanged`() {
        val textItem = createTextPasteItem(text = "hello")
        val result = plugin.process(coord, listOf(textItem), null)
        assertEquals(1, result.size)
        assertTrue(result[0] is TextPasteItem)
    }

    @Test
    fun `html without text generates text item`() {
        val htmlItem = createHtmlPasteItem(html = "<p>hello world</p>")
        val items: List<PasteItem> = listOf(htmlItem)
        val result = plugin.process(coord, items, null)
        assertEquals(2, result.size)
        assertTrue(result.any { it is HtmlPasteItem })
        assertTrue(result.any { it is TextPasteItem })
    }

    @Test
    fun `html with existing text does not generate duplicate`() {
        val htmlItem = createHtmlPasteItem(html = "<p>hello</p>")
        val textItem = createTextPasteItem(text = "existing")
        val items: List<PasteItem> = listOf(htmlItem, textItem)
        val result = plugin.process(coord, items, null)
        assertEquals(2, result.size)
        // No new text item added
        assertEquals(1, result.count { it is TextPasteItem })
    }

    @Test
    fun `generated text from html has content`() {
        val htmlItem = createHtmlPasteItem(html = "<b>bold text</b>")
        val items: List<PasteItem> = listOf(htmlItem)
        val result = plugin.process(coord, items, null)
        val textItem = result.filterIsInstance<TextPasteItem>().first()
        assertTrue(textItem.text.isNotEmpty())
    }
}
