package com.clipflow.paste.menu

import com.clipflow.app.AppInfo
import com.clipflow.app.AppName
import com.clipflow.i18n.GlobalCopywriter
import com.clipflow.image.OCRModule
import com.clipflow.paste.PasteData
import com.clipflow.paste.PasteReleaseService
import com.clipflow.paste.PasteType
import com.clipflow.paste.PasteboardService
import com.clipflow.paste.item.CreatePasteItemHelper.createTextPasteItem
import com.clipflow.paste.item.ImagesPasteItem
import com.clipflow.paste.item.getFilePaths
import com.clipflow.path.UserDataPathProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException

class ExtractTextMenuContributor(
    private val appInfo: AppInfo,
    private val copywriter: GlobalCopywriter,
    private val ocrModule: OCRModule,
    private val pasteboardService: PasteboardService,
    private val pasteReleaseService: PasteReleaseService,
    private val userDataPathProvider: UserDataPathProvider,
) : PasteMenuContributor {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun contribute(pasteData: PasteData): List<PasteMenuAction> =
        if (pasteData.getType() == PasteType.IMAGE_TYPE) {
            listOf(PasteMenuAction(copywriter.getText("extract_text")) { extractText(pasteData) })
        } else {
            emptyList()
        }

    private suspend fun extractText(pasteData: PasteData) {
        val extractText =
            pasteData
                .getPasteItem(ImagesPasteItem::class)
                ?.getFilePaths(userDataPathProvider)
                ?.mapNotNull { path ->
                    ocrModule.extractText(path).getOrNull()
                }?.joinToString(separator = "\n")

        if (extractText.isNullOrBlank()) return

        val pasteItem = createTextPasteItem(text = extractText)
        try {
            // Our own pasteboard writes are not captured, so record the paste directly
            val id =
                pasteReleaseService.createLocalPasteData(
                    appInstanceId = appInfo.appInstanceId,
                    pasteItem = pasteItem,
                    source = AppName,
                )
            pasteboardService
                .tryWritePasteboard(
                    id = id,
                    pasteItem = pasteItem,
                    localOnly = true,
                ).onFailure { logger.warn(it) { "Failed to write extracted text to pasteboard" } }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.warn(e) { "Failed to record extracted text paste" }
        }
    }
}
