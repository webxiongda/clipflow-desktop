package com.clipflow

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.memory.MemoryCache
import com.clipflow.app.AppControl
import com.clipflow.app.AppEnv
import com.clipflow.app.AppExitService
import com.clipflow.app.AppFileType
import com.clipflow.app.AppInfo
import com.clipflow.app.AppInfoFactory
import com.clipflow.app.AppLaunchState
import com.clipflow.app.AppLock
import com.clipflow.app.AppRestartService
import com.clipflow.app.AppStartUpService
import com.clipflow.app.AppUpdateService
import com.clipflow.app.AppUrls
import com.clipflow.app.ChangelogService
import com.clipflow.app.ClipFlowWebService
import com.clipflow.app.DesktopAppControl
import com.clipflow.app.DesktopAppExitService
import com.clipflow.app.DesktopAppInfoFactory
import com.clipflow.app.DesktopAppLaunch
import com.clipflow.app.DesktopAppLaunchState
import com.clipflow.app.DesktopAppRestartService
import com.clipflow.app.DesktopAppStartUpService
import com.clipflow.app.DesktopAppUpdateService
import com.clipflow.app.DesktopAppUrls
import com.clipflow.app.DesktopPidFileService
import com.clipflow.app.EndpointInfoFactory
import com.clipflow.app.NativeMessagingHostService
import com.clipflow.app.ResumableUpdateDownloader
import com.clipflow.app.UpdateMetadataFetcher
import com.clipflow.app.WindowsZipUpdater
import com.clipflow.cli.CliSymlinkService
import com.clipflow.cli.JnaWindowsUserPathManager
import com.clipflow.config.AppMetadataRepository
import com.clipflow.config.CommonConfigManager
import com.clipflow.config.DesktopConfigManager
import com.clipflow.config.DesktopSimpleConfigFactory
import com.clipflow.config.ReadWriteConfig
import com.clipflow.config.ReadWritePort
import com.clipflow.config.SimpleConfigFactory
import com.clipflow.image.DesktopFileExtLoader
import com.clipflow.image.DesktopImageHandler
import com.clipflow.image.DesktopVideoThumbnailLoader
import com.clipflow.image.FileExtImageLoader
import com.clipflow.image.ImageHandler
import com.clipflow.image.VideoThumbnailLoader
import com.clipflow.image.coil.AppSourceFactory
import com.clipflow.image.coil.AppSourceKeyer
import com.clipflow.image.coil.FaviconFactory
import com.clipflow.image.coil.GenerateImageFactory
import com.clipflow.image.coil.GenerateImageKeyer
import com.clipflow.image.coil.ImageKeyer
import com.clipflow.image.coil.ImageLoaderQualifiers
import com.clipflow.image.coil.UrlKeyer
import com.clipflow.image.coil.UserImageFactory
import com.clipflow.log.ClipFlowLogger
import com.clipflow.module.ModuleDownloadManager
import com.clipflow.module.ModuleManager
import com.clipflow.net.AbstractResourcesClient
import com.clipflow.net.ResourcesClient
import com.clipflow.net.Server
import com.clipflow.net.SyncInfoFactory
import com.clipflow.notification.NotificationCenter
import com.clipflow.pairing.v3.PairingCapabilityFlag
import com.clipflow.paste.CacheManager
import com.clipflow.paste.DesktopCacheManager
import com.clipflow.path.AppPathProvider
import com.clipflow.path.DesktopMigration
import com.clipflow.path.UserDataPathProvider
import com.clipflow.path.getPlatformPathProvider
import com.clipflow.platform.Platform
import com.clipflow.plugin.BundledPluginInstaller
import com.clipflow.plugin.PluginEnvironment
import com.clipflow.plugin.PluginRepository
import com.clipflow.plugin.findMissingExecutables
import com.clipflow.plugin.pluginExtraPathDirs
import com.clipflow.presist.FilePersist
import com.clipflow.share.AppShareService
import com.clipflow.share.DesktopAppShareService
import com.clipflow.sync.DesktopQRCodeGenerator
import com.clipflow.sync.QRCodeGenerator
import com.clipflow.ui.DesktopFontManager
import com.clipflow.ui.base.FontManager
import com.clipflow.utils.DesktopLocaleUtils
import com.clipflow.utils.DeviceUtils
import com.clipflow.utils.LocaleUtils
import com.clipflow.utils.getFileUtils
import io.github.oshai.kotlinlogging.KLogger
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.awt.image.BufferedImage

