package com.clipflow.serializer

import com.clipflow.paste.PasteCollection
import com.clipflow.paste.PasteData
import com.clipflow.paste.PasteState
import com.clipflow.paste.PasteType
import com.clipflow.paste.item.CreatePasteItemHelper.createTextPasteItem
import com.clipflow.paste.item.TextPasteItem
import com.clipflow.paste.plugin.type.DesktopTextTypePlugin
import com.clipflow.utils.DateUtils
import com.clipflow.utils.getJsonUtils
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SerializerTest {

    private val jsonUtils = getJsonUtils()

    @Test
    fun testPasteData() {
        val textPasteItem =
            createTextPasteItem(
                identifiers = listOf(DesktopTextTypePlugin.TEXT),
                text = "testPasteData",
            )

        val testStart = DateUtils.nowEpochMilliseconds()
        val sentinelCreateTime = testStart - 10_000L
        val pasteData =
            PasteData(
                pasteAppearItem = textPasteItem,
                pasteCollection = PasteCollection(listOf()),
                pasteType = PasteType.TEXT_TYPE.type,
                pasteSearchContent = textPasteItem.text.lowercase(),
                hash = textPasteItem.hash,
                size = textPasteItem.size,
                pasteState = PasteState.LOADED,
                createTime = sentinelCreateTime,
                appInstanceId = UUID.randomUUID().toString(),
            )

        val json = jsonUtils.JSON.encodeToString(pasteData)
        println(json)
        val newPasteData: PasteData = jsonUtils.JSON.decodeFromString(json)
        val newTextPasteItem = newPasteData.pasteAppearItem
        assertTrue(newTextPasteItem is TextPasteItem)
        assertEquals(textPasteItem.text, newTextPasteItem.text)
        assertEquals(textPasteItem.hash, newTextPasteItem.hash)
        assertEquals(textPasteItem.identifiers, newTextPasteItem.identifiers)
        // pasteSearchContent is transient and not serialized; deserializer sets it to null
        assertEquals(null, newPasteData.pasteSearchContent)
        assertEquals(pasteData.pasteType, newPasteData.pasteType)
        assertEquals(pasteData.hash, newPasteData.hash)
        assertEquals(PasteState.LOADING, newPasteData.pasteState)
        // createTime is @Transient: not serialized, defaults to now() on deserialize,
        // so it must drop the sentinel and land at or after the test start.
        assertNotEquals(sentinelCreateTime, newPasteData.createTime)
        assertTrue(newPasteData.createTime >= testStart)
        assertEquals(pasteData.appInstanceId, newPasteData.appInstanceId)
        // Default value for @Transient remote is false; callers set it explicitly
        assertEquals(false, newPasteData.remote)
    }
}
