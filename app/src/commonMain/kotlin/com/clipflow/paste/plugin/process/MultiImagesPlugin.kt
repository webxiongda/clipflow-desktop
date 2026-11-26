package com.clipflow.paste.plugin.process

import com.clipflow.paste.item.CreatePasteItemHelper.createImagesPasteItem
import com.clipflow.paste.item.ImagesPasteItem
import com.clipflow.paste.item.PasteCoordinate
import com.clipflow.paste.item.PasteItem
import com.clipflow.paste.item.clear
import com.clipflow.path.UserDataPathProvider

class MultiImagesPlugin(
    private val userDataPathProvider: UserDataPathProvider,
) : PasteProcessPlugin {

    override fun process(
        pasteCoordinate: PasteCoordinate,
        pasteItems: List<PasteItem>,
        source: String?,
    ): List<PasteItem> =
        if (pasteItems.size <= 1) {
            pasteItems
        } else {
            val allMemoryImages =
                pasteItems.map { it as ImagesPasteItem }.all {
                    it.identifiers.all { identifier -> identifier.startsWith("image") }
                }

            if (allMemoryImages) {
                val maxSizeImagePasteItem =
                    pasteItems.maxBy { it.size } as ImagesPasteItem

                pasteItems
                    .filter { it != maxSizeImagePasteItem }
                    .forEach {
                        it.clear(
                            clearResource = true,
                            userDataPathProvider = userDataPathProvider,
                        )
                    }

                listOf(maxSizeImagePasteItem)
            } else {
                val relativePathList =
                    pasteItems.map { it as ImagesPasteItem }.flatMap { it.relativePathList }
                val fileInfoMap =
                    pasteItems
                        .map { it as ImagesPasteItem }
                        .flatMap { it.fileInfoTreeMap.entries }
                        .associate { it.key to it.value }
                pasteItems.forEach {
                    it.clear(
                        clearResource = false,
                        userDataPathProvider = userDataPathProvider,
                    )
                }
                createImagesPasteItem(
                    identifiers = pasteItems.flatMap { it.identifiers },
                    relativePathList = relativePathList,
                    fileInfoTreeMap = fileInfoMap,
                ).let { listOf(it) }
            }
        }
}
