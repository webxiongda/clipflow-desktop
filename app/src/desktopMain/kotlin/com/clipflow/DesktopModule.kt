package com.clipflow

import com.clipflow.app.AppEnv
import com.clipflow.config.AppMetadataRepository
import com.clipflow.config.DesktopConfigManager
import com.clipflow.config.DevConfig
import com.clipflow.db.DesktopDriverFactory
import com.clipflow.db.DriverFactory
import com.clipflow.db.createDatabase
import com.clipflow.db.paste.PasteDao
import com.clipflow.db.paste.PasteTagDao
import com.clipflow.db.paste.QueryPasteTag
import com.clipflow.db.paste.SearchPasteData
import com.clipflow.db.paste.SqlPasteDao
import com.clipflow.db.paste.SqlPasteTagDao
import com.clipflow.db.plugin.PluginStateDao
import com.clipflow.db.secure.SecureIO
import com.clipflow.db.secure.SqlSecureDao
import com.clipflow.db.sync.SqlSyncRuntimeInfoDao
import com.clipflow.db.sync.SyncRuntimeInfoDao
import com.clipflow.db.task.SqlTaskDao
import com.clipflow.db.task.TaskDao
import com.clipflow.headless.headlessUiModule
import com.clipflow.headless.headlessViewModelModule
import com.clipflow.image.OCRModule
import com.clipflow.log.ClipFlowLogger
import com.clipflow.module.ocr.DesktopOCRModule
import com.clipflow.net.plugin.ClientDecryptPlugin
import com.clipflow.net.plugin.ClientEncryptPlugin
import com.clipflow.net.plugin.ServerDecryptionPluginFactory
import com.clipflow.net.plugin.ServerEncryptPluginFactory
import com.clipflow.paste.plugin.type.ColorTypePlugin
import com.clipflow.paste.plugin.type.DesktopColorTypePlugin
import com.clipflow.paste.plugin.type.DesktopFilesTypePlugin
import com.clipflow.paste.plugin.type.DesktopHtmlTypePlugin
import com.clipflow.paste.plugin.type.DesktopImageTypePlugin
import com.clipflow.paste.plugin.type.DesktopRtfTypePlugin
import com.clipflow.paste.plugin.type.DesktopTextTypePlugin
import com.clipflow.paste.plugin.type.DesktopUrlTypePlugin
import com.clipflow.paste.plugin.type.FilesTypePlugin
import com.clipflow.paste.plugin.type.HtmlTypePlugin
import com.clipflow.paste.plugin.type.ImageTypePlugin
import com.clipflow.paste.plugin.type.RtfTypePlugin
import com.clipflow.paste.plugin.type.TextTypePlugin
import com.clipflow.paste.plugin.type.UrlTypePlugin
import com.clipflow.path.AppPathProvider
import com.clipflow.platform.Platform
import com.clipflow.secure.DesktopSecureStoreFactory
import com.clipflow.secure.SecureKeyPairSerializer
import com.clipflow.secure.SecureStore
import com.clipflow.secure.SecureStoreFactory
import com.clipflow.ui.model.GeneralPasteSearchViewModel
import com.clipflow.ui.model.MarketingPasteData
import com.clipflow.ui.model.MarketingPasteSearchViewModel
import com.clipflow.ui.model.PastePanelViewModel
import com.clipflow.ui.model.PasteSearchViewModel
import com.clipflow.ui.model.PasteSelectionViewModel
import com.clipflow.ui.model.ScreenshotPreviewViewModel
import com.clipflow.utils.DeviceUtils
import com.clipflow.utils.getAppEnvUtils
import io.github.oshai.kotlinlogging.KLogger
import org.koin.core.KoinApplication
import org.koin.core.context.GlobalContext
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

