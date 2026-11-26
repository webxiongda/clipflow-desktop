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
import com.clipflow.ui.base.UISupport
import com.clipflow.ui.base.mail
import com.clipflow.ui.theme.AppUISize.huge
import com.clipflow.ui.theme.AppUISize.mediumRoundedCornerShape
import com.clipflow.ui.theme.AppUISize.xxLarge
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class Mail(
    private val notificationManager: NotificationManager,
    private val pasteboardService: PasteboardService,
    private val uiSupport: UISupport,
) : AppSharePlatform {
    override val platformName: String = "Mail"

    @Composable
    override fun ButtonPlatform() {
        Box(
            modifier =
                Modifier
                    .size(huge)
                    .background(
                        Color(0xFFE8F0FE),
                        mediumRoundedCornerShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = mail(),
                contentDescription = "mail",
                modifier = Modifier.size(xxLarge),
                tint = Color(0xFF4285F4),
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
        delay(2000.milliseconds)
        uiSupport.openEmailClient(null)
    }
}
