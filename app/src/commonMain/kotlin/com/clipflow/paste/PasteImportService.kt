package com.clipflow.paste

import com.clipflow.app.AppFileType
import com.clipflow.db.paste.PasteDao
import com.clipflow.exception.PasteException
import com.clipflow.exception.StandardErrorCode
import com.clipflow.notification.MessageType
import com.clipflow.notification.NotificationManager
import com.clipflow.paste.item.PasteFiles
import com.clipflow.paste.item.PasteItemReader
import com.clipflow.paste.item.bindItem
import com.clipflow.paste.item.getFilePaths
import com.clipflow.path.UserDataPathProvider
import com.clipflow.utils.DateUtils
import com.clipflow.utils.getCodecsUtils
import com.clipflow.utils.getCompressUtils
import com.clipflow.utils.getFileUtils
import com.clipflow.utils.ioDispatcher
import com.clipflow.utils.namedScope
import com.clipflow.utils.noOptionParent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import okio.Path

class PasteImportService(
    private val notificationManager: NotificationManager,
    private val pasteDao: PasteDao,
    private val pasteItemReader: PasteItemReader,
    private val searchContentService: SearchContentService,
    private val userDataPathProvider: UserDataPathProvider,
) {
    private val logger = KotlinLogging.logger { }

    private val codecsUtils = getCodecsUtils()

    private val compressUtils = getCompressUtils()

    private val fileUtils = getFileUtils()

    private val ioCoroutineDispatcher = namedScope(ioDispatcher, "PasteImportService")

    private val mutex = Mutex()

    fun import(
        pasteImportParam: PasteImportParam,
        updateProgress: (Float) -> Unit,
        onResult: (PasteImportResult) -> Unit = {},
    ) {
        ioCoroutineDispatcher.launch {
            if (!mutex.tryLock()) {
                logger.warn { "Import is already in progress, ignoring duplicate import request" }
                return@launch
            }
            try {
                doImport(pasteImportParam, updateProgress, onResult)
            } finally {
                mutex.unlock()
            }
        }
    }

    private suspend fun doImport(
        pasteImportParam: PasteImportParam,
        updateProgress: (Float) -> Unit,
        onResult: (PasteImportResult) -> Unit,
    ) {
        var importTempPath: Path? = null
        runCatching {
            val tempDir = userDataPathProvider.resolve(appFileType = AppFileType.TEMP)
            val epochMilliseconds = DateUtils.nowEpochMilliseconds()
            val basePath = tempDir.resolve("import-$epochMilliseconds", true)
            importTempPath = basePath
            userDataPathProvider.autoCreateDir(basePath)
            decompress(pasteImportParam, basePath)
            val importCount =
                fileUtils
                    .listFiles(basePath) {
                        it.name.endsWith(".count")
                    }.firstOrNull()
                    ?.name
                    ?.removeSuffix(".count")
                    ?.toLongOrNull() ?: 0L
            val pasteDataFile = basePath.resolve("paste.data")
            if (!fileUtils.existFile(pasteDataFile)) {
                throw PasteException(
                    StandardErrorCode.IMPORT_FAIL.toErrorCode(),
                    "Failed to find paste.data file",
                )
            }

            var totalCount = 0L
            var successCount = 0L

            fileUtils.readByLines(pasteDataFile) { line ->
                totalCount++
                readPasteData(line)?.let { pasteData ->
                    if (importPasteData(basePath, totalCount, pasteData)) {
                        successCount++
                    }
                } ?: run {
                    logger.error { "Error parsing paste data, index = $totalCount" }
                }
                val progress = if (importCount > 0L) totalCount.toFloat() / importCount.toFloat() else 0f
                updateProgress(progress)
            }
            if (successCount > 0 && successCount < totalCount) {
                notificationManager.sendNotification(
                    title = { it.getText("import_partial") },
                    messageType = MessageType.Warning,
                )
            } else if (successCount > 0) {
                notificationManager.sendNotification(
                    title = { it.getText("import_successful") },
                    messageType = MessageType.Success,
                )
            } else if (totalCount > 0) {
                notificationManager.sendNotification(
                    title = { it.getText("import_fail") },
                    messageType = MessageType.Error,
                )
            } else {
                notificationManager.sendNotification(
                    title = { it.getText("nothing_to_import") },
                    messageType = MessageType.Warning,
                )
            }
            updateProgress(1f)
            onResult(PasteImportResult.Completed(successCount, totalCount))
        }.onFailure { e ->
            updateProgress(-1f)
            onResult(PasteImportResult.Failed)
            logger.error(e) { "Error importing paste data" }
            notificationManager.sendNotification(
                title = { it.getText("import_fail") },
                messageType = MessageType.Error,
            )
        }.apply {
            importTempPath?.let { fileUtils.deleteFile(it) }
        }
    }

    private suspend fun importPasteData(
        basePath: Path,
        index: Long,
        pasteData: PasteData,
    ): Boolean {
        var recordId: Long? = null
        return runCatching {
            userDataPathProvider.validateImportPaths(pasteData)
            val id = pasteDao.createPasteData(pasteData)
            recordId = id

            val pasteCoordinate = pasteData.getPasteCoordinate(id = id)
            val pasteAppearItem = pasteData.pasteAppearItem
            val pasteCollection = pasteData.pasteCollection

            val newPasteAppearItem = pasteAppearItem?.bindItem(pasteCoordinate)
            val newPasteCollection = pasteCollection.bindItems(pasteCoordinate)

            val importPasteData =
                pasteData.copy(
                    id = id,
                    pasteAppearItem = newPasteAppearItem,
                    pasteCollection = newPasteCollection,
                    pasteSearchContent =
                        searchContentService.createSearchContent(
                            pasteData.source,
                            newPasteAppearItem?.let { pasteItemReader.getSearchContent(it) },
                        ),
                )

            val pasteFilesList = importPasteData.getPasteAppearItems().filterIsInstance<PasteFiles>()

            if (pasteFilesList.isNotEmpty()) {
                pasteDao.updateFilePath(importPasteData)
            }
            for (pasteFiles in pasteFilesList) {
                moveResource(basePath, index, importPasteData, pasteFiles)
            }

            pasteDao.updatePasteState(id, PasteState.LOADED)
        }.onFailure { e ->
            logger.error(e) { "Error importing paste data, index = $index" }
            // A delete task, not a bare state change, so the files moved in so far are reclaimed
            recordId?.let { pasteDao.markDeletePasteData(it) }
        }.isSuccess
    }

    private fun moveResource(
        basePath: Path,
        index: Long,
        importPasteData: PasteData,
        pasteFiles: PasteFiles,
    ) {
        val path =
            basePath
                .resolve(importPasteData.appInstanceId)
                .resolve(index.toString())

        for (filePath in pasteFiles.getFilePaths(userDataPathProvider)) {
            val importFilePath = path.resolve(filePath.name)
            userDataPathProvider.autoCreateDir(filePath.noOptionParent)
            // A record without its file is not imported, it is failed and reclaimed
            fileUtils.moveFile(importFilePath, filePath).getOrThrow()
        }
    }

    private fun readPasteData(line: String): PasteData? {
        val json = codecsUtils.base64Decode(line).decodeToString()
        return PasteData.fromStoredJson(json)
    }

    private fun decompress(
        pasteImportParam: PasteImportParam,
        decompressPath: Path,
    ) {
        pasteImportParam.importBufferedSource()?.let { bufferSource ->
            try {
                compressUtils
                    .unzip(bufferSource, decompressPath)
                    .onFailure {
                        logger.error(it) { "Failed to decompress the file" }
                        throw PasteException(
                            errorCode = StandardErrorCode.IMPORT_FAIL.toErrorCode(),
                            message = "Failed to decompress the file",
                        )
                    }
            } finally {
                runCatching { bufferSource.close() }
            }
        } ?: run {
            throw PasteException(
                errorCode = StandardErrorCode.IMPORT_FAIL.toErrorCode(),
                message = "Failed to read the file",
            )
        }
    }
}
