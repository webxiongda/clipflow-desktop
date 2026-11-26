package com.clipflow.paste.plugin.type

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.clipflow.paste.PasteCollector
import com.clipflow.paste.PasteDataFlavor
import com.clipflow.paste.PasteTransferable
import com.clipflow.paste.PasteType
import com.clipflow.paste.PasteWriteScope
import com.clipflow.paste.item.CreatePasteItemHelper.createHtmlPasteItem
import com.clipflow.paste.item.HtmlPasteItem
import com.clipflow.paste.item.PasteItem
import com.clipflow.paste.item.PasteItem.Companion.updateExtraInfo
import com.clipflow.paste.item.PasteItemProperties.BACKGROUND
import com.clipflow.paste.toPasteDataFlavor
import com.clipflow.platform.Platform
import com.clipflow.platform.windows.html.HTMLCodec
import com.clipflow.plugin.office.OfficeHtmlPlugin
import com.clipflow.utils.HtmlColorUtils
import com.clipflow.utils.getHtmlUtils
import kotlinx.serialization.json.put
import java.awt.datatransfer.DataFlavor

class DesktopHtmlTypePlugin(
    private val platform: Platform,
) : HtmlTypePlugin {

    companion object {

        const val HTML_ID = "text/html"

        private val htmlUtils = getHtmlUtils()

        private val officeHtmlPlugin = OfficeHtmlPlugin()
    }

    override fun getPasteType(): PasteType = PasteType.HTML_TYPE

    override fun getIdentifiers(): List<String> = listOf(HTML_ID)

    override fun createPrePasteItem(
        itemIndex: Int,
        identifier: String,
        pasteTransferable: PasteTransferable,
        pasteCollector: PasteCollector,
    ) {
        createHtmlPasteItem(
            identifiers = listOf(HTML_ID),
            html = "",
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
            val html = extractHtml(transferData)
            val background = HtmlColorUtils.getBackgroundColor(html) ?: Color.Transparent
            val update: (PasteItem) -> PasteItem = { pasteItem ->
                createHtmlPasteItem(
                    identifiers = pasteItem.identifiers,
                    html = html,
                    extraInfo =
                        updateExtraInfo(
                            pasteItem.extraInfo,
                            update = {
                                put(BACKGROUND, background.toArgb())
                            },
                        ),
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
        pasteItem as HtmlPasteItem
        var currentHtml = pasteItem.html
        if (platform.isWindows()) {
            currentHtml = HTMLCodec.convertToHTMLFormat(currentHtml).decodeToString()
        }
        map[DataFlavor.selectionHtmlFlavor.toPasteDataFlavor()] = currentHtml
        map[DataFlavor.fragmentHtmlFlavor.toPasteDataFlavor()] = currentHtml
        map[DataFlavor.allHtmlFlavor.toPasteDataFlavor()] = currentHtml
    }

    private fun extractHtml(inputStr: String): String {
        // this is microsoft html format
        // https://learn.microsoft.com/zh-cn/windows/win32/dataxchg/html-clipboard-format?redirectedfrom=MSDN
        return if (inputStr.startsWith("Version:")) {
            extractHtmlFromMicrosoftHtml(inputStr)
        } else {
            inputStr
        }
    }

    private fun extractHtmlFromMicrosoftHtml(inputStr: String): String {
        val start = inputStr.indexOfFirst { it == '<' }
        val html =
            if (start != -1) {
                inputStr.substring(start)
            } else {
                inputStr
            }
        return htmlUtils.ensureHtmlCharsetUtf8(html)
    }

    override fun normalizeHtml(
        html: String,
        source: String?,
    ): String =
        source?.let {
            if (officeHtmlPlugin.match(source)) {
                officeHtmlPlugin.officeNormalizationHTML(html)
            } else {
                html
            }
        } ?: html
}
