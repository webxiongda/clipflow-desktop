package com.clipflow.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.clipflow.ui.base.UISupport
import com.clipflow.ui.theme.AppUISize.huge
import com.clipflow.ui.theme.AppUISize.mediumRoundedCornerShape
import com.clipflow.ui.theme.AppUISize.xxLarge
import com.clipflow.utils.ioDispatcher
import com.composables.icons.fontawesome.FontAwesome
import com.composables.icons.fontawesome.brands.Reddit
import kotlinx.coroutines.withContext
import java.net.URLEncoder

class Reddit(
    private val uiSupport: UISupport,
) : AppSharePlatform {
    override val platformName: String = "Reddit"

    @Composable
    override fun ButtonPlatform() {
        Box(
            modifier =
                Modifier
                    .size(huge)
                    .background(
                        Color(0xFFFFE9E0),
                        mediumRoundedCornerShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = FontAwesome.Brands.Reddit,
                contentDescription = "reddit",
                modifier = Modifier.size(xxLarge),
                tint = Color(0xFFFF4500),
            )
        }
    }

    override suspend fun action(appShareService: AppShareService) {
        val appUrl = appShareService.getShareUrl()
        val (encodedTitle, encodedUrl) =
            withContext(ioDispatcher) {
                URLEncoder.encode(appShareService.getShareTitle(), "UTF-8") to
                    URLEncoder.encode(appUrl, "UTF-8")
            }
        val url = "https://www.reddit.com/submit?title=$encodedTitle&url=$encodedUrl"
        uiSupport.openUrlInBrowser(url)
    }
}
