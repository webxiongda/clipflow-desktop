package com.clipflow.net

import com.clipflow.app.AppInfo
import com.clipflow.app.EndpointInfoFactory
import com.clipflow.app.TestAppTokenService
import com.clipflow.config.TestAppConfig
import com.clipflow.config.TestConfigManager
import com.clipflow.config.TestReadWritePort
import com.clipflow.db.secure.MemorySecureIO
import com.clipflow.net.clientapi.PairingV3ClientApi
import com.clipflow.net.clientapi.PairingV3Transport
import com.clipflow.net.clientapi.SyncClientApi
import com.clipflow.net.exception.DesktopExceptionHandler
import com.clipflow.net.exception.ExceptionHandler
import com.clipflow.net.plugin.ClientDecryptPlugin
import com.clipflow.net.plugin.ClientEncryptPlugin
import com.clipflow.net.plugin.ServerDecryptionPluginFactory
import com.clipflow.net.plugin.ServerEncryptPluginFactory
import com.clipflow.net.routing.TestSyncRoutingApi
import com.clipflow.pairing.v3.PairingAcceptanceWindow
import com.clipflow.pairing.v3.PairingProtocolV3Service
import com.clipflow.pairing.v3.PairingRateLimiter
import com.clipflow.pairing.v3.PairingReceiptCache
import com.clipflow.pairing.v3.PairingSessionStore
import com.clipflow.pairing.v3.PairingV3
import com.clipflow.pairing.v3.PairingV3TelemetryObserver
import com.clipflow.pairing.v3.PairingVersionCoordinator
import com.clipflow.pairing.v3.PakeProvider
import com.clipflow.pairing.v3.TestPakeProvider
import com.clipflow.platform.Platform
import com.clipflow.secure.GeneralSecureStore
import com.clipflow.secure.SecureKeyPair
import com.clipflow.secure.SecureKeyPairSerializer
import com.clipflow.secure.SecureStore
import com.clipflow.sync.PendingKeyExchangeStore
import com.clipflow.utils.CryptographyUtils.generateSecureKeyPair
import com.clipflow.utils.DesktopDeviceUtils
import com.clipflow.utils.DeviceUtils
import com.clipflow.utils.getPlatformUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class TestInstance(
    val appInstanceId: String,
    userName: String = appInstanceId,
    pairingPinLifetime: Duration = PairingV3.DEFAULT_PIN_LIFETIME,
    pairingGenerationGrace: Duration = PairingV3.DEFAULT_GENERATION_GRACE,
    pairingSessionTtl: Duration = PairingV3.DEFAULT_SESSION_TTL,
    pairingRateLimiter: PairingRateLimiter = PairingRateLimiter(),
    pakeProvider: PakeProvider = TestPakeProvider(),
    pairingV3Enabled: Boolean = true,
    acceptanceWindowMaxProofFailures: Int = PairingV3.MAX_ACCEPTOR_PROOF_FAILURES,
    pairingTelemetryObserver: PairingV3TelemetryObserver = PairingV3TelemetryObserver.NOOP,
    // Wraps the real HTTP transport so a test can lose or alter individual calls.
    pairingV3Transport: (PairingV3Transport) -> PairingV3Transport = { it },
) {
    companion object {
        val platform: Platform = getPlatformUtils().platform
        val deviceUtils: DeviceUtils = DesktopDeviceUtils(platform)
        val secureKeyPairSerializer = SecureKeyPairSerializer()
        val syncApi: SyncApi = SyncApi
        val exceptionHandler: ExceptionHandler = DesktopExceptionHandler()
        val networkInterfaceService: NetworkInterfaceService =
            TestNetworkInterfaceService(
                testNetworkInterfaces =
                    listOf(
                        NetworkInterfaceInfo("lo0", 8, "127.0.0.1"),
                    ),
                testPreferredInterface = NetworkInterfaceInfo("lo0", 8, "127.0.0.1"),
            )
    }

    val appInfo =
        AppInfo(
            appInstanceId = appInstanceId,
            appVersion = "0.0.1",
            appRevision = "1",
            userName = userName,
        )

    val secureKeyPair: SecureKeyPair = runBlocking { generateSecureKeyPair() }
    val secureIO: MemorySecureIO = MemorySecureIO()

    val secureStore: SecureStore =
        GeneralSecureStore(secureKeyPair, secureKeyPairSerializer, secureIO)

    val appTokenApi = TestAppTokenService()

    private val readWritePort = TestReadWritePort()

    private val clientEncryptPlugin = ClientEncryptPlugin(secureStore)
    private val clientDecryptPlugin = ClientDecryptPlugin(secureStore)

    val pasteClient = PasteClient(appInfo, clientEncryptPlugin, clientDecryptPlugin)

    val syncRoutingApi: TestSyncRoutingApi = TestSyncRoutingApi()

    val pairingSessionStore = PairingSessionStore()

    val pairingAcceptanceWindow = PairingAcceptanceWindow(maxProofFailures = acceptanceWindowMaxProofFailures)

    val pairingReceiptCache = PairingReceiptCache()

    val pairingVersionCoordinator = PairingVersionCoordinator()

    val pairingV3ClientApi = PairingV3ClientApi(pasteClient, exceptionHandler)

    val pairingProtocolV3Service =
        PairingProtocolV3Service(
            appInfo = appInfo,
            pairingV3ClientApi = pairingV3Transport(pairingV3ClientApi),
            pakeProvider = pakeProvider,
            receiptCache = pairingReceiptCache,
            rateLimiter = pairingRateLimiter,
            secureKeyPairSerializer = secureKeyPairSerializer,
            secureStore = secureStore,
            sessionStore = pairingSessionStore,
            acceptanceWindow = pairingAcceptanceWindow,
            isPairingV3Enabled = { pairingV3Enabled },
            pinLifetime = pairingPinLifetime,
            generationGrace = pairingGenerationGrace,
            sessionTtl = pairingSessionTtl,
            telemetryObserver = pairingTelemetryObserver,
        )

    private val serverFactory =
        DesktopServerFactory()

    private val endpointInfoFactory =
        EndpointInfoFactory(deviceUtils, lazy { pasteServer }, platform)

    private val syncInfoFactory =
        SyncInfoFactory(appInfo, endpointInfoFactory)

    private val pendingKeyExchangeStore = PendingKeyExchangeStore()

    private val serverModule =
        TestServerModule(
            appInfo = appInfo,
            appTokenApi = appTokenApi,
            configManager = TestConfigManager(TestAppConfig()),
            exceptionHandler = exceptionHandler,
            networkInterfaceService = networkInterfaceService,
            pendingKeyExchangeStore = pendingKeyExchangeStore,
            secureKeyPairSerializer = secureKeyPairSerializer,
            secureStore = secureStore,
            serverEncryptPluginFactory = ServerEncryptPluginFactory(secureStore),
            serverDecryptionPluginFactory = ServerDecryptionPluginFactory(secureStore),
            syncApi = syncApi,
            syncInfoFactory = syncInfoFactory,
            syncRoutingApi = syncRoutingApi,
            pairingProtocolV3Service = pairingProtocolV3Service,
            pairingVersionCoordinator = pairingVersionCoordinator,
        )

    val pasteServer: Server =
        DesktopPasteServer(
            readWritePort,
            exceptionHandler,
            serverFactory,
            serverModule,
        )

    val syncClientApi =
        SyncClientApi(pasteClient, exceptionHandler, secureKeyPairSerializer, secureStore, syncApi)

    val telnetHelper = TelnetHelper(networkInterfaceService, pasteClient, syncApi, syncInfoFactory)

    suspend fun start() {
        pasteServer.start()
    }

    suspend fun stop() {
        pasteServer.stop()
        pasteClient.close()
    }

    fun getPort(): Int = pasteServer.port()

    fun getToken(): Int =
        appTokenApi.token.value
            .concatToString()
            .toInt()

    // Opens a pairing session (as the token dialog does) and returns its token.
    // The session stays open for the rest of the test instance's lifetime.
    suspend fun openPairingSession(): Int {
        appTokenApi.startRefresh(showToken = false)
        withTimeout(5.seconds) { appTokenApi.refresh.first { it } }
        return getToken()
    }
}
