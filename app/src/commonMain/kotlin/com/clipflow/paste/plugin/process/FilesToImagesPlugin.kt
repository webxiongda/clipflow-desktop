package com.clipflow.paste.plugin.process

import com.clipflow.app.AppFileType
import com.clipflow.paste.item.CreatePasteItemHelper.createImagesPasteItem
import com.clipflow.paste.item.FilesPasteItem
import com.clipflow.paste.item.PasteCoordinate
import com.clipflow.paste.item.PasteItem
import com.clipflow.paste.item.clear
import com.clipflow.paste.item.getFilePaths
import com.clipflow.path.UserDataPathProvider
import com.clipflow.utils.extension
import com.clipflow.utils.getFileUtils
import okio.Path

class FilesToImagesPlugin(
    private val userDataPathProvider: UserDataPathProvider,
) : PasteProcessPlugin {

    private val fileUtils = getFileUtils()

    private val fileBasePath = userDataPathProvider.resolve(appFileType = AppFileType.FILE)
    private val imageBasePath = userDataPathProvider.resolve(appFileType = AppFileType.IMAGE)

    override fun process(
        pasteCoordinate: PasteCoordinate,
        pasteItems: List<PasteItem>,
        source: String?,
    ): List<PasteItem> =
        pasteItems.map { pasteAppearItem ->
            if (pasteAppearItem is FilesPasteItem) {
                if (pasteAppearItem
                        .getFilePaths(userDataPathProvider)
                        .map { path -> path.extension }
                        .all { fileUtils.canPreviewImage(it) }
                ) {
                    val basePath = pasteAppearItem.basePath
                    if (basePath == null) {
                        val moved = mutableListOf<Pair<Path, Path>>()
                        pasteAppearItem.relativePathList.forEach {
                            val srcPath =
                                userDataPathProvider.resolve(
                                    fileBasePath,
                                    it,
                                    autoCreate = false,
                                    isFile = true,
                                )
                            val destPath =
                                userDataPathProvider.resolve(
                                    imageBasePath,
                                    it,
                                    autoCreate = true,
                                    isFile = true,
                                )
                            if (fileUtils.moveFile(srcPath, destPath).isFailure) {
                                // The caller keeps the item as it was, so put the moved files back
                                moved.asReversed().forEach { (from, to) -> fileUtils.moveFile(to, from) }
                                throw IllegalStateException("Failed to move file from $srcPath to $destPath")
                            }
                            moved += srcPath to destPath
                        }
                    }
                    val identifiers = pasteAppearItem.identifiers
                    val relativePathList = pasteAppearItem.relativePathList
                    val fileInfoTreeMap = pasteAppearItem.fileInfoTreeMap
                    pasteAppearItem.clear(
                        clearResource = false,
                        userDataPathProvider = userDataPathProvider,
                    )
                    createImagesPasteItem(
                        identifiers = identifiers,
                        basePath = basePath,
                        relativePathList = relativePathList,
                        fileInfoTreeMap = fileInfoTreeMap,
                        extraInfo = pasteAppearItem.extraInfo,
                    )
                } else {
                    pasteAppearItem
                }
            } else {
                pasteAppearItem
            }
        }
}
