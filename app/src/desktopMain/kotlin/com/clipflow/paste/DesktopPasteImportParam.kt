package com.clipflow.paste

import com.clipflow.utils.getFileUtils
import okio.BufferedSource
import okio.Path
import okio.buffer

class DesktopPasteImportParam(
    private val importPath: Path,
) : PasteImportParam() {

    private val fileUtils = getFileUtils()

    override fun importBufferedSource(): BufferedSource? = fileUtils.fileSystem.source(importPath).buffer()
}
