package com.clipflow.ui.base

import com.clipflow.app.DesktopAppWindowManager
import com.clipflow.i18n.GlobalCopywriter
import com.clipflow.notification.Message
import com.clipflow.notification.MessageType
import com.clipflow.notification.NotificationManager
import com.clipflow.platform.Platform
import com.clipflow.platform.macos.api.MacosApi
import com.clipflow.platform.windows.WindowsTrayNotifier
import com.clipflow.platform.windows.api.Shell32
import com.clipflow.platform.windows.api.User32
import com.clipflow.sound.SoundService
import com.sun.jna.platform.win32.Kernel32
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger

class DesktopNotificationManager(
    private val appWindowManager: DesktopAppWindowManager,
    copywriter: GlobalCopywriter,
    private val platform: Platform,
    private val soundService: SoundService,
    // Shows a balloon on the Windows tray icon; false when it cannot be shown
    private val postWindowsNotification: (Message) -> Boolean = defaultWindowsNotification(),
    // Posts to macOS Notification Center; false when unavailable to this process
    private val postMacosNotification: (Message) -> Boolean = { message ->
        MacosApi.INSTANCE.postUserNotification(message.title, message.message ?: "") != 0
    },
) : NotificationManager(copywriter) {

    private val logger = KotlinLogging.logger {}

    val idGenerator = AtomicInteger(0)

    override fun getMessageId(): Int = idGenerator.incrementAndGet()

    override fun doSendNotification(message: Message) {
        if (appWindowManager.getCurrentMainWindowInfo().show) {
            pushNotification(message)
        } else if (platform.isLinux()) {
            pushNotification(message)
        } else if (!message.needsSystemNotification()) {
            // Frequent results like "copy successful" would be noise as system notifications
            pushNotification(message)
        } else if (platform.isWindows()) {
            notifySystem(message, postWindowsNotification)
        } else {
            notifySystem(message, postMacosNotification)
        }
        if (message.messageType == MessageType.Error) {
            soundService.errorSound()
        } else if (message.messageType == MessageType.Success) {
            soundService.successSound()
        }
    }

    private fun Message.needsSystemNotification(): Boolean =
        messageType == MessageType.Error || messageType == MessageType.Warning

    private fun notifySystem(
        message: Message,
        post: (Message) -> Boolean,
    ) {
        // runCatching also catches JNA Errors (e.g. UnsatisfiedLinkError); anything thrown here
        // would end the collector in NotificationManager and silence every later notification
        val shown =
            runCatching { post(message) }
                .onFailure { logger.warn(it) { "Failed to show system notification" } }
                .getOrDefault(false)
        if (!shown) {
            pushNotification(message)
        }
    }
}

private fun defaultWindowsNotification(): (Message) -> Boolean {
    val notifier by lazy {
        WindowsTrayNotifier(User32.INSTANCE, Shell32.INSTANCE, Kernel32.INSTANCE.GetCurrentProcessId())
    }
    return { message -> notifier.notify(message.title, message.message ?: "", message.messageType) }
}
