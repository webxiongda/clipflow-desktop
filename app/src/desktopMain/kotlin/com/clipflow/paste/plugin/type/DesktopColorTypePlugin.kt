package com.clipflow.paste.plugin.type

import com.clipflow.paste.PasteCollector
import com.clipflow.paste.PasteDataFlavor
import com.clipflow.paste.PasteTransferable
import com.clipflow.paste.PasteType
import com.clipflow.paste.PasteWriteScope
import com.clipflow.paste.item.PasteItem

class DesktopColorTypePlugin : ColorTypePlugin {

    override fun getPasteType(): PasteType = PasteType.COLOR_TYPE

    override fun getIdentifiers(): List<String> = listOf()

    override fun createPrePasteItem(
        itemIndex: Int,
        identifier: String,
        pasteTransferable: PasteTransferable,
        pasteCollector: PasteCollector,
    ) {
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
    }

    override suspend fun buildTransferable(
        pasteItem: PasteItem,
        scope: PasteWriteScope,
        map: MutableMap<PasteDataFlavor, Any>,
    ) {
    }
}
