package com.clipflow.paste

import com.clipflow.app.DesktopAppWindowManager
import com.clipflow.config.CommonConfigManager
import com.clipflow.notification.NotificationManager
import com.clipflow.platform.Platform
import com.clipflow.sound.SoundService
import com.clipflow.sync.SyncManager

fun getDesktopPasteboardService(
    appWindowManager: DesktopAppWindowManager,
    configManager: CommonConfigManager,
    currentPaste: CurrentPaste,
    notificationManager: NotificationManager,
    pasteConsumer: TransferableConsumer,
    pasteProducer: TransferableProducer,
    pasteReleaseService: PasteReleaseService,
    platform: Platform,
    soundService: SoundService,
    sourceExclusionService: DesktopSourceExclusionService,
    syncManager: SyncManager,
    universalClipboardHint: UniversalClipboardHint,
): AbstractPasteboardService =
    if (platform.isMacos()) {
        MacosPasteboardService(
            appWindowManager,
            configManager,
            currentPaste,
            notificationManager,
            pasteConsumer,
            pasteProducer,
            pasteReleaseService,
            soundService,
            sourceExclusionService,
            syncManager,
            universalClipboardHint,
        )
    } else if (platform.isWindows()) {
        WindowsPasteboardService(
            appWindowManager,
            configManager,
            currentPaste,
            notificationManager,
            pasteConsumer,
            pasteProducer,
            pasteReleaseService,
            platform,
            soundService,
            sourceExclusionService,
        )
    } else if (platform.isLinux()) {
        LinuxPasteboardService(
            appWindowManager,
            configManager,
            currentPaste,
            notificationManager,
            pasteConsumer,
            pasteProducer,
            pasteReleaseService,
            soundService,
            sourceExclusionService,
        )
    } else {
        throw IllegalStateException("Unsupported platform: ${platform.name}")
    }
