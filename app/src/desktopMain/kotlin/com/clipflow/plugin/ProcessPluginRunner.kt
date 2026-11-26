package com.clipflow.plugin

import com.clipflow.app.AppInfo
import com.clipflow.app.AppName
import com.clipflow.cli.CliPasteDetailDto
import com.clipflow.cli.toDetailDto
import com.clipflow.db.paste.PasteTagDao
import com.clipflow.i18n.GlobalCopywriter
import com.clipflow.notification.MessageType
import com.clipflow.notification.NotificationManager
import com.clipflow.paste.PasteData
import com.clipflow.paste.PasteReleaseService
import com.clipflow.paste.PasteboardService
import com.clipflow.paste.item.CreatePasteItemHelper.createTextPasteItem
import com.clipflow.paste.item.PasteItem
import com.clipflow.paste.item.PasteItemReader
import com.clipflow.paste.plugin.type.DesktopTextTypePlugin
import com.clipflow.path.UserDataPathProvider
import com.clipflow.ui.base.UISupport
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

/**
 * Runs a process plugin on a paste: the paste goes to stdin in the same JSON
 * shape as `clipflow paste --json` (with rawContent), and stdout is applied
 * according to the manifest's output.
 */
class ProcessPluginRunner(
    private val appInfo: AppInfo,
    private val copywriter: GlobalCopywriter,
    private val environment: PluginEnvironment,
    private val executor: ProcessPluginExecutor,
    private val notificationManager: NotificationManager,
    private val pasteboardService: PasteboardService,
    private val pasteItemReader: PasteItemReader,
    private val pasteReleaseService: PasteReleaseService,
    private val pasteTagDao: PasteTagDao,
    private val uiSupport: UISupport,
    private val userDataPathProvider: UserDataPathProvider,
) {

    companion object {
        private val inputJson = Json { encodeDefaults = true }

        private const val MAX_ERROR_DETAIL_LENGTH = 300
    }

    private val logger = KotlinLogging.logger {}

    suspend fun run(
        plugin: LocalPlugin,
        pasteData: PasteData,
        action: String? = null,
    ) {
        val manifest = plugin.manifest ?: return
        val command = plugin.platform(environment)?.command ?: return
        if (command.isEmpty()) return
        val output = PluginOutput.fromWireName(manifest.output) ?: return
        val name = manifest.displayName(copywriter.language())

        val result =
            try {
                val input = pasteData.toDetailDto(pasteTagDao, pasteItemReader, true, userDataPathProvider)
                executor.execute(
                    directory = plugin.directory,
                    command = command,
                    input = inputJson.encodeToString(CliPasteDetailDto.serializer(), input),
                    action = action,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.warn(e) { "Plugin ${manifest.id} failed to start" }
                notifyFailure(name, e.message ?: e::class.simpleName.orEmpty())
                return
            }

        if (result.stderr.isNotBlank()) {
            logger.info { "Plugin ${manifest.id} stderr:\n${result.stderr}" }
        }
        when (result.exitCode) {
            null -> {
                logger.warn { "Plugin ${manifest.id} timed out" }
                notifyFailure(name, copywriter.getText("plugin_timed_out"))
            }
            0 -> {
                if (result.stdoutTruncated) {
                    logger.warn { "Plugin ${manifest.id} output exceeded the size limit" }
                    notifyFailure(name, copywriter.getText("plugin_output_too_large"))
                    return
                }
                try {
                    applyOutput(name, output, result.stdout.trimEnd('\n'))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logger.warn(e) { "Plugin ${manifest.id} failed to apply output" }
                    notifyFailure(name, e.message ?: e::class.simpleName.orEmpty())
                }
            }
            else -> {
                logger.warn { "Plugin ${manifest.id} exited with ${result.exitCode}" }
                notifyFailure(
                    name,
                    result.stderr
                        .trim()
                        .takeLast(MAX_ERROR_DETAIL_LENGTH)
                        .ifEmpty { "exit ${result.exitCode}" },
                )
            }
        }
    }

    private suspend fun applyOutput(
        name: String,
        output: PluginOutput,
        text: String,
    ) {
        if (text.isEmpty()) {
            notifyFailure(name, copywriter.getText("plugin_no_output"))
            return
        }
        when (output) {
            PluginOutput.REPLACE_CLIPBOARD -> {
                val pasteItem = textPasteItem(text)
                // Our own pasteboard writes are not captured, so record the paste directly
                val id = createLocalPaste(pasteItem)
                pasteboardService
                    .tryWritePasteboard(id = id, pasteItem = pasteItem, localOnly = true)
                    .onSuccess {
                        notificationManager.sendNotification(
                            title = { name },
                            message = { it.getText("copy_successful") },
                            messageType = MessageType.Success,
                        )
                    }.onFailure {
                        notifyFailure(name, it.message ?: copywriter.getText("copy_failed"))
                    }
            }
            PluginOutput.NEW_PASTE -> {
                createLocalPaste(textPasteItem(text))
                notificationManager.sendNotification(
                    title = { name },
                    message = { it.getText("save_successful") },
                    messageType = MessageType.Success,
                )
            }
            PluginOutput.NOTIFICATION ->
                notificationManager.sendNotification(
                    title = { name },
                    message = { text },
                    messageType = MessageType.Info,
                    duration = null,
                )
            PluginOutput.OPEN_URL -> uiSupport.openUrlInBrowser(text.trim())
        }
    }

    private suspend fun createLocalPaste(pasteItem: PasteItem): Long =
        pasteReleaseService.createLocalPasteData(
            appInstanceId = appInfo.appInstanceId,
            pasteItem = pasteItem,
            source = AppName,
        )

    private fun textPasteItem(text: String) =
        createTextPasteItem(
            identifiers = listOf(DesktopTextTypePlugin.TEXT),
            text = text,
        )

    private fun notifyFailure(
        name: String,
        detail: String,
    ) {
        notificationManager.sendNotification(
            title = { it.getText("plugin_run_failed", name) },
            message = { detail },
            messageType = MessageType.Error,
        )
    }
}