@Suppress("UNCHECKED_CAST")
fun desktopAppModule(
    appEnv: AppEnv,
    appMetadataRepository: AppMetadataRepository,
    appPathProvider: AppPathProvider,
    configManager: DesktopConfigManager,
    clipFlowLogger: ClipFlowLogger,
    deviceUtils: DeviceUtils,
    klogger: KLogger,
    platform: Platform,
    pairingBackend: DesktopPairingBackend,
): Module =
    module {
        // region App
        single<AppControl> { DesktopAppControl(get()) }
        single<AppEnv> { appEnv }
        single<AppExitService> { DesktopAppExitService }
        single<AppInfo> { get<AppInfoFactory>().createAppInfo() }
        single<AppInfoFactory> { DesktopAppInfoFactory(get(), get()) }
        single<AppLaunchState> { get<DesktopAppLaunchState>() }
        single<AppLock> { get<DesktopAppLaunch>() }
        single<AppRestartService> { DesktopAppRestartService(get(), get()) }
        single<AppStartUpService> { DesktopAppStartUpService(get(), get(), get(), get()) }
        single<AppUpdateService> { DesktopAppUpdateService(get(), get(), get(), get(), get(), get(), get(), get()) }
        single<AppUrls> { DesktopAppUrls }
        single<ChangelogService> { ChangelogService(get()) }
        single<CliSymlinkService> {
            val platform = get<Platform>()
            CliSymlinkService(
                appPathProvider = get(),
                platform = platform,
                windowsUserPath = if (platform.isWindows()) JnaWindowsUserPathManager() else null,
            )
        }
        single<ClipFlowWebService> { ClipFlowWebService(get(), get()) }
        single<DesktopAppLaunch> { DesktopAppLaunch(get(), get()) }
        single<DesktopAppLaunchState> { get<DesktopAppLaunch>().launchSync() }
        single<DesktopPidFileService> { DesktopPidFileService(get(), get()) }
        single<EndpointInfoFactory> { EndpointInfoFactory(get(), lazy { get<Server>() }, get()) }
        single<NativeMessagingHostService> { NativeMessagingHostService(get(), get(), get()) }
        single<ResumableUpdateDownloader> {
            ResumableUpdateDownloader(
                httpClient = { (get<ResourcesClient>() as AbstractResourcesClient).getHttpClient() },
            )
        }
        single<UpdateMetadataFetcher> { UpdateMetadataFetcher(get()) }
        single<WindowsZipUpdater> { WindowsZipUpdater(get(), get(), get(), get(), get(), get(), get(), get()) }
        // endregion

        // region Config
        single<AppMetadataRepository> { appMetadataRepository }
        single<CommonConfigManager> { configManager as CommonConfigManager }
        single<DesktopConfigManager> { configManager }
        single<DesktopPairingBackend> { pairingBackend }
        single<PairingCapabilityFlag> { pairingBackend.capabilityFlag }
        single<ReadWriteConfig<Int>>(named("readWritePort")) { ReadWritePort(get()) }
        single<SimpleConfigFactory> { DesktopSimpleConfigFactory(get()) }
        // endregion

        // region Image & Coil
        single<FileExtImageLoader> { DesktopFileExtLoader(get(), get(), get()) }
        single<ImageHandler<BufferedImage>> { DesktopImageHandler }
        single<ImageLoader>(ImageLoaderQualifiers.APP_SOURCE) {
            ImageLoader
                .Builder(get<PlatformContext>())
                .components {
                    add(AppSourceFactory(get()))
                        .add(AppSourceKeyer())
                }.memoryCache { get<MemoryCache>() }
                .build()
        }
        single<ImageLoader>(ImageLoaderQualifiers.FAVICON) {
            ImageLoader
                .Builder(get<PlatformContext>())
                .components {
                    add(FaviconFactory(get()))
                        .add(UrlKeyer())
                }.memoryCache { get<MemoryCache>() }
                .build()
        }
        single<ImageLoader>(ImageLoaderQualifiers.GENERATE_IMAGE) {
            ImageLoader
                .Builder(get<PlatformContext>())
                .components {
                    add(GenerateImageFactory(get()))
                        .add(GenerateImageKeyer())
                }.memoryCache { get<MemoryCache>() }
                .build()
        }
        single<ImageLoader>(ImageLoaderQualifiers.USER_IMAGE) {
            ImageLoader
                .Builder(get<PlatformContext>())
                .components {
                    add(UserImageFactory())
                        .add(ImageKeyer())
                }.memoryCache { get<MemoryCache>() }
                .build()
        }
        single<MemoryCache> {
            MemoryCache
                .Builder()
                .strongReferencesEnabled(true)
                .weakReferencesEnabled(true)
                .maxSizeBytes(256L * 1024L * 1024L)
                .build()
        }
        single<VideoThumbnailLoader> { DesktopVideoThumbnailLoader(get(), get()) }
        // endregion

        // region Module loader
        single<ModuleDownloadManager> { ModuleDownloadManager(get(), get()) }
        single<ModuleManager> {
            val ocrModule = get<com.clipflow.image.OCRModule>()
            ModuleManager(
                mapOf(
                    ocrModule.moduleId to ocrModule,
                ),
            )
        }
        // endregion

        // region Plugin
        single<BundledPluginInstaller> {
            BundledPluginInstaller(
                pluginsDirectory = { get<PluginRepository>().pluginsDirectory() },
                fileSystem = getFileUtils().fileSystem,
            )
        }
        single<PluginEnvironment> {
            PluginEnvironment(
                platform = get(),
                appVersion = get<AppInfo>().appVersion,
                runtimes = setOf(PluginEnvironment.RUNTIME_PROCESS),
            )
        }
        single<PluginRepository> {
            val platform = get<Platform>()
            val extraDirs = pluginExtraPathDirs(platform)
            PluginRepository(
                environment = get(),
                pluginStateDao = get(),
                userDataPathProvider = get(),
                fileSystem = getFileUtils().fileSystem,
                findMissingRequirements = { requires ->
                    findMissingExecutables(
                        requires = requires,
                        isWindows = platform.isWindows(),
                        extraDirs = extraDirs,
                    )
                },
            )
        }
        // endregion

        // region Path
        single<AppPathProvider> { appPathProvider }
        single<DesktopMigration> { DesktopMigration(get(), get(), get(), get()) }
        single<UserDataPathProvider> { UserDataPathProvider(get(), getPlatformPathProvider(get())) }
        // endregion

        // region Infra & misc
        single<AppShareService> { DesktopAppShareService(get(), get(), get(), get()) }
        single<CacheManager> { DesktopCacheManager(get(), get()) }
        single<ClipFlowLogger> { clipFlowLogger }
        single<DeviceUtils> { deviceUtils }
        single<FilePersist> { FilePersist }
        single<FontManager> { DesktopFontManager(get()) }
        single<KLogger> { klogger }
        single<LocaleUtils> { DesktopLocaleUtils }
        single {
            val path = appPathProvider.resolve("notifications.json", AppFileType.USER)
            NotificationCenter(FilePersist.createOneFilePersist(path))
        }
        single<Platform> { platform }
        single<QRCodeGenerator> { DesktopQRCodeGenerator(get(), get(), get()) }
        single<SyncInfoFactory> { SyncInfoFactory(get(), get()) }
        // endregion
    }
