package com.clipflow.paste.plugin.process

import com.clipflow.paste.item.PasteCoordinate
import com.clipflow.paste.item.PasteFiles
import com.clipflow.paste.item.PasteItem
import com.clipflow.paste.item.clear
import com.clipflow.paste.item.getFilePaths
import com.clipflow.path.UserDataPathProvider
import com.clipflow.utils.isDirectory

class RemoveFolderImagePlugin(
    private val userDataPathProvider: UserDataPathProvider,
) : PasteProcessPlugin {
    override fun process(
        pasteCoordinate: PasteCoordinate,
        pasteItems: List<PasteItem>,
        source: String?,
    ): List<PasteItem> {
        pasteItems.firstOrNull { it.getPasteType().isImage() }?.let { imageItem ->
            val files = imageItem as PasteFiles
            if (files.getFilePaths(userDataPathProvider).size == 1) {
                pasteItems.firstOrNull { it.getPasteType().isFile() }?.let {
                    val pasteFiles = it as PasteFiles
                    if (it.getFilePaths(userDataPathProvider).size == 1 &&
                        pasteFiles.getFilePaths(userDataPathProvider)[0].isDirectory
                    ) {
                        imageItem.clear(
                            clearResource = true,
                            userDataPathProvider = userDataPathProvider,
                        )
                        return pasteItems.filter { item -> item != imageItem }
                    }
                }
            }
        }
        return pasteItems
    }
}
