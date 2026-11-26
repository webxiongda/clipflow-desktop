package com.clipflow.path

import okio.Path

interface PlatformUserDataPathProvider {

    companion object {
        const val CLIPFLOW_DIR_NAME = ".clipflow"
    }

    fun getUserDefaultStoragePath(): Path
}
