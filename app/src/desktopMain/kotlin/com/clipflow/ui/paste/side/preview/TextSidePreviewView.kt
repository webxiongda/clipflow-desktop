package com.clipflow.ui.paste.side.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import com.clipflow.i18n.GlobalCopywriter
import com.clipflow.paste.item.PasteText
import com.clipflow.ui.paste.PasteDataScope
import com.clipflow.ui.theme.AppUIFont.pasteTextStyle
import com.clipflow.ui.theme.AppUIFont.previewAutoSize
import com.clipflow.ui.theme.AppUISize.small2X
import org.koin.compose.koinInject

@Composable
fun PasteDataScope.TextSidePreviewView() {
    val pasteText = getPasteItem(PasteText::class)
    val copywriter = koinInject<GlobalCopywriter>()
    SidePasteLayoutView(
        pasteBottomContent = {
            BottomGradient(copywriter.getText("character_count", "${pasteText.text.length}"))
        },
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(small2X)) {
            BasicText(
                modifier = Modifier.fillMaxSize(),
                text = AnnotatedString(pasteText.previewText()),
                maxLines = 9,
                softWrap = true,
                overflow = TextOverflow.Ellipsis,
                style = pasteTextStyle,
                autoSize = previewAutoSize,
            )
        }
    }
}
