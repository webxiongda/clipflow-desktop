package com.clipflow.ui.paste.side.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.clipflow.ui.theme.AppUIColors
import com.clipflow.ui.theme.AppUISize.large2X
import com.clipflow.ui.theme.AppUISize.medium
import com.clipflow.ui.theme.AppUISize.tiny
import com.clipflow.ui.theme.AppUISize.tiny4XRoundedCornerShape
import com.clipflow.ui.theme.DesktopAppUIFont.keyboardCharTextStyle

@Composable
fun Top9IndexView(index: Int) {
    Row(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(start = medium, bottom = tiny),
        verticalAlignment = Alignment.Bottom,
    ) {
        Box(
            modifier =
                Modifier
                    .size(large2X, medium)
                    .clip(tiny4XRoundedCornerShape)
                    .background(AppUIColors.importantColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "#${index + 1}",
                style = keyboardCharTextStyle,
                color = MaterialTheme.colorScheme.contentColorFor(AppUIColors.importantColor),
            )
        }
    }
}
