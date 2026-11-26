package com.clipflow.paste.plugin.process

import com.clipflow.paste.item.PasteCoordinate
import com.clipflow.paste.item.PasteItem

object RemoveInvalidPlugin : PasteProcessPlugin {
    override fun process(
        pasteCoordinate: PasteCoordinate,
        pasteItems: List<PasteItem>,
        source: String?,
    ): List<PasteItem> = pasteItems.filter { it.isValid() }
}
