package com.clipflow.ui.paste.side.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.clipflow.paste.PasteState
import com.clipflow.paste.PasteType
import com.clipflow.paste.item.HtmlPasteItem
import com.clipflow.paste.item.RtfPasteItem
import com.clipflow.ui.paste.PasteDataScope

@Composable
fun PasteDataScope.SidePreviewView(
    showTop9: Boolean,
    index: Int,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (pasteData.pasteState == PasteState.LOADING) {
            PreSidePreviewView()
        } else {
            when (pasteData.getType()) {
                PasteType.TEXT_TYPE -> TextSidePreviewView()
                PasteType.COLOR_TYPE -> ColorSidePreviewView()
                PasteType.URL_TYPE -> UrlSidePreviewView()
                PasteType.HTML_TYPE ->
                    getPasteItem(HtmlPasteItem::class).let {
                        RichTextSidePreviewView(it, it.getBackgroundColor())
                    }
                PasteType.RTF_TYPE ->
                    getPasteItem(RtfPasteItem::class).let {
                        RichTextSidePreviewView(it, it.getBackgroundColor())
                    }
                PasteType.IMAGE_TYPE -> ImageSidePreviewView()
                PasteType.FILE_TYPE -> FilesSidePreviewView()
            }
        }

        if (showTop9) {
            Top9IndexView(index)
        }
    }
}
