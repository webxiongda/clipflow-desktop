package com.clipflow.paste

import com.clipflow.db.paste.PasteDao
import com.clipflow.path.UserDataPathProvider
import com.clipflow.presist.FilesIndex
import com.clipflow.presist.buildFilesIndex
import com.clipflow.sync.FilePullService

interface CacheManager {

    val pasteDao: PasteDao

    val userDataPathProvider: UserDataPathProvider

    suspend fun getFilesIndex(id: Long): FilesIndex?

    fun loadKey(id: Long): FilesIndex? =
        pasteDao.getLoadedPasteDataBlock(id)?.let { pasteData ->
            buildFilesIndex(pasteData, userDataPathProvider, FilePullService.CHUNK_SIZE)
        }
}
