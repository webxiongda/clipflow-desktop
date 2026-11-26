package com.clipflow.paste.plugin.type

import com.clipflow.paste.NoneTransferData
import com.clipflow.paste.PasteCollector
import com.clipflow.paste.PasteDataFlavor
import com.clipflow.paste.PasteTransferable
import com.clipflow.paste.PasteType
import com.clipflow.paste.PasteWriteScope
import com.clipflow.paste.item.PasteItem

interface PasteTypePlugin {

    fun getPasteType(): PasteType

    fun getIdentifiers(): List<String>

    fun createPrePasteItem(
        itemIndex: Int,
        identifier: String,
        pasteTransferable: PasteTransferable,
        pasteCollector: PasteCollector,
    )

    // identity: unused on Desktop, required by Android/iOS to resolve platform-specific representations
    suspend fun loadRepresentation(
        pasteId: Long,
        itemIndex: Int,
        identity: String,
        dataFlavor: PasteDataFlavor,
        dataFlavorMap: Map<String, List<PasteDataFlavor>>,
        pasteTransferable: PasteTransferable,
        pasteCollector: PasteCollector,
    ) {
        runCatching {
            val transferData = pasteTransferable.getTransferData(dataFlavor)
            if (transferData != NoneTransferData) {
                doLoadRepresentation(
                    transferData,
                    pasteId,
                    itemIndex,
                    identity,
                    dataFlavor,
                    dataFlavorMap,
                    pasteTransferable,
                    pasteCollector,
                )
            }
        }.onFailure {
            collectError(it, pasteId, itemIndex, pasteCollector)
        }
    }

    suspend fun doLoadRepresentation(
        transferData: Any,
        pasteId: Long,
        itemIndex: Int,
        identity: String,
        dataFlavor: PasteDataFlavor,
        dataFlavorMap: Map<String, List<PasteDataFlavor>>,
        pasteTransferable: PasteTransferable,
        pasteCollector: PasteCollector,
    )

    fun collectError(
        error: Throwable,
        pasteId: Long,
        itemIndex: Int,
        pasteCollector: PasteCollector,
    ) {
        pasteCollector.collectError(pasteId, itemIndex, error)
    }

    /**
     * Puts the item's representations on the transferable. [scope] tells the
     * plugin whether items of the other category are written alongside.
     */
    suspend fun buildTransferable(
        pasteItem: PasteItem,
        scope: PasteWriteScope,
        map: MutableMap<PasteDataFlavor, Any>,
    )
}
