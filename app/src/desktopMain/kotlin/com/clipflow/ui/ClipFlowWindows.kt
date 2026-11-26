package com.clipflow.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.window.ApplicationScope
import com.clipflow.app.AppLaunchState
import com.clipflow.app.DesktopAppLaunch
import com.clipflow.app.DesktopAppWindowManager
import com.clipflow.app.WindowTrigger
import com.clipflow.app.generated.resources.Res
import com.clipflow.app.generated.resources.clipflow
import com.clipflow.app.generated.resources.clipflow_mac
import com.clipflow.config.DesktopConfigManager
import com.clipflow.platform.Platform
import com.clipflow.ui.tray.TrayView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

internal fun handleFirstLaunch(
    firstLaunch: Boolean,
    firstLaunchCompleted: Boolean,
    showMainWindow: () -> Unit,
    markFirstLaunchCompleted: () -> Unit,
) {
    if (firstLaunch && !firstLaunchCompleted) {
        showMainWindow()
        markFirstLaunchCompleted()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun ApplicationScope.ClipFlowWindows(exiting: Boolean) {
    val platform = koinInject<Platform>()
    val isMacos = remember { platform.isMacos() }
    val isWindows = remember { platform.isWindows() }
    val isLinux = remember { platform.isLinux() }

    val windowIcon: Painter? =
        if (isMacos) {
            painterResource(Res.drawable.clipflow_mac)
        } else if (isWindows || isLinux) {
            painterResource(Res.drawable.clipflow)
        } else {
            null
        }

    if (!exiting && isMacos) {
        MacApplicationMenu()
    }

    if (!exiting && windowIcon != null) {
        TrayView(windowIcon)
    }

    val appLaunch = koinInject<DesktopAppLaunch>()
    val appLaunchState = koinInject<AppLaunchState>()
    val appWindowManager = koinInject<DesktopAppWindowManager>()
    val configManager = koinInject<DesktopConfigManager>()

    LaunchedEffect(Unit) {
        configManager.config
            .map { it.showPastePanelButton }
            .distinctUntilChanged()
            .collect { shown ->
                if (shown) {
                    appWindowManager.showPastePanelButton()
                } else {
                    appWindowManager.hidePastePanelButton()
                }
            }
    }

    LaunchedEffect(Unit) {
        configManager.config
            .map { it.showPastePanelButton }
            .distinctUntilChanged()
            .flatMapLatest { enabled ->
                if (enabled) appWindowManager.fullscreenOverPastePanelButton else flowOf(false)
            }.collect { fullscreen ->
                appWindowManager.suppressPastePanelButton(fullscreen)
            }
    }

    LaunchedEffect(Unit) {
        handleFirstLaunch(
            firstLaunch = appLaunchState.firstLaunch,
            firstLaunchCompleted = appLaunch.firstLaunchCompleted.value,
            showMainWindow = {
                appWindowManager.showMainWindow(WindowTrigger.SYSTEM)
            },
            markFirstLaunchCompleted = {
                appLaunch.setFirstLaunchCompleted(true)
            },
        )
    }

    MainWindow(windowIcon)

    SearchWindow(windowIcon)

    PastePanelButtonWindow(windowIcon)

    PastePanelWindow(windowIcon)

    ScreenshotPreviewWindow(windowIcon)

    val bubbleWindowInfo by appWindowManager.bubbleWindowInfo.collectAsState()
    if (bubbleWindowInfo.show) {
        BubbleWindow(windowIcon)
    }
}
