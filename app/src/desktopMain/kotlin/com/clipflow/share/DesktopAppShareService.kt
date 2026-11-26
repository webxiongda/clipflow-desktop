package com.clipflow.share

import com.clipflow.i18n.GlobalCopywriter
import com.clipflow.notification.NotificationManager
import com.clipflow.paste.PasteboardService
import com.clipflow.ui.base.UISupport

class DesktopAppShareService(
    private val copywriter: GlobalCopywriter,
    notificationManager: NotificationManager,
    pasteboardService: PasteboardService,
    private val uiSupport: UISupport,
) : AppShareService {
    override val appSharePlatformList: List<AppSharePlatform> =
        listOf(
            X(uiSupport),
            Weibo(uiSupport),
            Facebook(notificationManager, pasteboardService, uiSupport),
            Reddit(uiSupport),
            LinkedIn(notificationManager, pasteboardService, uiSupport),
            Telegram(uiSupport),
            Mail(notificationManager, pasteboardService, uiSupport),
            Clipboard(notificationManager, pasteboardService),
        )
    override val shareContentKey: String = "share_content"

    override val shareTitleKey: String = "share_title"

    override fun getShareText(): String =
        "${copywriter.getText(shareContentKey)}\n${uiSupport.getClipFlowWebUrl("download")}"

    override fun getShareTitle(): String = copywriter.getText(shareTitleKey)

    override fun getShareContent(): String = copywriter.getText(shareContentKey)

    override fun getShareUrl(): String = uiSupport.getClipFlowWebUrl("download")
}
