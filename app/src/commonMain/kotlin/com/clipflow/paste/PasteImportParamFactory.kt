package com.clipflow.paste

interface PasteImportParamFactory<T> {

    fun createPasteImportParam(importPath: T): PasteImportParam
}
