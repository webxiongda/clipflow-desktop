package com.clipflow

import com.clipflow.app.AppEnv
import com.clipflow.cli.CliEndpointFile
import com.clipflow.cli.CliPairingService
import com.clipflow.cli.CliServer
import com.clipflow.cli.devCliEndpointPointerPath
import com.clipflow.config.DesktopConfigManager
import com.clipflow.image.DesktopFaviconLoader
import com.clipflow.image.FaviconLoader
import com.clipflow.mcp.DesktopMcpServer
import com.clipflow.mcp.McpPasteItemFormatter
import com.clipflow.mcp.McpResourceProvider
import com.clipflow.mcp.McpServer
import com.clipflow.mcp.McpToolProvider
import com.clipflow.net.DesktopNetworkInterfaceService
import com.clipflow.net.DesktopNetworkProfileService
import com.clipflow.net.DesktopPasteBonjourService
import com.clipflow.net.DesktopPasteServer
import com.clipflow.net.DesktopResourcesClient
import com.clipflow.net.DesktopServerFactory
import com.clipflow.net.DesktopServerModule
import com.clipflow.net.LinuxNetworkStateMonitor
import com.clipflow.net.MacosNetworkStateMonitor
import com.clipflow.net.NetworkInterfaceService
import com.clipflow.net.NetworkProfileService
import com.clipflow.net.NetworkStateMonitor
import com.clipflow.net.NoopNetworkStateMonitor
import com.clipflow.net.PasteBonjourService
import com.clipflow.net.PasteClient
import com.clipflow.net.ResourcesClient
import com.clipflow.net.Server
import com.clipflow.net.ServerFactory
import com.clipflow.net.ServerModule
import com.clipflow.net.SyncApi
import com.clipflow.net.SyncInfoAdvertiser
import com.clipflow.net.TelnetHelper
import com.clipflow.net.WindowsNetworkStateMonitor
import com.clipflow.net.clientapi.PairingV3ClientApi
import com.clipflow.net.clientapi.PairingV3Transport
import com.clipflow.net.clientapi.PasteClientApi
import com.clipflow.net.clientapi.PullClientApi
import com.clipflow.net.clientapi.PushClientApi
import com.clipflow.net.clientapi.SyncClientApi
import com.clipflow.net.exception.DesktopExceptionHandler
import com.clipflow.net.exception.ExceptionHandler
import com.clipflow.net.routing.SyncRoutingApi
import com.clipflow.net.ws.WS_MAX_PAYLOAD_SIZE
import com.clipflow.net.ws.WsClientConnector
import com.clipflow.net.ws.WsMessageHandler
import com.clipflow.net.ws.WsSessionManager
import com.clipflow.pairing.v3.LibCryptoResolution
import com.clipflow.pairing.v3.OpenSslPakeEcOps
import com.clipflow.pairing.v3.PairingAcceptanceWindow
import com.clipflow.pairing.v3.PairingCapabilityFlag
import com.clipflow.pairing.v3.PairingProtocolV3Service
import com.clipflow.pairing.v3.PairingRateLimiter
import com.clipflow.pairing.v3.PairingReceiptCache
import com.clipflow.pairing.v3.PairingSessionStore
import com.clipflow.pairing.v3.PairingV3
import com.clipflow.pairing.v3.PairingVersionCoordinator
import com.clipflow.pairing.v3.PakeProvider
import com.clipflow.pairing.v3.Spake2PakeProvider
import com.clipflow.pairing.v3.UnavailablePakeProvider
import com.clipflow.path.AppPathProvider
import com.clipflow.platform.Platform
import com.clipflow.sync.FilePushService
import com.clipflow.sync.GeneralNearbyDeviceManager
import com.clipflow.sync.GeneralSyncManager
import com.clipflow.sync.MarketingNearbyDeviceManager
import com.clipflow.sync.MarketingSyncManager
import com.clipflow.sync.NearbyDeviceManager
import com.clipflow.sync.PendingExchangeLedger
import com.clipflow.sync.PendingKeyExchangeStore
import com.clipflow.sync.PushSessionManager
import com.clipflow.sync.SharePushOrchestrator
import com.clipflow.sync.SyncDeviceManager
import com.clipflow.sync.SyncManager
import com.clipflow.sync.SyncResolver
import com.clipflow.sync.SyncResolverApi
import com.clipflow.ui.devices.DefaultPairingV3UiController
import com.clipflow.ui.devices.PairingV3UiController
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.server.cio.CIOApplicationEngine
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun desktopNetworkModule(
    marketingMode: Boolean,
    headless: Boolean,
): Module =
    module {
        // region CLI
        single<CliEndpointFile> { CliEndpointFile(get(), devCliEndpointPointerPath(get())) }
        single<CliPairingService> {
            CliPairingService(
                appTokenApi = get(),
                nearbyDeviceManager = get(),
                pendingKeyExchangeStore = get(),
                pairingCapabilityFlag = get(),
                pairingV3UiController = get(),
                pasteBonjourService = get(),
                syncManager = get(),
            )
        }
        single<CliServer> {
            CliServer(
                appInfo = get(),
                cliPairingService = get(),
                configManager = get(),
                cliEndpointFile = get(),
                nearbyDeviceManager = get(),
                supportsPasteCopy = !headless,
                pasteboardService = get(),
                pasteDao = get(),
                pasteDataHelper = get(),
                pasteItemReader = get(),
                pasteReleaseService = get(),
                pasteTagDao = get(),
                platform = get(),
                searchContentService = get(),
                syncManager = get(),
                syncRuntimeInfoDao = get(),
                pasteContentEditor = get(),
                taskSubmitter = get(),
                userDataPathProvider = get(),
            )
        }
        // endregion

        // region MCP
        single<McpPasteItemFormatter> { McpPasteItemFormatter(get(), get(), get()) }
        single<McpResourceProvider> { McpResourceProvider(get(), get()) }
        single<McpServer> {
            DesktopMcpServer(
                mcpPort = (get<DesktopConfigManager>().getCurrentConfig()).mcpServerPort,
                mcpToolProvider = get(),
                mcpResourceProvider = get(),
            )
        }
        single<McpToolProvider> { McpToolProvider(get(), get(), get(), get(), get(), get()) }
        // endregion

        // region Network core
        single<ExceptionHandler> { DesktopExceptionHandler() }
        single<NetworkInterfaceService> { DesktopNetworkInterfaceService(get(), get()) }
        single<NetworkProfileService> { DesktopNetworkProfileService(get(), get(), get(), get()) }
        single<NetworkStateMonitor> {
            val platform = get<Platform>()
            when {
                platform.isMacos() -> MacosNetworkStateMonitor()
                platform.isWindows() -> WindowsNetworkStateMonitor()
                platform.isLinux() -> LinuxNetworkStateMonitor()
                else -> NoopNetworkStateMonitor()
            }
        }
        single<PasteBonjourService> { DesktopPasteBonjourService(get(), get(), get(), get()) }
        single<SyncInfoAdvertiser> { SyncInfoAdvertiser(get(), get()) }
        single<TelnetHelper> { TelnetHelper(get(), get(), get(), get()) }
        // endregion

        // region HTTP client & API
        single<FaviconLoader> { DesktopFaviconLoader(get(), get(), get()) }
        single<PairingV3ClientApi> { PairingV3ClientApi(get(), get(), get()) }
        single<PairingV3Transport> { get<PairingV3ClientApi>() }
        single<PasteClient> { PasteClient(get(), get(), get()) }
        single<PasteClientApi> { PasteClientApi(get(), get()) }
        single<PullClientApi> { PullClientApi(get(), get(), get()) }
        single<PushClientApi> { PushClientApi(get(), get(), get()) }
        single<ResourcesClient> { DesktopResourcesClient(get(), get()) }
        single<SyncClientApi> { SyncClientApi(get(), get(), get(), get(), get()) }
        // endregion

        // region Server
        single<Server> {
            DesktopPasteServer(
                get(named("readWritePort")),
                get(),
                get<ServerFactory<CIOApplicationEngine, CIOApplicationEngine.Configuration>>(),
                get(),
            )
        }
        single<ServerFactory<CIOApplicationEngine, CIOApplicationEngine.Configuration>> {
            DesktopServerFactory()
        }
        single<ServerModule> {
            DesktopServerModule(
                appControl = get(),
                appInfo = get(),
                appTokenApi = get(),
                cacheManager = get(),
                configManager = get(),
                exceptionHandler = get(),
                nearbyDeviceManager = get(),
                networkInterfaceService = get(),
                pairingProtocolV3Service = get(),
                pairingVersionCoordinator = get(),
                pendingKeyExchangeStore = get(),
                pasteboardService = get(),
                pasteDao = get(),
                pasteReleaseService = get(),
                pushSessionManager = get(),
                secureKeyPairSerializer = get(),
                secureStore = get(),
                syncApi = get(),
                syncInfoFactory = get(),
                syncRoutingApi = get(),
                serverEncryptPluginFactory = get(),
                serverDecryptionPluginFactory = get(),
                userDataPathProvider = get(),
                wsMessageHandler = get(),
                wsSessionManager = get(),
            )
        }
        single<SyncApi> { SyncApi }
        single<SyncRoutingApi> { get<SyncManager>() }
        // endregion

        // region Pairing v3
        single<PairingAcceptanceWindow> { PairingAcceptanceWindow() }
        // Despite the ui.devices package, this controller has no Compose
        // dependency; it lives here so the headless daemon (which swaps the UI
        // module for stubs) can drive initiator-side pairing through the CLI
        single<PairingV3UiController> { DefaultPairingV3UiController(get(), get()) }
        single<PairingProtocolV3Service> {
            val pairingCapabilityFlag = get<PairingCapabilityFlag>()
            PairingProtocolV3Service(
                appInfo = get(),
                pairingV3ClientApi = get(),
                pakeProvider = get(),
                receiptCache = get(),
                rateLimiter = get(),
                secureKeyPairSerializer = get(),
                secureStore = get(),
                sessionStore = get(),
                acceptanceWindow = get(),
                isPairingV3Enabled = { pairingCapabilityFlag.isPairingV3Enabled },
            )
        }
        single<PairingRateLimiter> { PairingRateLimiter() }
        single<PairingReceiptCache> { PairingReceiptCache() }
        single<PairingSessionStore> { PairingSessionStore() }
        single<PairingVersionCoordinator> { PairingVersionCoordinator() }
        single<PakeProvider> { get<DesktopPairingBackend>().pakeProvider }
        // endregion

        // region WebSocket
        single<WsClientConnector> {
            val wsHttpClient =
                HttpClient(CIO) {
                    install(WebSockets) {
                        pingIntervalMillis = 30_000
                        // Receive-side compatibility cap, not an expansion of the
                        // native pairing v2 send contract: legacy peers may still
                        // push a whole paste as one oversized frame, which must keep
                        // being deliverable, but the buffer a paired peer can force
                        // into memory has to stay bounded.
                        maxFrameSize = WS_MAX_PAYLOAD_SIZE
                    }
                }
            WsClientConnector(
                appInfo = get(),
                client = wsHttpClient,
                secureStore = get(),
                wsSessionManager = get(),
                wsMessageHandler = get(),
            )
        }
        single<WsMessageHandler> {
            WsMessageHandler(
                lazyAppControl = lazy { get() },
                lazyCacheManager = lazy { get() },
                lazyPasteDao = lazy { get() },
                lazyPasteboardService = lazy { get() },
                lazySyncRoutingApi = lazy { get() },
                secureStore = get(),
                userDataPathProvider = get(),
                wsSessionManager = get(),
            )
        }
        single<WsSessionManager> {
            WsSessionManager()
        }
        // endregion

        // region Sync & devices
        single<FilePushService> {
            FilePushService(
                pasteSyncProcessManager = get(),
                pushClientApi = get(),
                userDataPathProvider = get(),
            )
        }
        single<NearbyDeviceManager> {
            if (marketingMode) {
                MarketingNearbyDeviceManager()
            } else {
                GeneralNearbyDeviceManager(get(), get(), get(), get())
            }
        }
        single<PendingExchangeLedger> { PendingExchangeLedger() }
        single<PendingKeyExchangeStore> { PendingKeyExchangeStore() }
        single<PushSessionManager> {
            PushSessionManager(
                pasteDao = get(),
                pasteboardService = get(),
                userDataPathProvider = get(),
            )
        }
        single<SharePushOrchestrator> { SharePushOrchestrator(get(), get(), get()) }
        single<SyncDeviceManager> {
            SyncDeviceManager(
                pastePullCursorManager = get(),
                pendingExchangeLedger = get(),
                secureStore = get(),
                syncClientApi = get(),
                syncRuntimeInfoDao = get(),
                wsSessionManager = get(),
            )
        }
        single<SyncManager> {
            if (marketingMode) {
                MarketingSyncManager()
            } else {
                GeneralSyncManager(
                    pendingExchangeLedger = get(),
                    syncResolver = get(),
                    syncRuntimeInfoDao = get(),
                    syncClientApi = get(),
                    wsSessionManager = get(),
                    pairingCapabilityFlag = get(),
                )
            }
        }
        single<SyncResolverApi> {
            SyncResolver(
                appInfo = get(),
                localPlatform = get(),
                lazyPasteBonjourService = lazy { get() },
                networkInterfaceService = get(),
                pendingExchangeLedger = get(),
                ratingPromptManager = get(),
                secureKeyPairSerializer = get(),
                secureStore = get(),
                syncClientApi = get(),
                syncDeviceManager = get(),
                syncInfoFactory = get(),
                syncRuntimeInfoDao = get(),
                telnetHelper = get(),
                tokenCache = get(),
                wsClientConnector = get(),
                wsSessionManager = get(),
            )
        }
        // endregion
    }

