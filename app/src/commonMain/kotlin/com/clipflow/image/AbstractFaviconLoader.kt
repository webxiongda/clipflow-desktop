package com.clipflow.image

import com.clipflow.app.AppFileType
import com.clipflow.config.CommonConfigManager
import com.clipflow.path.UserDataPathProvider
import com.clipflow.utils.ConcurrentLoader
import com.clipflow.utils.StripedMutex
import com.clipflow.utils.getFileUtils
import com.clipflow.utils.getUrlUtils
import io.github.oshai.kotlinlogging.KLogger
import io.ktor.http.*
import okio.Path

abstract class AbstractFaviconLoader(
    private val configManager: CommonConfigManager,
    private val userDataPathProvider: UserDataPathProvider,
) : ConcurrentLoader<String, Path>,
    FaviconLoader {

    protected abstract val logger: KLogger

    protected val fileUtils = getFileUtils()

    private val urlUtils = getUrlUtils()

    override val mutex = StripedMutex()

    private fun getGoogleIconUrl(host: String): String =
        "https://t1.gstatic.com/faviconV2?client=SOCIAL&type=FAVICON&fallback_opts=TYPE,SIZE,URL&url=http://$host&size=256"

    private fun getDefaultIcoUrl(host: String): String = "https://$host/favicon.ico"

    abstract suspend fun saveIco(
        url: String,
        path: Path,
    ): Path?

    override suspend fun load(value: String): Path? {
        if (!urlUtils.isHttpUrl(value)) {
            return null
        }
        return super<ConcurrentLoader>.load(value)
    }

    override fun resolve(
        key: String,
        value: String,
    ): Path = userDataPathProvider.resolve("$key.ico", AppFileType.FAVICON)

    override fun exist(result: Path): Boolean = fileUtils.existFile(result)

    override fun loggerWarning(
        value: String,
        e: Throwable,
    ) {
        logger.warn(e) { "Failed to get favicon for $value" }
    }

    override suspend fun save(
        key: String,
        value: String,
        result: Path,
    ) {
        // Downloading a favicon reveals the copied link's host to the network;
        // when URL preview is disabled, skip the download (cached icons still load)
        if (!configManager.getCurrentConfig().enableUrlPreview) {
            return
        }
        saveIco(getDefaultIcoUrl(key), result)
            ?: saveIco(getGoogleIconUrl(key), result)
    }

    override fun convertToKey(value: String): String = Url(value).host
}
