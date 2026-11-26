package com.clipflow.paste.plugin.process

import com.clipflow.paste.item.PasteCoordinate
import com.clipflow.paste.item.PasteItem

interface PasteProcessPlugin {

    fun process(
        pasteCoordinate: PasteCoordinate,
        pasteItems: List<PasteItem>,
        source: String?,
    ): List<PasteItem>
}