/**
 * The advertised pairing capability and the PAKE backend, resolved TOGETHER as
 * one immutable process-level snapshot. Keeping them in one value makes the
 * split-brain state unrepresentable where a device advertises pairing v3 while
 * its PAKE backend is unavailable — peers would select V3_PIN and then fail at
 * the provider instead of negotiating v2.
 */
class DesktopPairingBackend(
    val capabilityFlag: PairingCapabilityFlag,
    val pakeProvider: PakeProvider,
)

internal fun resolveDesktopPairingBackend(
    appEnv: AppEnv,
    developmentV3InteropEnabled: Boolean,
    libcryptoResolution: LibCryptoResolution = LibCryptoResolution.Environment,
    loadPakeProvider: () -> PakeProvider = { Spake2PakeProvider(OpenSslPakeEcOps.load(libcryptoResolution)) },
): DesktopPairingBackend {
    // Pairing v3 is rolled out (PAIRING_VERSION = 3, Phase C review passed).
    // Exactly one probe decides BOTH the advertised capability and the
    // registered provider, so the split-brain state "advertise 3 but the PAKE
    // backend is unavailable" stays unrepresentable:
    // - production/beta load only the bundled libcrypto (LibCryptoResolution
    //   .Bundled, fail-closed, no system fallback — #4865);
    // - DEVELOPMENT loads from the environment, gated by the interop flag
    //   (default on; opting out forces the v2 SAS path for testing);
    // - TEST never loads implicitly — suites that exercise v3 inject their
    //   provider explicitly.
    val attemptLoad =
        libcryptoResolution is LibCryptoResolution.Bundled ||
            (appEnv == AppEnv.DEVELOPMENT && developmentV3InteropEnabled)
    if (attemptLoad) {
        runCatching { loadPakeProvider() }
            .onSuccess { pakeProvider ->
                return DesktopPairingBackend(
                    PairingCapabilityFlag(SyncApi.PAIRING_VERSION),
                    pakeProvider,
                )
            }.onFailure { e ->
                KotlinLogging.logger {}.warn(e) {
                    "OpenSSL libcrypto unavailable, pairing v3 stays disabled; " +
                        "advertising v2 so peers negotiate SAS pairing"
                }
            }
    }
    // Fail-closed fallback: no reviewed backend loaded, so advertise the
    // highest version that works without one (v2 SAS).
    val fallbackVersion = PairingV3.PROTOCOL_VERSION - 1
    return DesktopPairingBackend(PairingCapabilityFlag(fallbackVersion), UnavailablePakeProvider)
}

/**
 * The libcrypto resolution for this build: [LibCryptoResolution.Bundled] in
 * PRODUCTION/BETA, [LibCryptoResolution.Environment] in DEVELOPMENT/TEST.
 *
 * Conveyor moves the bare library inputs (conveyor.conf, pinned in
 * app/openssl.yaml) into the packaged JVM's native-library directory — the
 * same directory `skiko.library.path` points at and [AppPathProvider.pasteAppExePath]
 * resolves on every platform (macOS `runtime/Contents/Home/lib`, Windows
 * `bin`, Linux `runtime/lib`).
 */
internal fun desktopLibcryptoResolution(
    appEnv: AppEnv,
    platform: Platform,
    appPathProvider: AppPathProvider,
): LibCryptoResolution {
    if (appEnv != AppEnv.PRODUCTION && appEnv != AppEnv.BETA) {
        return LibCryptoResolution.Environment
    }
    val libName =
        when {
            platform.isMacos() -> "libcrypto.3.dylib"
            platform.isWindows() -> "libcrypto-3-x64.dll"
            else -> "libcrypto.so.3"
        }
    return LibCryptoResolution.Bundled(
        appPathProvider.pasteAppExePath
            .resolve(libName)
            .toString(),
    )
}
