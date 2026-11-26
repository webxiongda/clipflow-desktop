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
import com.composables.icons.fontawesome.brands.Weibo
import kotlinx.coroutines.withContext
import java.net.URLEncoder

class Weibo(
    private val uiSupport: UISupport,
) : AppSharePlatform {
    override val platformName: String = "Weibo"

    @Composable
    override fun ButtonPlatform() {
        Box(
            modifier =
                Modifier
                    .size(huge)
                    .background(
                        Color(0xFFFDECEE),
                        mediumRoundedCornerShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = FontAwesome.Brands.Weibo,
                contentDescription = "weibo",
                modifier = Modifier.size(xxLarge),
                tint = Color(0xFFE6162D),
            )
        }
    }

    override suspend fun action(appShareService: AppShareService) {
        val encodedText =
            withContext(ioDispatcher) {
                URLEncoder.encode(appShareService.getShareText(), "UTF-8")
            }
        val url = "https://service.weibo.com/share/share.php?title=$encodedText"
        uiSupport.openUrlInBrowser(url)
    }
}
