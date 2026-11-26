package com.clipflow.paste.plugin.type

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.clipflow.paste.PasteCollector
import com.clipflow.paste.PasteDataFlavor
import com.clipflow.paste.PasteTransferable
import com.clipflow.paste.PasteType
import com.clipflow.paste.PasteWriteScope
import com.clipflow.paste.item.CreatePasteItemHelper.createRtfPasteItem
import com.clipflow.paste.item.PasteItem
import com.clipflow.paste.item.PasteItem.Companion.updateExtraInfo
import com.clipflow.paste.item.PasteItemProperties.BACKGROUND
import com.clipflow.paste.item.RtfPasteItem
import com.clipflow.paste.toPasteDataFlavor
import com.clipflow.utils.HtmlColorUtils
import com.clipflow.utils.getRtfUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.put
import java.awt.datatransfer.DataFlavor
import java.io.InputStream

class DesktopRtfTypePlugin : RtfTypePlugin {

    private val logger = KotlinLogging.logger {}

    companion object {
        const val RTF_ID = "text/rtf"

        val RTF_DATA_FLAVOR =
            DataFlavor(
                "text/rtf;  class=java.io.InputStream",
                "Rich Text Format",
            )

        private val rtfUtils = getRtfUtils()
    }

    override fun getPasteType(): PasteType = PasteType.RTF_TYPE

    override fun getIdentifiers(): List<String> = listOf(RTF_ID)

    override fun createPrePasteItem(
        itemIndex: Int,
        identifier: String,
        pasteTransferable: PasteTransferable,
        pasteCollector: PasteCollector,
    ) {
        createRtfPasteItem(
            identifiers = listOf(identifier),
            rtf = "",
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
        if (transferData is InputStream) {
            val rtfBytes = transferData.use { it.readBytes() }
            val rtf = rtfBytes.toString(Charsets.UTF_8)
            val html =
                rtfUtils.rtfToHtml(rtf) ?: run {
                    logger.warn { "Failed to convert RTF to HTML for pasteId=$pasteId itemIndex=$itemIndex" }
                    return
                }
            val background = HtmlColorUtils.getBackgroundColor(html) ?: Color.Transparent
            val update: (PasteItem) -> PasteItem = { pasteItem ->
                createRtfPasteItem(
                    identifiers = pasteItem.identifiers,
                    rtf = rtf,
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
        pasteItem as RtfPasteItem
        val currentRtf = pasteItem.rtf
        map[RTF_DATA_FLAVOR.toPasteDataFlavor()] = currentRtf.byteInputStream()
    }
}
