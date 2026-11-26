package com.clipflow.net

import com.clipflow.app.AppInfo
import com.clipflow.app.AppTokenApi
import com.clipflow.config.CommonConfigManager
import com.clipflow.net.exception.ExceptionHandler
import com.clipflow.net.plugin.ServerDecryptionPluginFactory
import com.clipflow.net.plugin.ServerEncryptPluginFactory
import com.clipflow.net.routing.SyncRoutingApi
import com.clipflow.net.routing.pairingV3Routing
import com.clipflow.net.routing.syncRouting
import com.clipflow.pairing.v3.PairingProtocolV3Service
import com.clipflow.pairing.v3.PairingVersionCoordinator
import com.clipflow.pairing.v3.WindowOpenSource
import com.clipflow.secure.SecureKeyPairSerializer
import com.clipflow.secure.SecureStore
import com.clipflow.sync.MarketingNearbyDeviceManager
import com.clipflow.sync.NearbyDeviceManager
import com.clipflow.sync.PendingKeyExchangeStore
import com.clipflow.utils.getJsonUtils
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*

class TestServerModule(
    private val appInfo: AppInfo,
    private val appTokenApi: AppTokenApi,
    private val configManager: CommonConfigManager,
    private val exceptionHandler: ExceptionHandler,
    private val nearbyDeviceManager: NearbyDeviceManager = MarketingNearbyDeviceManager(),
    private val networkInterfaceService: NetworkInterfaceService,
    private val pendingKeyExchangeStore: PendingKeyExchangeStore,
    private val secureKeyPairSerializer: SecureKeyPairSerializer,
    private val secureStore: SecureStore,
    private val serverEncryptPluginFactory: ServerEncryptPluginFactory,
    private val serverDecryptionPluginFactory: ServerDecryptionPluginFactory,
    private val syncApi: SyncApi,
    private val syncInfoFactory: SyncInfoFactory,
    private val syncRoutingApi: SyncRoutingApi,
    private val pairingProtocolV3Service: PairingProtocolV3Service? = null,
    private val pairingVersionCoordinator: PairingVersionCoordinator = PairingVersionCoordinator(),
) : ServerModule {
    override fun installModules(): Application.() -> Unit =
        {
            install(ContentNegotiation) {
                json(getJsonUtils().JSON)
            }
            install(serverEncryptPluginFactory.createPlugin())
            install(serverDecryptionPluginFactory.createPlugin())
            routing {
                syncRouting(
                    appInfo,
                    appTokenApi,
                    configManager,
                    exceptionHandler,
                    nearbyDeviceManager,
                    networkInterfaceService,
                    pendingKeyExchangeStore,
                    secureKeyPairSerializer,
                    secureStore,
                    syncApi,
                    syncInfoFactory,
                    syncRoutingApi,
                    { _, _, _ -> },
                    { appInstanceId ->
                        if (pendingKeyExchangeStore.remove(appInstanceId)) {
                            appTokenApi.releaseVerifier(appInstanceId)
                        }
                    },
                    pairingVersionCoordinator,
                    { appInstanceId ->
                        pairingProtocolV3Service?.hasActiveSession(appInstanceId) ?: false
                    },
                    { pairingProtocolV3Service?.acceptanceWindow?.open(WindowOpenSource.REMOTE) ?: true },
                )
                pairingProtocolV3Service?.let { service ->
                    pairingV3Routing(
                        service,
                        pairingVersionCoordinator,
                        { appInstanceId ->
                            if (pendingKeyExchangeStore.remove(appInstanceId)) {
                                appTokenApi.releaseVerifier(appInstanceId)
                            }
                        },
                    ) { _, _, _ -> }
                }
            }
        }
}
