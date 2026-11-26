package com.clipflow.ui.model

import androidx.compose.ui.graphics.toArgb
import com.clipflow.app.AppFileType
import com.clipflow.i18n.GlobalCopywriter
import com.clipflow.paste.PasteCollection
import com.clipflow.paste.PasteData
import com.clipflow.paste.PasteState
import com.clipflow.paste.PasteType
import com.clipflow.paste.item.CreatePasteItemHelper.createColorPasteItem
import com.clipflow.paste.item.CreatePasteItemHelper.createFilesPasteItem
import com.clipflow.paste.item.CreatePasteItemHelper.createHtmlPasteItem
import com.clipflow.paste.item.CreatePasteItemHelper.createImagesPasteItem
import com.clipflow.paste.item.CreatePasteItemHelper.createTextPasteItem
import com.clipflow.paste.item.CreatePasteItemHelper.createUrlPasteItem
import com.clipflow.paste.item.PasteItemProperties.MARKETING_PATH
import com.clipflow.paste.item.PasteItemProperties.TITLE
import com.clipflow.path.UserDataPathProvider
import com.clipflow.platform.Platform.Companion.MACOS
import com.clipflow.presist.FileInfoTree
import com.clipflow.utils.ColorParser.parseHexColor
import com.clipflow.utils.getCodecsUtils
import com.clipflow.utils.getFileUtils
import com.clipflow.utils.noOptionParent
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

open class MarketingPasteData(
    copywriter: GlobalCopywriter,
    private val userDataPathProvider: UserDataPathProvider,
) {

    private val codecsUtils = getCodecsUtils()

    private val fileUtils = getFileUtils()

    private val color =
        run {
            val colorHex = "#A6D6D6FF"
            val pasteItem =
                createColorPasteItem(
                    color = parseHexColor(colorHex)!!.toArgb(),
                )

            PasteData(
                id = 1L,
                appInstanceId = "$MACOS-id",
                favorite = false,
                pasteAppearItem = pasteItem,
                pasteCollection = PasteCollection(listOf()),
                pasteType = PasteType.COLOR_TYPE.type,
                source = "Figma",
                size = pasteItem.size,
                hash = pasteItem.hash,
                pasteState = PasteState.LOADED,
            )
        }

    private val url =
        run {
            val imagePath =
                userDataPathProvider
                    .resolve(appFileType = AppFileType.MARKETING)
                    .resolve("openGraphImage.png")
                    .toString()

            val url = "https://github.com"
            val urlBytes = url.encodeToByteArray()

            PasteData(
                id = 2L,
                appInstanceId = "$MACOS-id",
                favorite = false,
                pasteAppearItem =
                    createUrlPasteItem(
                        url = url,
                        extraInfo =
                            buildJsonObject {
                                put(MARKETING_PATH, imagePath)
                                put(TITLE, "Github - Build software better, together")
                            },
                    ),
                pasteCollection = PasteCollection(listOf()),
                pasteType = PasteType.URL_TYPE.type,
                source = "Google Chrome",
                size = urlBytes.size.toLong(),
                hash = codecsUtils.hash(urlBytes),
                pasteState = PasteState.LOADED,
            )
        }

    private val zipData =
        run {
            val dataZipPath = userDataPathProvider.resolve("data.zip", AppFileType.MARKETING)
            val fileInfoTree = fileUtils.getFileInfoTree(dataZipPath)
            val fileInfoTrees = mapOf("data.zip" to fileInfoTree)
            val pasteItem =
                createFilesPasteItem(
                    basePath = dataZipPath.noOptionParent.toString(),
                    relativePathList = listOf("data.zip"),
                    fileInfoTreeMap = fileInfoTrees,
                )

            PasteData(
                id = 3L,
                appInstanceId = "$MACOS-id",
                favorite = false,
                pasteAppearItem = pasteItem,
                pasteCollection = PasteCollection(listOf()),
                pasteType = PasteType.FILE_TYPE.type,
                source = "Finder",
                size = pasteItem.size,
                hash = pasteItem.hash,
                pasteState = PasteState.LOADED,
            )
        }

    private val imageFile =
        run {
            val imageName = "sunflower.png"
            val imagePath =
                userDataPathProvider.resolve(
                    imageName,
                    AppFileType.MARKETING,
                )
            val fileInfoTree = fileUtils.getFileInfoTree(imagePath)
            val fileInfoTrees: Map<String, FileInfoTree> = mapOf(imageName to fileInfoTree)
            val pasteItem =
                createImagesPasteItem(
                    basePath = imagePath.noOptionParent.toString(),
                    relativePathList = listOf(imageName),
                    fileInfoTreeMap = fileInfoTrees,
                )

            PasteData(
                id = 4L,
                appInstanceId = "$MACOS-id",
                favorite = false,
                pasteAppearItem = pasteItem,
                pasteCollection = PasteCollection(listOf()),
                pasteType = PasteType.IMAGE_TYPE.type,
                source = "Photo Album",
                size = pasteItem.size,
                hash = pasteItem.hash,
                pasteState = PasteState.LOADED,
            )
        }

    private val language = if (copywriter.language() == "zh") "zh" else "en"

    private val text =
        run {
            val textPath =
                userDataPathProvider.resolve(
                    "$language.txt",
                    AppFileType.MARKETING,
                )

            val byteArray = fileUtils.fileSystem.read(textPath) { readByteArray() }

            val pasteItem =
                createTextPasteItem(
                    text = byteArray.decodeToString(),
                )

            PasteData(
                id = 5L,
                appInstanceId = "$MACOS-id",
                favorite = false,
                pasteAppearItem = pasteItem,
                pasteCollection = PasteCollection(listOf()),
                pasteType = PasteType.TEXT_TYPE.type,
                source = "Notes Archive",
                size = pasteItem.size,
                hash = pasteItem.hash,
                pasteState = PasteState.LOADED,
            )
        }

    private val html =
        run {
            val imagePath =
                userDataPathProvider
                    .resolve(appFileType = AppFileType.MARKETING)
                    .resolve("$language-html2Image.png")
                    .toString()

            val htmlPath =
                userDataPathProvider.resolve(
                    "$language.html",
                    AppFileType.MARKETING,
                )

            val html = fileUtils.fileSystem.read(htmlPath) { readUtf8() }

            val pasteItem =
                createHtmlPasteItem(
                    html = html,
                    extraInfo =
                        buildJsonObject {
                            put(MARKETING_PATH, imagePath)
                        },
                )

            PasteData(
                id = 6L,
                appInstanceId = "$MACOS-id",
                favorite = false,
                pasteAppearItem = pasteItem,
                pasteCollection = PasteCollection(listOf()),
                pasteType = PasteType.HTML_TYPE.type,
                source = "Email",
                size = pasteItem.size,
                hash = pasteItem.hash,
                pasteState = PasteState.LOADED,
            )
        }

    open fun getPasteDataList(): List<PasteData> =
        listOf(
            html,
            text,
            imageFile,
            zipData,
            url,
            color,
        )
}
