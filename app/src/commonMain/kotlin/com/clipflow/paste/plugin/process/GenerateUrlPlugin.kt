package com.clipflow.paste.plugin.process

import com.clipflow.paste.item.CreatePasteItemHelper.createUrlPasteItem
import com.clipflow.paste.item.PasteCoordinate
import com.clipflow.paste.item.PasteItem
import com.clipflow.paste.item.TextPasteItem
import com.clipflow.paste.item.UrlPasteItem
import com.clipflow.utils.getUrlUtils
import com.clipflow.utils.normalizeUrl

object GenerateUrlPlugin : PasteProcessPlugin {

    private val urlUtils = getUrlUtils()

    override fun process(
        pasteCoordinate: PasteCoordinate,
        pasteItems: List<PasteItem>,
        source: String?,
    ): List<PasteItem> {
        if (pasteItems.all { it !is UrlPasteItem }) {
            pasteItems.filterIsInstance<TextPasteItem>().firstOrNull()?.let {
                val text = normalizeUrl(it.text)
                if (urlUtils.isValidUrl(text)) {
                    return pasteItems +
                        createUrlPasteItem(
                            identifiers = it.identifiers,
                            url = text,
                        )
                }
            }
        }
        return pasteItems
    }
}
