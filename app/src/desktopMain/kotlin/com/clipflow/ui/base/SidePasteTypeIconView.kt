package com.clipflow.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.clipflow.ui.paste.PasteDataScope

@Composable
fun PasteDataScope.SidePasteTypeIconView(modifier: Modifier = Modifier) {
    pasteData.source?.let {
        SideAppSourceIcon(
            modifier = modifier,
        ) {
            SideDefaultPasteTypeIcon()
        }
    } ?: run {
        SideDefaultPasteTypeIcon()
    }
}
