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
import com.clipflow.ui.theme.AppUISize.huge
import com.clipflow.ui.theme.AppUISize.mediumRoundedCornerShape
import com.clipflow.ui.theme.AppUISize.xxLarge
import com.clipflow.utils.ioDispatcher
import com.composables.icons.fontawesome.FontAwesome
import com.composables.icons.fontawesome.brands.Linkedin
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import kotlin.time.Duration.Companion.milliseconds

class LinkedIn(
    private val notificationManager: NotificationManager,
    private val pasteboardService: PasteboardService,
    private val uiSupport: UISupport,
) : AppSharePlatform {
    override val platformName: String = "LinkedIn"

    @Composable
    override fun ButtonPlatform() {
        Box(
            modifier =
                Modifier
                    .size(huge)
                    .background(
                        Color(0xFFF5F5F5),
                        mediumRoundedCornerShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = FontAwesome.Brands.Linkedin,
                contentDescription = "LinkedIn",
                modifier = Modifier.size(xxLarge),
                tint = Color(0xFF0077B5),
            )
        }
    }

    override suspend fun action(appShareService: AppShareService) {
        pasteboardService.tryWritePasteboard(
            pasteItem = createTextPasteItem(text = appShareService.getShareText()),
            localOnly = true,
        )
        var url = "https://www.linkedin.com/sharing/share-offsite/?mini=true"

        val appUrl = appShareService.getShareUrl()
        val encodedUrl =
            withContext(ioDispatcher) {
                URLEncoder.encode(appUrl, "UTF-8")
            }
        url += "&url=$encodedUrl"

        notificationManager.sendNotification(
            title = { it.getText("copy_successful") },
            messageType = MessageType.Success,
        )
        delay(2000.milliseconds)
        uiSupport.openUrlInBrowser(url)
    }
}
