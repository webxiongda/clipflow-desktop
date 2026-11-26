package com.clipflow.headless

import com.clipflow.DesktopModule
import com.clipflow.app.AppEnv
import com.clipflow.app.AppExitService
import com.clipflow.app.AppFileType
import com.clipflow.app.AppLock
import com.clipflow.app.DesktopPidFileService
import com.clipflow.clean.CleanScheduler
import com.clipflow.cli.CliServer
import com.clipflow.config.AppMetadataRepository
import com.clipflow.config.DesktopConfigManager
import com.clipflow.db.DriverFactory
import com.clipflow.log.ClipFlowLogger
import com.clipflow.mcp.McpServer
import com.clipflow.net.NetworkInterfaceService
import com.clipflow.net.PasteBonjourService
import com.clipflow.net.PasteClient
import com.clipflow.net.ResourcesClient
import com.clipflow.net.Server
import com.clipflow.net.TestNetworkInterfaceService
import com.clipflow.notification.NotificationManager
import com.clipflow.paste.PasteboardService
import com.clipflow.path.AppPathProvider
import com.clipflow.path.DesktopPathProvider
import com.clipflow.path.UserDataPathProvider
import com.clipflow.presist.FilePersist
import com.clipflow.rendering.RenderingService
import com.clipflow.secure.SecureStore
import com.clipflow.sync.PastePullService
import com.clipflow.sync.QRCodeGenerator
import com.clipflow.sync.SyncManager
import com.clipflow.task.TaskExecutor
import com.clipflow.utils.DesktopDeviceUtils
import com.clipflow.utils.DesktopLocaleUtils
import com.clipflow.utils.getPlatformUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okio.Path
import okio.Path.Companion.toOkioPath
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.nio.file.Files
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Resolves every service the headless startup and shutdown paths in
 * [com.clipflow.ClipFlow] touch against the complete headless Koin
 * assembly ([DesktopModule.modules] with headless = true).
 *
 * This is the regression pin for definition gaps that only surface at
 * runtime on a server: a GUI-only binding referenced from a service that
 * headless mode starts (e.g. DesktopAppUpdateService requiring the
 * uiModule-only DesktopAppWindowManager) crashes the daemon with
 * NoDefinitionFoundException after it has already written its pid file.
 */
class HeadlessStartupResolutionTest {

    private class TempAppPathProvider(
        root: Path,
    ) : AppPathProvider {
        override val userHome: Path = root
        override val pasteAppPath: Path = root
        override val pasteAppJarPath: Path = root
        override val pasteAppExePath: Path = root
        override val pasteUserPath: Path = root

        private val pathProvider = DesktopPathProvider(pasteAppPath, pasteUserPath)

        override fun resolve(
            fileName: String?,
            appFileType: AppFileType,
        ): Path = pathProvider.resolve(fileName, appFileType)
    }

    @Test
    fun testHeadlessStartupAndShutdownServicesResolve(): Unit =
        runBlocking {
            val tempRoot = createTempDirectory("clipflow-headless-di").toOkioPath(normalize = true)
            val platform = getPlatformUtils().platform
            val appPathProvider = TempAppPathProvider(tempRoot)
            val configManager =
                DesktopConfigManager(
                    FilePersist.createOneFilePersist(appPathProvider.resolve("appConfig.json", AppFileType.USER)),
                    DesktopLocaleUtils,
                )
            val desktopModule =
                DesktopModule(
                    appEnv = AppEnv.TEST,
                    appMetadataRepository =
                        AppMetadataRepository(
                            FilePersist.createOneFilePersist(appPathProvider.resolve(".metadata", AppFileType.USER)),
                            DesktopDeviceUtils(platform),
                        ),
                    appPathProvider = appPathProvider,
                    configManager = configManager,
                    clipFlowLogger = mockk<ClipFlowLogger>(relaxed = true),
                    deviceUtils = DesktopDeviceUtils(platform),
                    klogger = KotlinLogging.logger {},
                    platform = platform,
                    headless = true,
                )

            val testOverrides =
                module {
                    single<SecureStore> { mockk(relaxed = true) }
                    single<NetworkInterfaceService> { TestNetworkInterfaceService() }
                }
            val koinApplication =
                koinApplication {
                    allowOverride(true)
                    modules(desktopModule.modules() + testOverrides)
                }
            val koin = koinApplication.koin
            var pasteboardService: PasteboardService? = null
            var syncManager: SyncManager? = null
            var server: Server? = null
            var cliServer: CliServer? = null
            var mcpServer: McpServer? = null
            var pasteClient: PasteClient? = null
            var pasteBonjourService: PasteBonjourService? = null
            var cleanScheduler: CleanScheduler? = null
            var renderingService: RenderingService<String>? = null
            var taskExecutor: TaskExecutor? = null
            var resourcesClient: ResourcesClient? = null
            var driverFactory: DriverFactory? = null

            try {
                // The startApplication() service set on the headless path.
                assertNotNull(koin.get<DesktopConfigManager>())
                assertNotNull(koin.get<NotificationManager>())
                pasteboardService = koin.get()
                assertNotNull(pasteboardService)
                assertNotNull(koin.get<QRCodeGenerator>())
                syncManager = koin.get()
                assertNotNull(syncManager)
                assertNotNull(koin.get<PastePullService>())
                server = koin.get()
                assertNotNull(server)
                cliServer = koin.get()
                assertNotNull(cliServer)
                mcpServer = koin.get()
                assertNotNull(mcpServer)
                pasteClient = koin.get()
                assertNotNull(pasteClient)
                pasteBonjourService = koin.get()
                assertNotNull(pasteBonjourService)
                cleanScheduler = koin.get()
                assertNotNull(cleanScheduler)
                assertNotNull(koin.get<DesktopPidFileService>())
                renderingService = koin.get(named("urlRendering"))
                assertNotNull(renderingService)

                // The shutdownAllServices()/runHeadless() service set on the headless path.
                assertNotNull(koin.get<AppExitService>())
                assertNotNull(koin.get<AppLock>())
                taskExecutor = koin.get()
                assertNotNull(taskExecutor)
                assertNotNull(koin.get<UserDataPathProvider>())
                resourcesClient = koin.get()
                assertNotNull(resourcesClient)
                driverFactory = koin.get()
                assertNotNull(driverFactory)
            } finally {
                runCatching { cleanScheduler?.stop() }
                runCatching { taskExecutor?.shutdown() }
                runCatching { renderingService?.stop() }
                runCatching { pasteboardService?.stop() }
                runCatching { pasteBonjourService?.close() }
                runCatching { server?.stop() }
                runCatching { cliServer?.stop() }
                runCatching { mcpServer?.stop() }
                runCatching { syncManager?.stop() }
                runCatching { pasteClient?.close() }
                runCatching { resourcesClient?.close() }
                runCatching { driverFactory?.closeDriver() }
                koinApplication.close()
                Files.walk(tempRoot.toNioPath()).use { paths ->
                    paths.sorted(Comparator.reverseOrder()).forEach(Files::deleteIfExists)
                }
            }
        }
}
