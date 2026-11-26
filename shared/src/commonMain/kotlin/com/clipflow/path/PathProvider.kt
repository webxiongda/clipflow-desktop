package com.clipflow.path

import com.clipflow.app.AppFileType
import com.clipflow.exception.PasteException
import com.clipflow.exception.StandardErrorCode
import com.clipflow.utils.FileUtils
import com.clipflow.utils.getFileUtils
import okio.Path

interface PathProvider {

    val fileUtils: FileUtils get() = getFileUtils()

    fun resolve(
        fileName: String? = null,
        appFileType: AppFileType,
    ): Path

    fun resolve(
        basePath: Path,
        path: String,
        autoCreate: Boolean = true,
        isFile: Boolean = false,
    ): Path {
        val newPath = basePath.resolve(path)
        if (autoCreate) {
            if (isFile) {
                newPath.parent?.let { autoCreateDir(it) }
            } else {
                autoCreateDir(newPath)
            }
        }
        return newPath
    }

    fun autoCreateDir(path: Path) {
        if (fileUtils.createDir(path).isFailure) {
            throw PasteException(
                StandardErrorCode.CANT_CREATE_DIR.toErrorCode(),
                "Failed to create directory: $path",
            )
        }
    }
}
