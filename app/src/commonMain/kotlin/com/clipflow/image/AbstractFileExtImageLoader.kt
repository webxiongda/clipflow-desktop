package com.clipflow.image

import com.clipflow.app.AppFileType
import com.clipflow.path.UserDataPathProvider
import com.clipflow.utils.ConcurrentLoader
import com.clipflow.utils.StripedMutex
import com.clipflow.utils.extension
import com.clipflow.utils.getFileUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import okio.Path

abstract class AbstractFileExtImageLoader(
    private val userDataPathProvider: UserDataPathProvider,
) : ConcurrentLoader<Path, Path>,
    FileExtImageLoader {

    private val logger = KotlinLogging.logger {}

    private val fileUtils = getFileUtils()

    override val mutex = StripedMutex()

    override fun resolve(
        key: String,
        value: Path,
    ): Path = userDataPathProvider.resolve("$key.png", AppFileType.FILE_EXT_ICON)

    override fun exist(result: Path): Boolean = fileUtils.existFile(result)

    override fun loggerWarning(
        value: Path,
        e: Throwable,
    ) {
        logger.warn { "Failed to load icon for file extension: $value" }
    }

    override fun convertToKey(value: Path): String = value.extension
}
