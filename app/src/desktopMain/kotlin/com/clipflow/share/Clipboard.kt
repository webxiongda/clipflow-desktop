package com.clipflow.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.clipflow.notification.MessageType
import com.clipflow.notification.NotificationManager
import com.clipflow.paste.PasteboardService
import com.clipflow.paste.item.CreatePasteItemHelper.createTextPasteItem
import com.clipflow.ui.theme.AppUISize.huge
import com.clipflow.ui.theme.AppUISize.mediumRoundedCornerShape
import com.clipflow.ui.theme.AppUISize.xxLarge
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Content_paste

class Clipboard(
    private val notificationManager: NotificationManager,
    private val pasteboardService: PasteboardService,
) : AppSharePlatform {
    override val platformName: String = "Clipboard"

    @Composable
    override fun ButtonPlatform() {
        Box(
            modifier =
                Modifier
                    .size(huge)
                    .background(
                        Color(0xFFF1F3F4),
                        mediumRoundedCornerShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = MaterialSymbols.Rounded.Content_paste,
                contentDescription = "clipboard",
                modifier = Modifier.size(xxLarge),
                tint = Color(0xFF5F6368),
            )
        }
    }

    override suspend fun action(appShareService: AppShareService) {
        pasteboardService.tryWritePasteboard(
            pasteItem = createTextPasteItem(text = appShareService.getShareText()),
            localOnly = true,
        )
        notificationManager.sendNotification(
            title = { it.getText("copy_successful") },
            messageType = MessageType.Success,
        )
    }
}
