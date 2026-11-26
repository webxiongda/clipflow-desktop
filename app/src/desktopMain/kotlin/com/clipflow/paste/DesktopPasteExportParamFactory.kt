package com.clipflow.paste

import okio.Path

class DesktopPasteExportParamFactory : PasteExportParamFactory<Path> {
    override fun createPasteExportParam(
        types: Set<Long>,
        onlyTagged: Boolean,
        maxFileSize: Long?,
        exportPath: Path,
    ): PasteExportParam = DesktopPasteExportParam(types, onlyTagged, maxFileSize, exportPath)
}
