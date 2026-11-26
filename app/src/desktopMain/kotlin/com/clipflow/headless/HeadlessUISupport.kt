package com.clipflow.headless

import com.clipflow.app.ClipFlowWebService
import com.clipflow.paste.PasteData
import com.clipflow.ui.base.UISupport
import io.github.oshai.kotlinlogging.KotlinLogging
import okio.Path

class HeadlessUISupport(
    override val clipFlowWebService: ClipFlowWebService,
) : UISupport {

    private val logger = KotlinLogging.logger {}

    override fun openUrlInBrowser(url: String) {
        logger.info { "Headless mode: cannot open URL in browser: $url" }
    }

    override fun getClipFlowWebUrl(path: String): String = clipFlowWebService.getWebUrl("en", path)

    override fun openEmailClient(email: String?) {
        logger.info { "Headless mode: cannot open email client" }
    }

    override fun openHtml(
        id: Long,
        html: String,
    ) {
        logger.info { "Headless mode: cannot open HTML viewer" }
    }

    override fun browseFile(filePath: Path) {
        logger.info { "Headless mode: cannot browse file: $filePath" }
    }

    override fun openColorPicker(pasteData: PasteData) {
        logger.info { "Headless mode: cannot open color picker" }
    }

    override fun openFile(filePath: Path) {
        logger.info { "Headless mode: cannot open file: $filePath" }
    }

    override fun openText(pasteData: PasteData) {
        logger.info { "Headless mode: cannot open text viewer" }
    }

    override fun openRtf(pasteData: PasteData) {
        logger.info { "Headless mode: cannot open RTF viewer" }
    }

    override fun openPasteData(
        pasteData: PasteData,
        index: Int,
    ) {
        logger.info { "Headless mode: cannot open paste data viewer" }
    }

    override fun jumpPrivacyAccessibility() {
        logger.info { "Headless mode: cannot open privacy accessibility settings" }
    }
}
