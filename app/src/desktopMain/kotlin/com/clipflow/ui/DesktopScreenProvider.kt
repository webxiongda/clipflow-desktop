package com.clipflow.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.clipflow.platform.Platform
import com.clipflow.sync.NearbyDeviceManager
import com.clipflow.sync.SyncManager
import com.clipflow.ui.base.ShareContentView
import com.clipflow.ui.devices.DesktopPromoteGuide
import com.clipflow.ui.devices.DeviceActions
import com.clipflow.ui.devices.DeviceDetailContentView
import com.clipflow.ui.devices.DeviceScopeFactory
import com.clipflow.ui.devices.DevicesContentView
import com.clipflow.ui.devices.NearbyDeviceDetailContentView
import com.clipflow.ui.devices.PairingCodeContentView
import com.clipflow.ui.devices.SyncScopeFactory
import com.clipflow.ui.devices.TokenView
import com.clipflow.ui.extension.CliToolContentView
import com.clipflow.ui.extension.ExtensionContentView
import com.clipflow.ui.extension.mcp.McpContentView
import com.clipflow.ui.extension.ocr.OCRContentView
import com.clipflow.ui.extension.plugin.PluginsContentView
import com.clipflow.ui.extension.sourcecontrol.SourceControlContentView
import com.clipflow.ui.paste.PasteExportContentView
import com.clipflow.ui.paste.PasteImportContentView
import com.clipflow.ui.settings.AppearanceSettingsContentView
import com.clipflow.ui.settings.DesktopNetworkSettingsContentView
import com.clipflow.ui.settings.DesktopPasteboardSettingsContentView
import com.clipflow.ui.settings.SettingsContentView
import com.clipflow.ui.settings.ShortcutKeysContentView
import com.clipflow.ui.settings.StoragePathManager
import com.clipflow.ui.settings.StorageSettingsContentView
import com.clipflow.ui.theme.AppUIColors
import com.clipflow.ui.theme.AppUISize.medium
import com.clipflow.ui.theme.AppUISize.xxLarge
import com.clipflow.ui.theme.AppUISize.zero
import kotlinx.coroutines.channels.Channel

