package com.clipflow.image

import com.clipflow.app.AppInfo
import com.clipflow.app.AppName
import com.clipflow.app.generated.resources.Res
import com.clipflow.path.UserDataPathProvider
import com.clipflow.utils.FileUtils
import com.clipflow.utils.getFileUtils
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Stores ClipFlow's own Mac-style icon (transparent padding included) as the
 * source icon of the pastes it creates itself, such as OCR and plugin output,
 * so they load, crop and tint exactly like any captured app icon. Rewritten on
 * every launch so the icon follows the bundled logo across upgrades.
 */
suspend fun installClipFlowSourceIcon(
    appInfo: AppInfo,
    userDataPathProvider: UserDataPathProvider,
    fileUtils: FileUtils = getFileUtils(),
) {
    runCatching {
        val bytes = Res.readBytes("drawable/clipflow_mac.png")
        val iconPath = userDataPathProvider.resolveIconPath(appInfo.appInstanceId, AppName)
        if (fileUtils.existFile(iconPath) && fileUtils.getFileSize(iconPath) == bytes.size.toLong()) {
            return
        }
        val tempPath = iconPath.parent?.resolve("${iconPath.name}.tmp") ?: return
        fileUtils.fileSystem.write(tempPath) { write(bytes) }
        fileUtils.moveFile(tempPath, iconPath).getOrThrow()
    }.onFailure { logger.warn(it) { "Failed to install the ClipFlow source icon" } }
}
