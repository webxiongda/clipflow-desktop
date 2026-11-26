package com.clipflow.listener

import com.clipflow.app.AppFileChooser
import com.clipflow.app.DesktopAppWindowManager
import com.clipflow.app.WindowTrigger
import com.clipflow.config.DesktopConfigManager
import com.clipflow.db.paste.PasteDao
import com.clipflow.listener.DesktopShortcutKeys.Companion.HIDE_WINDOW
import com.clipflow.listener.DesktopShortcutKeys.Companion.PASTE_LOCAL_LAST
import com.clipflow.listener.DesktopShortcutKeys.Companion.PASTE_PLAIN_TEXT
import com.clipflow.listener.DesktopShortcutKeys.Companion.PASTE_PRIMARY_TYPE
import com.clipflow.listener.DesktopShortcutKeys.Companion.PASTE_REMOTE_LAST
import com.clipflow.listener.DesktopShortcutKeys.Companion.SHOW_MAIN
import com.clipflow.listener.DesktopShortcutKeys.Companion.SHOW_PASTE_PANEL
import com.clipflow.listener.DesktopShortcutKeys.Companion.SHOW_SCREENSHOT_PREVIEW
import com.clipflow.listener.DesktopShortcutKeys.Companion.SHOW_SEARCH
import com.clipflow.listener.DesktopShortcutKeys.Companion.TOGGLE_ENCRYPT
import com.clipflow.listener.DesktopShortcutKeys.Companion.TOGGLE_PASTEBOARD_MONITORING
import com.clipflow.notification.MessageType
import com.clipflow.notification.NotificationManager
import com.clipflow.paste.CurrentPaste
import com.clipflow.paste.PasteWriteScope
import com.clipflow.paste.PasteboardService
import com.clipflow.paste.item.PasteText
import com.clipflow.ui.model.PasteSelectionViewModel
import com.clipflow.ui.model.ScreenshotPreviewViewModel
import com.clipflow.utils.GlobalCoroutineScope.mainCoroutineDispatcher
import com.clipflow.utils.ioDispatcher
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DesktopShortKeysAction(
    private val appFileChooser: AppFileChooser,
    private val appWindowManager: DesktopAppWindowManager,
    private val lazyShortcutKeysListener: Lazy<DesktopShortcutKeysListener>,
    private val configManager: DesktopConfigManager,
    private val currentPaste: CurrentPaste,
    private val notificationManager: NotificationManager,
    private val pasteboardService: PasteboardService,
    private val pasteDao: PasteDao,
    private val pasteSelectionViewModel: PasteSelectionViewModel,
    private val screenshotPreviewViewModel: ScreenshotPreviewViewModel,
) : ShortcutKeysAction {

    private val logger = KotlinLogging.logger {}

    @Volatile
    override var actioning: Boolean = false

    @Volatile
    override var event: NativeKeyEvent? = null

    override val action: (String, NativeKeyEvent) -> Unit = { actionName, event ->
        this.event = event
        when (actionName) {
            PASTE_PLAIN_TEXT -> pastePlainText()
            PASTE_PRIMARY_TYPE -> pastePrimaryType()
            PASTE_LOCAL_LAST -> pasteLast(true)
            PASTE_REMOTE_LAST -> pasteLast(false)
            SHOW_MAIN -> showMainWindow()
            SHOW_SEARCH -> switchSearchWindow()
            SHOW_PASTE_PANEL -> switchPastePanelButton()
            SHOW_SCREENSHOT_PREVIEW -> showScreenshotPreview()
            HIDE_WINDOW -> hideWindow()
            TOGGLE_PASTEBOARD_MONITORING -> togglePasteboardMonitoring()
            TOGGLE_ENCRYPT -> toggleEncrypt()
        }
    }

    private fun mainRunAction(
        actionName: String,
        actionLogMessage: String,
        action: suspend () -> Unit,
    ) {
        mainCoroutineDispatcher.launch {
            runCatching {
                actioning = true
                logger.info { actionLogMessage }
                action()
            }.onFailure { e ->
                logger.error(e) { "Failed to run shortcut key action $actionName" }
            }.also {
                actioning = false
            }
        }
    }

    private fun showMainWindow() {
        mainRunAction(
            actionName = "OpenMainWindow",
            actionLogMessage = "Open main window",
        ) {
            appWindowManager.saveCurrentActiveAppInfo()
            appWindowManager.showMainWindow(WindowTrigger.SHORTCUT)
        }
    }

    private suspend fun awaitShortcutReleased() {
        lazyShortcutKeysListener.value.awaitKeysReleased()
    }

    private fun switchSearchWindow() {
        mainRunAction(
            actionName = "OpenSearchWindow",
            actionLogMessage = "Open search window",
        ) {
            appWindowManager.switchSearchWindow(
                windowTrigger = WindowTrigger.SHORTCUT,
                awaitTriggerReleased = ::awaitShortcutReleased,
            ) {
                appWindowManager.saveCurrentActiveAppInfo()
            }
        }
    }

    private fun switchPastePanelButton() {
        mainRunAction(
            actionName = "SwitchPastePanelButton",
            actionLogMessage = "Switch paste panel button",
        ) {
            // A button hidden for a full-screen app comes back instead of the setting
            // being switched off unseen
            if (appWindowManager.revealPastePanelButton()) return@mainRunAction
            // The button window follows this setting, see ClipFlowWindows
            configManager.updateConfig(
                "showPastePanelButton",
                !configManager.config.value.showPastePanelButton,
            )
        }
    }

    private fun showScreenshotPreview() {
        mainRunAction(
            actionName = "ShowScreenshotPreview",
            actionLogMessage = "Show screenshot preview",
        ) {
            screenshotPreviewViewModel.reload()
            appWindowManager.showScreenshotPreviewWindow(WindowTrigger.SHORTCUT)
        }
    }

    private fun hideWindow() {
        mainRunAction(
            actionName = "HideWindow",
            actionLogMessage = "Hide window",
        ) {
            // Like any popup, the open type menu goes first and the search window stays. This
            // shortcut is the menu's only Esc owner: its popup takes no focus, and handling Esc in
            // Compose as well would close the menu and then, seeing it closed, hide the window.
            // Returning early also skips hiding the main window, on purpose: one Esc, one layer.
            if (appWindowManager.getCurrentSearchWindowInfo().show &&
                pasteSelectionViewModel.typeMenuIndex.value != null
            ) {
                pasteSelectionViewModel.cancelTypeMenu()
                return@mainRunAction
            }

            if (appWindowManager.getCurrentMainWindowInfo().show &&
                !appFileChooser.showFileDialog.value
            ) {
                appWindowManager.hideMainWindow()
            }

            if (appWindowManager.getCurrentSearchWindowInfo().show) {
                appWindowManager.dismissSearchWindow(::awaitShortcutReleased)
            }
        }
    }

    private fun pastePlainText() {
        mainRunAction(
            actionName = "PastePlainText",
            actionLogMessage = "Paste plain text",
        ) {
            currentPaste.getCurrentPaste()?.let { pasteData ->
                withContext(ioDispatcher) {
                    pasteData.getPasteAppearItems().firstOrNull { it is PasteText }?.let {
                        handleCopyResult(
                            result =
                                pasteboardService.tryWritePasteboard(
                                    id = pasteData.id,
                                    pasteItem = it,
                                    localOnly = true,
                                    updateCreateTime = true,
                                ),
                        )
                    }
                }
            }
        }
    }

    private fun pastePrimaryType() {
        mainRunAction(
            actionName = "PastePrimaryType",
            actionLogMessage = "Paste primary type",
        ) {
            currentPaste.getCurrentPaste()?.let {
                withContext(ioDispatcher) {
                    handleCopyResult(
                        result =
                            pasteboardService.tryWritePasteboard(
                                pasteData = it,
                                localOnly = true,
                                scope = PasteWriteScope.PRIMARY_CATEGORY,
                                updateCreateTime = true,
                            ),
                    )
                }
            }
        }
    }

    private fun pasteLast(local: Boolean) {
        mainRunAction(
            actionName = "PasteLast",
            actionLogMessage = "Paste ${if (local) "Local" else "Remote"} Last",
        ) {
            val result =
                pasteDao.searchPasteData(
                    searchTerms = listOf(),
                    local = local,
                    limit = 1,
                )

            if (result.isNotEmpty()) {
                withContext(ioDispatcher) {
                    handleCopyResult(
                        result =
                            pasteboardService.tryWritePasteboard(
                                pasteData = result[0],
                                localOnly = true,
                                updateCreateTime = true,
                            ),
                    )
                }
            }
        }
    }

    private suspend fun handleCopyResult(result: Result<Unit?>) {
        result
            .onSuccess {
                // The action fires on key press; injecting while the user still holds the
                // shortcut's modifiers would merge them into the paste (Ctrl+Shift+V).
                awaitShortcutReleased()
                appWindowManager.toPaste()
            }.onFailure {
                notificationManager.sendNotification(
                    title = { copyWriter -> copyWriter.getText("copy_failed") },
                    messageType = MessageType.Error,
                )
            }
    }

    private fun togglePasteboardMonitoring() {
        mainRunAction(
            actionName = "TogglePasteboardMonitoring",
            actionLogMessage = "Toggle Pasteboard Monitoring",
        ) {
            pasteboardService.toggle()
        }
    }

    private fun toggleEncrypt() {
        mainRunAction(
            actionName = "ToggleEncrypt",
            actionLogMessage = "Toggle Encrypt",
        ) {
            configManager.updateConfig(
                "isEncryptSync",
                !configManager.getCurrentConfig().enableEncryptSync,
            )
        }
    }
}
