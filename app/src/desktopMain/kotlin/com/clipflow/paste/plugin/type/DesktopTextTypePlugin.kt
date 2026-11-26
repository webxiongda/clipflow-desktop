package com.clipflow.paste.plugin.type

import com.clipflow.paste.PasteCollector
import com.clipflow.paste.PasteDataFlavor
import com.clipflow.paste.PasteTransferable
import com.clipflow.paste.PasteType
import com.clipflow.paste.PasteWriteScope
import com.clipflow.paste.item.CreatePasteItemHelper.createTextPasteItem
import com.clipflow.paste.item.PasteItem
import com.clipflow.paste.item.TextPasteItem
import com.clipflow.paste.toPasteDataFlavor
import java.awt.datatransfer.DataFlavor

class DesktopTextTypePlugin : TextTypePlugin {

    companion object {

        const val UNICODE_STRING = "Unicode String"
        const val TEXT = "text/plain"
        const val PLAIN_TEXT = "Plain Text"
    }

    override fun getPasteType(): PasteType = PasteType.TEXT_TYPE

    override fun getIdentifiers(): List<String> = listOf(UNICODE_STRING, TEXT, PLAIN_TEXT)

    override fun createPrePasteItem(
        itemIndex: Int,
        identifier: String,
        pasteTransferable: PasteTransferable,
        pasteCollector: PasteCollector,
    ) {
        createTextPasteItem(
            identifiers = listOf(identifier),
            text = "",
        ).let {
            pasteCollector.preCollectItem(itemIndex, this::class, it)
        }
    }

    override suspend fun doLoadRepresentation(
        transferData: Any,
        pasteId: Long,
        itemIndex: Int,
        identity: String,
        dataFlavor: PasteDataFlavor,
        dataFlavorMap: Map<String, List<PasteDataFlavor>>,
        pasteTransferable: PasteTransferable,
        pasteCollector: PasteCollector,
    ) {
        if (transferData is String) {
            val update: (PasteItem) -> PasteItem = { pasteItem ->
                createTextPasteItem(
                    identifiers = pasteItem.identifiers,
                    text = transferData,
                    extraInfo = pasteItem.extraInfo,
                )
            }
            pasteCollector.updateCollectItem(itemIndex, this::class, update)
        }
    }

    override suspend fun buildTransferable(
        pasteItem: PasteItem,
        scope: PasteWriteScope,
        map: MutableMap<PasteDataFlavor, Any>,
    ) {
        pasteItem as TextPasteItem
        map[DataFlavor.stringFlavor.toPasteDataFlavor()] = pasteItem.text
    }
}
