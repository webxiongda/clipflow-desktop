package com.clipflow.ui.theme

import androidx.compose.runtime.Composable
import com.clipflow.ui.theme.ClipFlowTheme.Theme

@Composable
fun DesktopTheme(content: @Composable () -> Unit) {
    Theme {
        content()
    }
}