class DesktopScreenProvider(
    private val platform: Platform,
    private val deviceScopeFactory: DeviceScopeFactory,
    private val syncScopeFactory: SyncScopeFactory,
    private val storagePathManager: StoragePathManager,
    private val syncManager: SyncManager,
    private val nearbyDeviceManager: NearbyDeviceManager,
) : ScreenProvider {

    companion object {
        fun AnimatedContentTransitionScope<NavBackStackEntry>.slideOutRight(): ExitTransition =
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300),
            )

        fun AnimatedContentTransitionScope<NavBackStackEntry>.slideInLeft(): EnterTransition =
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300),
            )
    }

    private val navigationEvents = Channel<NavigationEvent>(Channel.UNLIMITED)

    override fun navigate(route: Route) {
        navigationEvents.trySend(Navigate(route))
    }

    override fun navigateAndClearStack(route: Route) {
        navigationEvents.trySend(NavigateAndClearStack(route))
    }

    override fun navigateUp() {
        navigationEvents.trySend(NavigateUp)
    }

    override fun navigateAction(
        event: NavigationEvent,
        navController: NavHostController,
    ) {
        when (event) {
            is Navigate -> {
                navController.navigate(event.route)
            }
            is NavigateAndClearStack -> {
                navController.navigate(event.route) {
                    popUpTo(0) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            is NavigateUp -> {
                navController.navigateUp()
            }
        }
    }

    @Composable
    override fun screen() {
        val navController = LocalNavHostController.current

        LaunchedEffect(Unit) {
            for (event in navigationEvents) {
                navigateAction(event, navController)
            }
        }

        NavHost(
            navController = navController,
            startDestination = DevicesGraph,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) {
            composable<About> { AboutScreen() }
            composable<ChangeLog> { ChangeLogScreen() }

            navigation<DevicesGraph>(startDestination = Devices) {
                composable<Devices> {
                    DevicesScreen()
                }
                composable<DeviceDetail>(
                    exitTransition = { slideOutRight() },
                    enterTransition = { slideInLeft() },
                ) { backStackEntry ->
                    backStackEntry.DeviceDetailScreen()
                }
                composable<NearbyDeviceDetail>(
                    exitTransition = { slideOutRight() },
                    enterTransition = { slideInLeft() },
                ) { backStackEntry ->
                    backStackEntry.NearbyDeviceDetailScreen()
                }
            }
            composable<Export> { ExportScreen() }
            navigation<ExtensionGraph>(startDestination = Extension) {
                composable<Extension> { ExtensionScreen() }
                composable<CliTool>(
                    exitTransition = { slideOutRight() },
                    enterTransition = { slideInLeft() },
                ) {
                    CliToolScreen()
                }
                composable<MCP>(
                    exitTransition = { slideOutRight() },
                    enterTransition = { slideInLeft() },
                ) {
                    McpSettingsScreen()
                }
                composable<OCR>(
                    exitTransition = { slideOutRight() },
                    enterTransition = { slideInLeft() },
                ) {
                    OCRSettingsScreen()
                }
                composable<Plugins>(
                    exitTransition = { slideOutRight() },
                    enterTransition = { slideInLeft() },
                ) {
                    PluginsScreen()
                }
            }
            composable<Import> { ImportScreen() }
            composable<NotificationCenterRoute> { DesktopScreenLayout { NotificationCenterContentView() } }
            composable<PairingCode> { PairingCodeScreen() }
            composable<Share> { ShareScreen() }
            navigation<SettingsGraph>(startDestination = Settings) {
                composable<Settings> { SettingsScreen() }
                composable<AppearanceSettings>(
                    exitTransition = { slideOutRight() },
                    enterTransition = { slideInLeft() },
                ) {
                    AppearanceSettingsScreen()
                }
                composable<PasteboardSettings>(
                    exitTransition = { slideOutRight() },
                    enterTransition = { slideInLeft() },
                ) {
                    PasteboardSettingsScreen()
                }
                composable<NetworkSettings>(
                    exitTransition = { slideOutRight() },
                    enterTransition = { slideInLeft() },
                ) {
                    NetworkSettingsScreen()
                }
                composable<StorageSettings>(
                    exitTransition = { slideOutRight() },
                    enterTransition = { slideInLeft() },
                ) {
                    StorageSettingsScreen()
                }
                composable<SourceControl>(
                    exitTransition = { slideOutRight() },
                    enterTransition = { slideInLeft() },
                ) {
                    SourceControlSettingsScreen()
                }
            }
            composable<ShortcutKeys> { ShortcutKeysScreen() }
        }
    }

    @Composable
    override fun ScreenLayout(
        horizontal: Dp,
        top: Dp,
        bottom: Dp,
        content: @Composable BoxScope.() -> Unit,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(AppUIColors.contentBackground)
                    .padding(horizontal = horizontal)
                    .padding(top = top, bottom = bottom),
        ) {
            content()
        }
    }

    @Composable
    private fun DesktopScreenLayout(content: @Composable BoxScope.() -> Unit) {
        ScreenLayout(
            horizontal = medium,
            top = zero,
            bottom = medium,
            content = content,
        )
    }

    @Composable
    private fun AboutScreen() {
        DesktopScreenLayout {
            AboutContentView()
        }
    }

    @Composable
    private fun ChangeLogScreen() {
        DesktopScreenLayout {
            ChangeLogContentView()
        }
    }

    @Composable
    private fun NavBackStackEntry.DeviceDetailScreen() {
        val deviceDetail = toRoute<DeviceDetail>()

        // Observe the shared list rather than the handler's own flow: the handler
        // is dropped when the device is removed (from this page or by the peer),
        // and only the list observably goes to null so we can leave the page.
        val syncRuntimeInfos by syncManager.realTimeSyncRuntimeInfos.collectAsState()

        val syncRuntimeInfo =
            syncRuntimeInfos.find {
                it.appInstanceId == deviceDetail.appInstanceId
            }

        LaunchedEffect(syncRuntimeInfo) {
            if (syncRuntimeInfo == null) {
                navigateAndClearStack(Devices)
            }
        }

        syncRuntimeInfo?.let { currentSyncRuntimeInfo ->
            val scope =
                remember(currentSyncRuntimeInfo) {
                    deviceScopeFactory.createDeviceScope(currentSyncRuntimeInfo)
                }
            DesktopScreenLayout {
                scope.DeviceDetailContentView(headerActions = { DeviceActions(it) })
            }
        }
    }

    @Composable
    private fun DevicesScreen() {
        DevicesContentView(
            guideContent = { DesktopPromoteGuide() },
        )
    }

    @Composable
    private fun ExportScreen() {
        DesktopScreenLayout {
            CompositionLocalProvider(LocalSmallSettingItemState provides true) {
                PasteExportContentView()
            }
        }
    }

    @Composable
    private fun ExtensionScreen() {
        DesktopScreenLayout {
            ExtensionContentView()
        }
    }

    @Composable
    private fun CliToolScreen() {
        DesktopScreenLayout {
            CliToolContentView()
        }
    }

    @Composable
    private fun McpSettingsScreen() {
        DesktopScreenLayout {
            McpContentView()
        }
    }

    @Composable
    private fun OCRSettingsScreen() {
        DesktopScreenLayout {
            OCRContentView()
        }
    }

    @Composable
    private fun PluginsScreen() {
        DesktopScreenLayout {
            PluginsContentView()
        }
    }

    @Composable
    private fun SourceControlSettingsScreen() {
        DesktopScreenLayout {
            SourceControlContentView()
        }
    }

    @Composable
    private fun ImportScreen() {
        DesktopScreenLayout {
            CompositionLocalProvider(LocalSmallSettingItemState provides true) {
                PasteImportContentView()
            }
        }
    }

    @Composable
    private fun NavBackStackEntry.NearbyDeviceDetailScreen() {
        val nearbyDeviceDetail = toRoute<NearbyDeviceDetail>()

        val nearbySyncInfos by nearbyDeviceManager.nearbySyncInfos.collectAsState()

        val nearbyDeviceInfo =
            nearbySyncInfos.find {
                it.appInfo.appInstanceId == nearbyDeviceDetail.appInstanceId
            }

        LaunchedEffect(nearbyDeviceInfo) {
            if (nearbyDeviceInfo == null) {
                navigateAndClearStack(Devices)
            }
        }

        nearbyDeviceInfo?.let {
            val scope =
                remember(it) {
                    syncScopeFactory.createSyncScope(it)
                }
            DesktopScreenLayout {
                scope.NearbyDeviceDetailContentView()
            }
        }
    }

    @Composable
    private fun PairingCodeScreen() {
        DesktopScreenLayout {
            PairingCodeContentView()
        }
    }

    @Composable
    private fun ShortcutKeysScreen() {
        DesktopScreenLayout {
            CompositionLocalProvider(LocalSmallSettingItemState provides true) {
                ShortcutKeysContentView()
            }
        }
    }

    @Composable
    private fun SettingsScreen() {
        DesktopScreenLayout {
            SettingsContentView()
        }
    }

    @Composable
    private fun AppearanceSettingsScreen() {
        DesktopScreenLayout {
            AppearanceSettingsContentView()
        }
    }

    @Composable
    private fun PasteboardSettingsScreen() {
        DesktopScreenLayout {
            CompositionLocalProvider(LocalSmallSettingItemState provides true) {
                DesktopPasteboardSettingsContentView(platform)
            }
        }
    }

    @Composable
    private fun NetworkSettingsScreen() {
        DesktopScreenLayout {
            CompositionLocalProvider(LocalSmallSettingItemState provides true) {
                DesktopNetworkSettingsContentView()
            }
        }
    }

    @Composable
    private fun StorageSettingsScreen() {
        DesktopScreenLayout {
            CompositionLocalProvider(LocalSmallSettingItemState provides true) {
                StorageSettingsContentView(storagePathManager)
            }
        }
    }

    @Composable
    private fun ShareScreen() {
        ScreenLayout(
            horizontal = xxLarge,
            top = xxLarge,
            bottom = xxLarge,
        ) {
            ShareContentView()
        }
    }

    @Composable
    fun TokenView() {
        TokenView(IntOffset(0, 0))
    }

    @Composable
    fun DragTargetView() {
        DragTargetContentView()
    }
}
