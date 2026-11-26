package com.clipflow.paste.plugin.type

import com.clipflow.app.AppFileType
import com.clipflow.app.AppInfo
import com.clipflow.config.CommonConfigManager
import com.clipflow.paste.PasteCollector
import com.clipflow.paste.PasteDataFlavor
import com.clipflow.paste.PasteDataFlavors
import com.clipflow.paste.PasteDataFlavors.URL_FLAVOR
import com.clipflow.paste.PasteTransferable
import com.clipflow.paste.PasteType
import com.clipflow.paste.PasteWriteScope
import com.clipflow.paste.item.CreatePasteItemHelper.createFilesPasteItem
import com.clipflow.paste.item.FilesPasteItem
import com.clipflow.paste.item.PasteCoordinate
import com.clipflow.paste.item.PasteItem
import com.clipflow.paste.item.getFilePaths
import com.clipflow.paste.toPasteDataFlavor
import com.clipflow.path.UserDataPathProvider
import com.clipflow.platform.Platform
import com.clipflow.presist.FileInfoTree
import com.clipflow.utils.FileNameNormalizer
import com.clipflow.utils.getFileUtils
import com.clipflow.utils.noOptionParent
import okio.Path.Companion.toOkioPath
import java.awt.datatransfer.DataFlavor
import java.io.ByteArrayInputStream
import java.io.File

class DesktopFilesTypePlugin(
    private val appInfo: AppInfo,
    private val configManager: CommonConfigManager,
    private val platform: Platform,
    private val userDataPathProvider: UserDataPathProvider,
) : FilesTypePlugin {

    companion object {

        const val FILE_LIST_ID = "application/x-java-file-list"
    }

    private val fileUtils = getFileUtils()

    override fun getPasteType(): PasteType = PasteType.FILE_TYPE

    override fun getIdentifiers(): List<String> = listOf(FILE_LIST_ID)

    override fun createPrePasteItem(
        itemIndex: Int,
        identifier: String,
        pasteTransferable: PasteTransferable,
        pasteCollector: PasteCollector,
    ) {
        createFilesPasteItem(
            identifiers = listOf(identifier),
            relativePathList = listOf(),
            fileInfoTreeMap = mapOf(),
        ).let {
            pasteCollector.preCollectItem(itemIndex, this::class, it)
        }
    }

    override suspend fun doLoadRepresentation(
        transferData: Any,
        pasteId: Long,
        itemIndex: Int,
        identity: String,
        dataFlavor: PasteDataFlavor,
        dataFlavorMap: Map<String, List<PasteDataFlavor>>,
        pasteTransferable: PasteTransferable,
        pasteCollector: PasteCollector,
    ) {
        if (transferData is List<*>) {
            val files = transferData.filterIsInstance<File>()

            if (files.isEmpty()) {
                return
            }

            val paths = files.map { it.toOkioPath(normalize = true) }

            val parentPath = paths[0].noOptionParent

            val fileInfoTrees = mutableMapOf<String, FileInfoTree>()
            val relativePathList = mutableListOf<String>()

            val maxBackupFileSize =
                fileUtils.bytesSize(
                    configManager.getCurrentConfig().maxBackupFileSize,
                )

            val sumFileSize =
                fileUtils.calculateTotalSizeWithLimit(
                    paths,
                    maxBackupFileSize,
                )

            val copySizeExceeding = maxBackupFileSize < sumFileSize

            val copyFromClipFlow =
                files.any {
                    it.startsWith(userDataPathProvider.getUserDataPath().toFile())
                }

            // If the file size exceeds the limit
            // or the file is copied from the clip-flow directory
            // the file will not be copied
            // Instead, record the file path
            val useRefCopyFiles = copySizeExceeding || copyFromClipFlow

            for (path in paths) {
                val originFileName = path.name
                val fileName = FileNameNormalizer.normalize(originFileName)

                if (useRefCopyFiles) {
                    relativePathList.add(originFileName)
                    fileInfoTrees[originFileName] = fileUtils.getFileInfoTree(path)
                } else {
                    val relativePath =
                        fileUtils.createPasteRelativePath(
                            pasteCoordinate =
                                PasteCoordinate(
                                    id = pasteId,
                                    appInstanceId = appInfo.appInstanceId,
                                ),
                            fileName = fileName,
                        )
                    relativePathList.add(relativePath)
                    val filePath =
                        fileUtils.createPastePath(
                            relativePath,
                            isFile = true,
                            AppFileType.FILE,
                            userDataPathProvider,
                        )
                    if (fileUtils.copyPath(path, filePath).isSuccess) {
                        fileInfoTrees[fileName] = fileUtils.getFileInfoTree(filePath)
                    } else {
                        throw IllegalStateException("Failed to copy file")
                    }
                }
            }

            val update: (PasteItem) -> PasteItem = { pasteItem ->
                createFilesPasteItem(
                    identifiers = pasteItem.identifiers,
                    basePath = if (useRefCopyFiles) parentPath.toString() else null,
                    relativePathList = relativePathList,
                    fileInfoTreeMap = fileInfoTrees,
                    extraInfo = pasteItem.extraInfo,
                )
            }
            pasteCollector.updateCollectItem(itemIndex, this::class, update)
        }
    }

    override suspend fun buildTransferable(
        pasteItem: PasteItem,
        scope: PasteWriteScope,
        map: MutableMap<PasteDataFlavor, Any>,
    ) {
        pasteItem as FilesPasteItem
        val fileList: List<File> = pasteItem.getFilePaths(userDataPathProvider).map { it.toFile() }
        map[DataFlavor.javaFileListFlavor.toPasteDataFlavor()] = fileList
        // Next to items of the other category, offer the files as text and URIs
        // too, so a consumer that only takes text still gets something to paste.
        if (scope == PasteWriteScope.ALL) {
            map[PasteDataFlavors.URI_LIST_FLAVOR.toPasteDataFlavor()] =
                ByteArrayInputStream(
                    fileList
                        .joinToString(separator = "\n") { it.absolutePath }
                        .encodeToByteArray(),
                )
            map[DataFlavor.stringFlavor.toPasteDataFlavor()] =
                fileList.joinToString(separator = "\n") { it.name }

            if (fileList.size == 1) {
                map[URL_FLAVOR.toPasteDataFlavor()] = fileList[0].toURI().toURL()
            }
        }

        if (platform.isLinux()) {
            val content =
                fileList.joinToString(
                    separator = "\n",
                    prefix = "copy\n",
                ) { it.toURI().toString() }
            val inputStream = ByteArrayInputStream(content.encodeToByteArray())
            map[PasteDataFlavors.GNOME_COPIED_FILES_FLAVOR.toPasteDataFlavor()] = inputStream
        }
    }
}
