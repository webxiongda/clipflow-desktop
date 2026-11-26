package com.clipflow.paste

import okio.BufferedSource

abstract class PasteImportParam {

    abstract fun importBufferedSource(): BufferedSource?
}
