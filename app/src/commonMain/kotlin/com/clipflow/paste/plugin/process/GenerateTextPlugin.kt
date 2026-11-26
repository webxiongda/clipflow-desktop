package com.clipflow.paste.plugin.process

import com.clipflow.paste.item.CreatePasteItemHelper.createTextPasteItem
import com.clipflow.paste.item.HtmlPasteItem
import com.clipflow.paste.item.PasteCoordinate
import com.clipflow.paste.item.PasteItem
import com.clipflow.paste.item.PasteItemReader
import com.clipflow.paste.item.RtfPasteItem
import com.clipflow.paste.item.TextPasteItem

class GenerateTextPlugin(
    private val pasteItemReader: PasteItemReader,
) : PasteProcessPlugin {
    override fun process(
        pasteCoordinate: PasteCoordinate,
        pasteItems: List<PasteItem>,
        source: String?,
    ): List<PasteItem> {
        if (pasteItems.any { it is HtmlPasteItem } &&
            pasteItems.all { it !is TextPasteItem }
        ) {
            pasteItems.filterIsInstance<HtmlPasteItem>().firstOrNull()?.let {
                val text = pasteItemReader.getText(it)
                if (text.isEmpty()) return pasteItems
                return pasteItems +
                    createTextPasteItem(
                        text = text,
                    )
            }
        }

        if (pasteItems.any { it is RtfPasteItem } &&
            pasteItems.all { it !is TextPasteItem }
        ) {
            pasteItems.filterIsInstance<RtfPasteItem>().firstOrNull()?.let {
                val text = pasteItemReader.getText(it)
                if (text.isEmpty()) return pasteItems
                return pasteItems +
                    createTextPasteItem(
                        text = text,
                    )
            }
        }

        return pasteItems
    }
}
