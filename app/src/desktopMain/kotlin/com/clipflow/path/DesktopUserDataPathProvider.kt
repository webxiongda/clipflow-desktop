package com.clipflow.path

import com.clipflow.config.DevConfig
import com.clipflow.path.LinuxAppPathProvider.Companion.LOCAL
import com.clipflow.path.LinuxAppPathProvider.Companion.SHARE
import com.clipflow.path.PlatformUserDataPathProvider.Companion.CLIPFLOW_DIR_NAME
import com.clipflow.platform.Platform
import com.clipflow.utils.getAppEnvUtils
import com.clipflow.utils.getFileUtils
import com.clipflow.utils.getSystemProperty
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.Path
import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath
import kotlin.io.path.createTempDirectory

fun getPlatformPathProvider(platform: Platform): PlatformUserDataPathProvider {
    val appEnvUtils = getAppEnvUtils()
    return if (appEnvUtils.isDevelopment()) {
        DevelopmentPlatformUserDataPathProvider()
    } else if (appEnvUtils.isTest()) {
        // In the test environment, DesktopPathProvider will be mocked
        TestPlatformUserDataPathProvider()
    } else {
        if (platform.isWindows()) {
            WindowsPlatformUserDataPathProvider()
        } else if (platform.isMacos()) {
            MacosPlatformUserDataPathProvider()
        } else if (platform.isLinux()) {
            LinuxPlatformUserDataPathProvider()
        } else {
            throw IllegalStateException("Unknown platform: ${platform.name}")
        }
    }
}

class DevelopmentPlatformUserDataPathProvider : PlatformUserDataPathProvider {

    private val systemProperty = getSystemProperty()

    private val composeAppDir = systemProperty.get("user.dir")

    override fun getUserDefaultStoragePath(): Path =
        DevConfig.pasteUserPath?.let {
            val path = it.toPath(normalize = true)
            if (path.isAbsolute) {
                path
            } else {
                composeAppDir.toPath().resolve(it)
            }
        } ?: composeAppDir.toPath()
}

class TestPlatformUserDataPathProvider : PlatformUserDataPathProvider {

    private val tempDir = createTempDirectory(CLIPFLOW_DIR_NAME)

    override fun getUserDefaultStoragePath(): Path = tempDir.toOkioPath(normalize = true)
}

class WindowsPlatformUserDataPathProvider : PlatformUserDataPathProvider {

    private val systemProperty = getSystemProperty()

    private val userHome: Path = systemProperty.get("user.home").toPath(normalize = true)

    override fun getUserDefaultStoragePath(): Path = userHome.resolve(CLIPFLOW_DIR_NAME)
}

class MacosPlatformUserDataPathProvider : PlatformUserDataPathProvider {

    private val systemProperty = getSystemProperty()

    private val fileUtils = getFileUtils()

    private val userHome: Path = systemProperty.get("user.home").toPath(normalize = true)

    override fun getUserDefaultStoragePath(): Path {
        val appSupportPath =
            userHome
                .resolve("Library")
                .resolve("Application Support")
                .resolve("ClipFlow")
        if (!fileUtils.existFile(appSupportPath)) {
            fileUtils.createDir(appSupportPath).getOrThrow()
        }
        return appSupportPath
    }
}

class LinuxPlatformUserDataPathProvider : PlatformUserDataPathProvider {

    companion object {
        val fileUtils = getFileUtils()
    }

    private val logger = KotlinLogging.logger {}

    private val migrateLock = Mutex()

    @Volatile private var migrationChecked = false

    private val systemProperty = getSystemProperty()

    private val userHome: Path = systemProperty.get("user.home").toPath(normalize = true)

    override fun getUserDefaultStoragePath(): Path {
        // Migrate from ~/.local/shard/.clipflow to ~/.local/share/.clipflow
        val oldPath = userHome.resolve(LOCAL).resolve("shard").resolve(CLIPFLOW_DIR_NAME)
        val newPath = userHome.resolve(LOCAL).resolve(SHARE).resolve(CLIPFLOW_DIR_NAME)

        if (!migrationChecked) {
            runBlockingMigrationOnce(oldPath, newPath)
        }

        return newPath
    }

    private fun runBlockingMigrationOnce(
        oldPath: Path,
        newPath: Path,
    ) {
        // double-check locking
        if (migrationChecked) return

        runBlocking {
            migrateLock.withLock {
                if (migrationChecked) return@withLock

                val oldExists = fileUtils.existFile(oldPath)
                val newExists = fileUtils.existFile(newPath)

                if (oldExists && !newExists) {
                    runCatching {
                        fileUtils.createDir(newPath)
                        fileUtils.moveFile(oldPath, newPath)
                    }.onFailure { e ->
                        logger.error(e) { "Error migration from $oldPath move to $newPath" }
                        runCatching {
                            fileUtils.fileSystem.copy(oldPath, newPath)
                        }.onFailure { e2 ->
                            logger.error(e2) { "Error migration from $oldPath copy to $newPath" }
                        }
                    }
                }

                migrationChecked = true
            }
        }
    }
}
