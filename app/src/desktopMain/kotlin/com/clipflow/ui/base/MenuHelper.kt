package com.clipflow.ui.base

import com.clipflow.app.AppUpdateService
import com.clipflow.app.DesktopAppWindowManager
import com.clipflow.app.WindowTrigger
import com.clipflow.app.WindowsZipUpdater
import com.clipflow.i18n.Copywriter
import com.clipflow.i18n.GlobalCopywriter
import com.clipflow.ui.About
import com.clipflow.ui.ChangeLog
import com.clipflow.ui.Devices
import com.clipflow.ui.Export
import com.clipflow.ui.Extension
import com.clipflow.ui.Import
import com.clipflow.ui.NavigationManager
import com.clipflow.ui.Route
import com.clipflow.ui.Settings
import com.clipflow.ui.ShortcutKeys
import com.clipflow.utils.GlobalCoroutineScope.mainCoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.launch

class MenuHelper(
    private val appUpdateService: AppUpdateService,
    private val appWindowManager: DesktopAppWindowManager,
    private val copywriter: GlobalCopywriter,
    private val navigationManager: NavigationManager,
    private val windowsZipUpdater: WindowsZipUpdater,
    uiSupport: UISupport,
) {

    /**
     * Start the Windows portable-zip download and open the changelog screen so the user
     * sees download/verify progress and the restart prompt. Shared by the update dialog,
     * the tray "update" item, and the search-window hint button.
     */
    fun triggerPortableUpdate() {
        windowsZipUpdater.startDownload()
        mainCoroutineDispatcher.launch(CoroutineName("Open changelog for update")) {
            navigationManager.navigateAndClearStack(ChangeLog)
            appWindowManager.showMainWindow(WindowTrigger.MENU)
        }
    }

    val about =
        MenuItem(
            title = { copywriter -> copywriter.getText("about") },
            action = {
                mainCoroutineDispatcher.launch(CoroutineName("Open about")) {
                    trayMenuAction(About)
                }
            },
        )

    val checkUpdate =
        MenuItem(
            title = { copywriter -> copywriter.getText("check_for_updates") },
            action = {
                appUpdateService.tryTriggerUpdate()
            },
        )

    val devices =
        MenuItem(
            title = { copywriter -> copywriter.getText("devices") },
            action = {
                mainCoroutineDispatcher.launch(CoroutineName("Open devices")) {
                    trayMenuAction(Devices)
                }
            },
        )

    val export =
        MenuItem(
            title = { copywriter -> copywriter.getText("export") },
            action = {
                mainCoroutineDispatcher.launch(CoroutineName("Export")) {
                    trayMenuAction(Export)
                }
            },
        )

    val extension =
        MenuItem(
            title = { copywriter -> copywriter.getText("extension") },
            action = {
                mainCoroutineDispatcher.launch(CoroutineName("Extension")) {
                    trayMenuAction(Extension)
                }
            },
        )

    val import =
        MenuItem(
            title = { copywriter -> copywriter.getText("import") },
            action = {
                mainCoroutineDispatcher.launch(CoroutineName("Import")) {
                    trayMenuAction(Import)
                }
            },
        )

    val settings =
        MenuItem(
            title = { copywriter -> copywriter.getText("settings") },
            action = {
                mainCoroutineDispatcher.launch(CoroutineName("Open settings")) {
                    trayMenuAction(Settings)
                }
            },
        )

    val shortcutKeys =
        MenuItem(
            title = { copywriter -> copywriter.getText("shortcut_keys") },
            action = {
                mainCoroutineDispatcher.launch(CoroutineName("Open shortcut keys")) {
                    trayMenuAction(ShortcutKeys)
                }
            },
        )

    val faq =
        MenuItem(
            title = { copywriter -> copywriter.getText("faq") },
            action = {
                mainCoroutineDispatcher.launch(CoroutineName("Open FAQ")) {
                    uiSupport.openClipFlowWebInBrowser("FAQ")
                }
            },
        )

    val menuItems =
        listOf(
            devices,
            settings,
            extension,
            shortcutKeys,
            about,
            checkUpdate,
            faq,
        )

    private fun trayMenuAction(route: Route) {
        navigationManager.navigateAndClearStack(route)
        appWindowManager.showMainWindow(WindowTrigger.MENU)
    }
}

data class MenuItem(
    val title: (Copywriter) -> String,
    val action: () -> Unit,
)
