package com.clipflow.ui.base

import com.clipflow.app.ClipFlowWebService
import com.clipflow.paste.PasteData
import okio.Path

interface UISupport {

    val clipFlowWebService: ClipFlowWebService

    /**
     * Must never throw: the URL may come from pasteboard data (malformed,
     * whitespace-padded) or the device may have no handler for it.
     * Implementations normalize the URL, catch open failures, and notify
     * the user instead of propagating.
     */
    fun openUrlInBrowser(url: String)

    fun getClipFlowWebUrl(path: String = ""): String

    fun openClipFlowWebInBrowser(path: String = "") {
        openUrlInBrowser(getClipFlowWebUrl(path))
    }

    fun openEmailClient(email: String?)

    fun openHtml(
        id: Long,
        html: String,
    )

    fun browseFile(filePath: Path)

    fun openColorPicker(pasteData: PasteData)

    fun openFile(filePath: Path)

    fun openText(pasteData: PasteData)

    fun openRtf(pasteData: PasteData)

    fun openPasteData(
        pasteData: PasteData,
        index: Int = 0,
    )

    fun jumpPrivacyAccessibility()
}
