package com.clipflow.paste

import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.clipflow.app.AppControl
import com.clipflow.app.AppInfo
import com.clipflow.app.AppWindowManager
import com.clipflow.app.DesktopAppWindowManager
import com.clipflow.app.WindowTrigger
import com.clipflow.db.paste.PasteDao
import com.clipflow.db.paste.PasteTagDao
import com.clipflow.db.sync.SyncState
import com.clipflow.i18n.GlobalCopywriter
import com.clipflow.notification.MessageType
import com.clipflow.notification.NotificationManager
import com.clipflow.paste.item.PasteFiles
import com.clipflow.paste.menu.PasteMenuAction
import com.clipflow.paste.menu.PasteMenuContributor
import com.clipflow.sync.SyncManager
import com.clipflow.task.TaskSubmitter
import com.clipflow.ui.base.UISupport
import com.clipflow.ui.contextmenu.ContextMenuDivider
import com.clipflow.ui.contextmenu.ContextMenuGroup
import com.clipflow.ui.contextmenu.MaterialContextMenuItem
import com.clipflow.ui.model.PasteSearchViewModel
import com.clipflow.ui.theme.AppUIColors
import com.clipflow.ui.theme.AppUISize.small
import com.clipflow.utils.ioDispatcher
import com.clipflow.utils.namedScope
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Check
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DesktopPasteMenuService(
    private val appControl: AppControl,
    private val appInfo: AppInfo,
    appWindowManager: AppWindowManager,
    private val copywriter: GlobalCopywriter,
    private val notificationManager: NotificationManager,
    private val pasteboardService: PasteboardService,
    private val pasteDao: PasteDao,
    private val pasteTagDao: PasteTagDao,
    private val pasteSearchViewModel: PasteSearchViewModel,
    private val syncManager: SyncManager,
    private val taskSubmitter: TaskSubmitter,
    private val uiSupport: UISupport,
    private val contributors: List<PasteMenuContributor>,
) : PasteMenuService {

    companion object {
        private const val CUT_UNDO_DELAY_MS = 60_000L
        private val logger = KotlinLogging.logger {}
    }

    private val desktopAppWindowManager = appWindowManager as DesktopAppWindowManager

    private val menuScope = namedScope(ioDispatcher, "DesktopPasteMenuService")

    private val _pendingCutPasteId = MutableStateFlow<Long?>(null)
    val pendingCutPasteId: StateFlow<Long?> = _pendingCutPasteId.asStateFlow()
    private var pendingCutJob: Job? = null

    override fun copyPasteData(pasteData: PasteData) {
        menuScope.launch {
            pasteboardService
                .tryWritePasteboard(
                    pasteData = pasteData,
                    localOnly = true,
                ).onSuccess {
                    notificationManager.sendNotification(
                        title = { it.getText("copy_successful") },
                        messageType = MessageType.Success,
                    )
                }.onFailure {
                    notificationManager.sendNotification(
                        title = { it.getText("copy_failed") },
                        messageType = MessageType.Error,
                    )
                }
        }
    }

    private fun cutPasteData(pasteData: PasteData) {
        menuScope.launch {
            pasteboardService
                .tryWritePasteboard(
                    pasteData = pasteData,
                    localOnly = true,
                ).onSuccess {
                    pendingCutJob?.cancel()
                    _pendingCutPasteId.value = null

                    pendingCutJob =
                        menuScope.launch {
                            pasteDao.cutPasteData(pasteData.id, CUT_UNDO_DELAY_MS)
                            _pendingCutPasteId.value = pasteData.id
                            delay(CUT_UNDO_DELAY_MS)
                            _pendingCutPasteId.value = null
                            pendingCutJob = null
                        }
                    notificationManager.sendNotification(
                        title = { it.getText("copy_successful") },
                        messageType = MessageType.Success,
                    )
                }.onFailure {
                    notificationManager.sendNotification(
                        title = { it.getText("copy_failed") },
                        messageType = MessageType.Error,
                    )
                }
        }
    }

    fun cancelCut() {
        menuScope.launch {
            pendingCutJob?.cancel()
            pendingCutJob = null
            _pendingCutPasteId.value?.let { id ->
                pasteDao.updatePasteState(id, PasteState.LOADED)
            }
            _pendingCutPasteId.value = null
        }
    }

    override fun openPasteData(
        pasteData: PasteData,
        index: Int,
    ) {
        menuScope.launch {
            uiSupport.openPasteData(pasteData, index)
            val pasteType = pasteData.getType()
            if (pasteType.isText() || pasteType.isHtml()) {
                if (!desktopAppWindowManager.getCurrentSearchWindowInfo().show) {
                    desktopAppWindowManager.showMainWindow(WindowTrigger.SYSTEM)
                }
            } else if (pasteType.isColor()) {
                desktopAppWindowManager.showMainWindow(WindowTrigger.SYSTEM)
            } else {
                desktopAppWindowManager.hideMainWindow()
            }
        }
    }

    override fun deletePasteData(pasteData: PasteData) {
        menuScope.launch {
            pasteDao.markDeletePasteData(pasteData.id)
        }
    }

    override fun quickPasteFromMainWindow(pasteData: PasteData) {
        menuScope.launch {
            desktopAppWindowManager.hideMainWindowAndPaste {
                pasteboardService
                    .tryWritePasteboard(
                        pasteData = pasteData,
                        localOnly = true,
                        updateCreateTime = true,
                    ).isSuccess
            }
        }
    }

    override fun quickPasteFromSearchWindow(pasteData: PasteData) {
        menuScope.launch {
            desktopAppWindowManager.hideSearchWindowAndPaste(0) {
                pasteboardService
                    .tryWritePasteboard(
                        pasteData = pasteData,
                        localOnly = true,
                        updateCreateTime = true,
                    ).isSuccess
            }
        }
    }

    private fun createCopyContextMenuItem(pasteData: PasteData): ContextMenuItem =
        ContextMenuItem(copywriter.getText("copy")) {
            copyPasteData(pasteData)
        }

    private fun createCutContextMenuItem(pasteData: PasteData): ContextMenuItem =
        ContextMenuItem(copywriter.getText("cut")) {
            cutPasteData(pasteData)
        }

    private fun createDeleteContextMenuItem(pasteData: PasteData): ContextMenuItem =
        ContextMenuItem(copywriter.getText("delete")) {
            deletePasteData(pasteData)
        }

    private fun createEditContextMenuItem(pasteData: PasteData): ContextMenuItem =
        ContextMenuItem(copywriter.getText("edit")) {
            openPasteData(pasteData)
        }

    private fun createPinTagMenuItem(pasteData: PasteData): ContextMenuItem =
        ContextMenuGroup(copywriter.getText("tags")) {
            val tagList = pasteSearchViewModel.tagList.value
            if (tagList.isEmpty()) {
                listOf(
                    ContextMenuItem(copywriter.getText("no_tags")) {
                    },
                )
            } else {
                val tagIdList = pasteTagDao.getPasteTagsBlock(pasteData.id)

                tagList.map { tag ->
                    MaterialContextMenuItem(
                        label = tag.name,
                        onClick = {
                            if (appControl.isPinTagEnabled()) {
                                pasteTagDao.switchPinPasteTagBlock(pasteData.id, tag.id)
                            }
                        },
                        leadingIcon = {
                            Box(
                                modifier =
                                    Modifier
                                        .size(small)
                                        .background(color = Color(tag.color.toInt()), shape = CircleShape),
                            )
                        },
                        trailingIcon = {
                            if (tagIdList.contains(tag.id)) {
                                Icon(
                                    imageVector = MaterialSymbols.Rounded.Check,
                                    contentDescription = "Checked",
                                    tint = AppUIColors.importantColor,
                                )
                            }
                        },
                    )
                }
            }
        }

    private fun createSyncToMenuItem(pasteData: PasteData): ContextMenuItem =
        ContextMenuGroup(copywriter.getText("sync_to")) {
            val connectedDevices =
                syncManager.realTimeSyncRuntimeInfos.value.filter {
                    it.connectState == SyncState.CONNECTED
                }
            if (connectedDevices.isEmpty()) {
                listOf(
                    ContextMenuItem(copywriter.getText("no_connected_devices")) {
                    },
                )
            } else {
                connectedDevices.map { syncRuntimeInfo ->
                    ContextMenuItem(syncRuntimeInfo.getDeviceDisplayName()) {
                        menuScope.launch {
                            taskSubmitter.submit {
                                addSyncTask(
                                    id = pasteData.id,
                                    fileSize = pasteData.size,
                                    appInstanceId = appInfo.appInstanceId,
                                    targetAppInstanceIds = setOf(syncRuntimeInfo.appInstanceId),
                                )
                            }
                        }
                    }
                }
            }
        }

    private fun createOpenContextMenuItem(pasteData: PasteData): ContextMenuItem =
        ContextMenuItem(copywriter.getText("open")) {
            openPasteData(pasteData)
        }

    private fun createLoadingMenuItems(pasteData: PasteData): List<ContextMenuItem> =
        listOf(
            createCopyContextMenuItem(pasteData),
            ContextMenuDivider,
            createDeleteContextMenuItem(pasteData),
        )

    private fun MutableList<ContextMenuItem>.addCutIfOwnedFiles(pasteData: PasteData) {
        val pasteFiles = pasteData.getPasteItem(PasteFiles::class)
        if (pasteFiles != null && !pasteFiles.isRefFiles()) {
            add(createCutContextMenuItem(pasteData))
        }
    }

    private fun createPrimaryMenuItems(pasteData: PasteData): List<ContextMenuItem>? =
        when (pasteData.getType()) {
            PasteType.TEXT_TYPE,
            PasteType.HTML_TYPE,
            ->
                listOf(createCopyContextMenuItem(pasteData), createEditContextMenuItem(pasteData))
            PasteType.COLOR_TYPE,
            PasteType.URL_TYPE,
            PasteType.RTF_TYPE,
            ->
                listOf(createCopyContextMenuItem(pasteData), createOpenContextMenuItem(pasteData))
            PasteType.IMAGE_TYPE ->
                buildList {
                    add(createCopyContextMenuItem(pasteData))
                    addCutIfOwnedFiles(pasteData)
                    add(createEditContextMenuItem(pasteData))
                }
            PasteType.FILE_TYPE ->
                buildList {
                    add(createCopyContextMenuItem(pasteData))
                    addCutIfOwnedFiles(pasteData)
                    add(createOpenContextMenuItem(pasteData))
                }
            else -> null
        }

    private fun createContributedMenuItems(pasteData: PasteData): List<ContextMenuItem> =
        contributors
            .flatMap { contributor ->
                try {
                    contributor.contribute(pasteData)
                } catch (e: Exception) {
                    logger.error(e) { "Contributor ${contributor::class.simpleName} failed to contribute menu items" }
                    emptyList()
                }
            }.map(::toContextMenuItem)

    private fun toContextMenuItem(action: PasteMenuAction): ContextMenuItem =
        if (action.children.isEmpty()) {
            ContextMenuItem(action.label) {
                menuScope.launch { action.onClick() }
            }
        } else {
            ContextMenuGroup(action.label) { action.children.map(::toContextMenuItem) }
        }

    private fun createMenuItems(pasteData: PasteData): List<ContextMenuItem> {
        if (pasteData.pasteState == PasteState.LOADING) {
            return createLoadingMenuItems(pasteData)
        }
        val primaryItems = createPrimaryMenuItems(pasteData) ?: return createLoadingMenuItems(pasteData)
        return buildList {
            addAll(primaryItems)
            addAll(createContributedMenuItems(pasteData))
            add(createPinTagMenuItem(pasteData))
            add(createSyncToMenuItem(pasteData))
            add(ContextMenuDivider)
            add(createDeleteContextMenuItem(pasteData))
        }
    }

    fun pasteMenuItemsProvider(pasteData: PasteData): () -> List<ContextMenuItem> =
        {
            createMenuItems(pasteData)
        }
}
