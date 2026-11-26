package com.clipflow.presist

import okio.Path

object FilePersist {

    fun createOneFilePersist(path: Path): OneFilePersist = OneFilePersist(path)
}
