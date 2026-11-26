package com.clipflow.paste

import com.clipflow.app.DesktopAppWindowManager
import com.clipflow.config.DesktopConfigManager
import com.clipflow.db.paste.PasteDao
import com.clipflow.paste.PasteType
import com.clipflow.ui.model.ScreenshotPreviewViewModel
import com.clipflow.utils.ioDispatcher
import kotlinx.coroutines.withContext

/**
 * After an image is captured, reloads the screenshot history and pops the preview
 * bubble. Gated by the `showScreenshotPreview` setting so users who find an automatic
 * bubble distracting can turn it off.
 */
class DesktopScreenshotCaptureObserver(
    private val configManager: DesktopConfigManager,
    private val pasteDao: PasteDao,
    private val screenshotPreviewViewModel: ScreenshotPreviewViewModel,
    private val appWindowManager: DesktopAppWindowManager,
) : ScreenshotCaptureObserver {

    override suspend fun onCaptured(pasteId: Long) {
        if (!configManager.config.value.showScreenshotPreview) return

        val isImage =
            withContext(ioDispatcher) {
                pasteDao.getNoDeletePasteData(pasteId)?.getType() == PasteType.IMAGE_TYPE
            }
        if (!isImage) return

        screenshotPreviewViewModel.reload(focusId = pasteId)
        appWindowManager.showScreenshotPreviewWindow()
    }
}