class DesktopModule(
    private val appEnv: AppEnv,
    private val appMetadataRepository: AppMetadataRepository,
    private val appPathProvider: AppPathProvider,
    private val configManager: DesktopConfigManager,
    private val clipFlowLogger: ClipFlowLogger,
    private val deviceUtils: DeviceUtils,
    private val klogger: KLogger,
    private val platform: Platform,
    private val headless: Boolean = false,
) : ClipFlowModule {

    val marketingMode = getAppEnvUtils().isDevelopment() && DevConfig.marketingMode

    private val pairingBackend =
        resolveDesktopPairingBackend(
            appEnv = appEnv,
            developmentV3InteropEnabled =
                appEnv == AppEnv.DEVELOPMENT && DevConfig.pairingV3InteropEnabled,
            libcryptoResolution = desktopLibcryptoResolution(appEnv, platform, appPathProvider),
        )

    override fun appModule() =
        desktopAppModule(
            appEnv,
            appMetadataRepository,
            appPathProvider,
            configManager,
            clipFlowLogger,
            deviceUtils,
            klogger,
            platform,
            pairingBackend,
        )

    override fun extensionModule() =
        module {
            single { DesktopOCRModule(get(), get(), get(), get(), get()) } bind OCRModule::class
        }

    // SqlDelight
    override fun sqlDelightModule() =
        module {
            single<Database> { createDatabase(get()) }
            single<DriverFactory> { DesktopDriverFactory(get()) }
            single<PasteDao> {
                SqlPasteDao(
                    appInfo = get(),
                    database = get(),
                    pasteItemReader = get(),
                    searchContentService = get(),
                    taskSubmitter = get(),
                    userDataPathProvider = get(),
                )
            }
            single<PasteTagDao> { SqlPasteTagDao(get()) }
            single<PluginStateDao> { PluginStateDao(get()) }
            single<QueryPasteTag> { get<PasteTagDao>() }
            single<SearchPasteData> { get<PasteDao>() }
            single<SecureIO> { SqlSecureDao(get()) }
            single<SyncRuntimeInfoDao> { SqlSyncRuntimeInfoDao(get()) }
            single<TaskDao> { SqlTaskDao(get()) }
        }

    override fun networkModule() = desktopNetworkModule(marketingMode, headless)

    // SecurityModule
    override fun securityModule() =
        module {
            single<ClientDecryptPlugin> { ClientDecryptPlugin(get()) }
            single<ClientEncryptPlugin> { ClientEncryptPlugin(get()) }
            single<SecureKeyPairSerializer> { SecureKeyPairSerializer() }
            single<SecureStore> { get<SecureStoreFactory>().createSecureStore() }
            single<SecureStoreFactory> { DesktopSecureStoreFactory(get(), get(), get(), get(), get()) }
            single<ServerDecryptionPluginFactory> { ServerDecryptionPluginFactory(get()) }
            single<ServerEncryptPluginFactory> { ServerEncryptPluginFactory(get()) }
        }

    // PasteTypePluginModule
    override fun pasteTypePluginModule() =
        module {
            single<ColorTypePlugin> { DesktopColorTypePlugin() }
            single<FilesTypePlugin> { DesktopFilesTypePlugin(get(), get(), get(), get()) }
            single<HtmlTypePlugin> { DesktopHtmlTypePlugin(get()) }
            single<ImageTypePlugin> { DesktopImageTypePlugin(get(), get(), get(), get()) }
            single<RtfTypePlugin> { DesktopRtfTypePlugin() }
            single<TextTypePlugin> { DesktopTextTypePlugin() }
            single<UrlTypePlugin> { DesktopUrlTypePlugin(get()) }
        }

    override fun pasteComponentModule() = desktopPasteComponentModule(headless)

    override fun uiModule() = desktopUiModule()

    // ViewModelModule
    override fun viewModelModule() =
        module {
            single<MarketingPasteData> { MarketingPasteData(get(), get()) }
            single<PasteSearchViewModel> {
                if (marketingMode) {
                    MarketingPasteSearchViewModel(get())
                } else {
                    GeneralPasteSearchViewModel(get(), get(), get())
                }
            }
            single<PastePanelViewModel> { PastePanelViewModel(get(), get(), get()) }
            single<ScreenshotPreviewViewModel> { ScreenshotPreviewViewModel(get(), get()) }
            single<PasteSelectionViewModel> { PasteSelectionViewModel(get(), get(), get()) }
        }

    fun modules(): List<Module> =
        if (headless) {
            listOf(
                appModule(),
                extensionModule(),
                sqlDelightModule(),
                networkModule(),
                securityModule(),
                pasteTypePluginModule(),
                pasteComponentModule(),
                headlessUiModule(),
                headlessViewModelModule(),
            )
        } else {
            listOf(
                appModule(),
                extensionModule(),
                sqlDelightModule(),
                networkModule(),
                securityModule(),
                pasteTypePluginModule(),
                pasteComponentModule(),
                uiModule(),
                viewModelModule(),
            )
        }

    fun initKoinApplication(): KoinApplication =
        GlobalContext.startKoin {
            modules(this@DesktopModule.modules())
        }
}
