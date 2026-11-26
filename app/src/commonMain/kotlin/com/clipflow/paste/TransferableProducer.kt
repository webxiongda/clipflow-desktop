package com.clipflow.paste

import com.clipflow.paste.item.PasteItem

interface TransferableProducer {

    suspend fun produce(
        pasteItem: PasteItem,
        localOnly: Boolean,
    ): PasteTransferable?

    suspend fun produce(
        pasteData: PasteData,
        localOnly: Boolean,
        scope: PasteWriteScope,
    ): PasteTransferable?
}
