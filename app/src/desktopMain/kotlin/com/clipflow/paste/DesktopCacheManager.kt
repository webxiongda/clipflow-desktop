package com.clipflow.paste

import com.clipflow.db.paste.PasteDao
import com.clipflow.path.UserDataPathProvider
import com.clipflow.presist.FilesIndex
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.TimeUnit

class DesktopCacheManager(
    override val pasteDao: PasteDao,
    override val userDataPathProvider: UserDataPathProvider,
) : CacheManager {

    private val logger = KotlinLogging.logger {}

    private val filesIndexCache: LoadingCache<Long, FilesIndex?> =
        Caffeine
            .newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build(::loadKey)

    override suspend fun getFilesIndex(id: Long): FilesIndex? =
        runCatching {
            filesIndexCache.get(id)
        }.onFailure { e ->
            logger.warn(e) { "getFilesIndex failed: $id" }
        }.getOrNull()
}
