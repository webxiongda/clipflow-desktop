package com.clipflow.paste.plugin.process

import androidx.compose.ui.graphics.toArgb
import com.clipflow.paste.item.ColorPasteItem
import com.clipflow.paste.item.CreatePasteItemHelper.createColorPasteItem
import com.clipflow.paste.item.PasteCoordinate
import com.clipflow.paste.item.PasteItem
import com.clipflow.paste.item.TextPasteItem
import com.clipflow.utils.getColorParser

object TextToColorPlugin : PasteProcessPlugin {

    private val colorParser = getColorParser()

    override fun process(
        pasteCoordinate: PasteCoordinate,
        pasteItems: List<PasteItem>,
        source: String?,
    ): List<PasteItem> {
        if (pasteItems.all { it !is ColorPasteItem }) {
            pasteItems.filterIsInstance<TextPasteItem>().firstOrNull()?.let {
                colorParser.toColor(it.text)?.let { color ->
                    return pasteItems +
                        createColorPasteItem(
                            identifiers = it.identifiers,
                            color = color.toArgb(),
                        )
                }
            }
        }
        return pasteItems
    }
}
