package com.clipflow.paste

interface PasteDataFlavor

interface PasteTransferable {

    fun getTransferData(pasteDataFlavor: PasteDataFlavor): Any
}
