package com.clipflow.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalContextMenuRepresentation
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.LocalTextContextMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import com.clipflow.app.WindowInfo
import com.clipflow.ui.contextmenu.ContextMenuColors
import com.clipflow.ui.contextmenu.ContextMenuMeasurements
import com.clipflow.ui.contextmenu.MaterialContextMenuRepresentation
import com.clipflow.ui.contextmenu.MaterialTextContextMenu
import com.clipflow.ui.theme.AppUISize
import com.clipflow.ui.theme.ClipFlowTheme.Theme

object DesktopContext {

    @OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
    @Composable
    private fun BaseContext(content: @Composable () -> Unit) {
        Theme {
            CompositionLocalProvider(
                LocalContextMenuRepresentation provides
                    MaterialContextMenuRepresentation(
                        measurements =
                            ContextMenuMeasurements(
                                minWidth = AppUISize.giant,
                                maxWidth = AppUISize.gigantic,
                                itemMinHeight = AppUISize.xLarge,
                                itemPadding = PaddingValues(AppUISize.medium, AppUISize.tiny3X),
                                menuTopPadding = AppUISize.tiny3X,
                                menuBottomPadding = AppUISize.tiny3X,
                                iconPadding = AppUISize.tiny,
                                dividerHeight = AppUISize.small2X,
                            ),
                        colors =
                            ContextMenuColors(
                                surface = MaterialTheme.colorScheme.surface,
                                text = MaterialTheme.colorScheme.onSurface,
                                itemHover = MaterialTheme.colorScheme.primary,
                                itemHoverText = MaterialTheme.colorScheme.onPrimary,
                            ),
                    ),
                LocalTextContextMenu provides MaterialTextContextMenu,
            ) {
                content()
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
    @Composable
    fun MainWindowContext(
        windowInfo: WindowInfo,
        content: @Composable () -> Unit,
    ) {
        BaseContext {
            CompositionLocalProvider(
                LocalMainWindowInfoState provides windowInfo,
            ) {
                content()
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
    @Composable
    fun SearchWindowContext(
        windowInfo: WindowInfo,
        content: @Composable () -> Unit,
    ) {
        BaseContext {
            CompositionLocalProvider(
                LocalSearchWindowInfoState provides windowInfo,
            ) {
                content()
            }
        }
    }

    @Composable
    fun PastePanelWindowContext(content: @Composable () -> Unit) {
        BaseContext {
            content()
        }
    }

    @Composable
    fun BubbleWindowContext(content: @Composable () -> Unit) {
        BaseContext {
            content()
        }
    }
}
