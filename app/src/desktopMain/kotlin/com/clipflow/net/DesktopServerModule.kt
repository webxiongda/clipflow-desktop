package com.clipflow.net

import com.clipflow.app.AppControl
import com.clipflow.app.AppInfo
import com.clipflow.app.AppTokenApi
import com.clipflow.config.CommonConfigManager
import com.clipflow.db.paste.PasteDao
import com.clipflow.net.exception.ExceptionHandler
import com.clipflow.net.plugin.ServerDecryptionPluginFactory
import com.clipflow.net.plugin.ServerEncryptPluginFactory
import com.clipflow.net.routing.SyncRoutingApi
import com.clipflow.net.ws.WsMessageHandler
import com.clipflow.net.ws.WsSessionManager
import com.clipflow.pairing.v3.PairingProtocolV3Service
import com.clipflow.pairing.v3.PairingVersionCoordinator
import com.clipflow.paste.CacheManager
import com.clipflow.paste.PasteReleaseService
import com.clipflow.paste.PasteboardService
import com.clipflow.path.UserDataPathProvider
import com.clipflow.secure.SecureKeyPairSerializer
import com.clipflow.secure.SecureStore
import com.clipflow.sync.NearbyDeviceManager
import com.clipflow.sync.PendingKeyExchangeStore
import com.clipflow.sync.PushSessionManager
import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*

class DesktopServerModule(
    appControl: AppControl,
    appInfo: AppInfo,
    appTokenApi: AppTokenApi,
    cacheManager: CacheManager,
    configManager: CommonConfigManager,
    exceptionHandler: ExceptionHandler,
    nearbyDeviceManager: NearbyDeviceManager,
    networkInterfaceService: NetworkInterfaceService,
    pairingProtocolV3Service: PairingProtocolV3Service,
    pairingVersionCoordinator: PairingVersionCoordinator,
    pendingKeyExchangeStore: PendingKeyExchangeStore,
    pasteboardService: PasteboardService,
    pasteDao: PasteDao,
    pasteReleaseService: PasteReleaseService,
    pushSessionManager: PushSessionManager,
    secureKeyPairSerializer: SecureKeyPairSerializer,
    secureStore: SecureStore,
    syncApi: SyncApi,
    syncInfoFactory: SyncInfoFactory,
    syncRoutingApi: SyncRoutingApi,
    serverEncryptPluginFactory: ServerEncryptPluginFactory,
    serverDecryptionPluginFactory: ServerDecryptionPluginFactory,
    userDataPathProvider: UserDataPathProvider,
    wsMessageHandler: WsMessageHandler,
    wsSessionManager: WsSessionManager,
) : DefaultServerModule(
        appControl,
        appInfo,
        appTokenApi,
        cacheManager,
        configManager,
        exceptionHandler,
        nearbyDeviceManager,
        networkInterfaceService,
        pairingProtocolV3Service,
        pairingVersionCoordinator,
        pendingKeyExchangeStore,
        pasteboardService,
        pasteDao,
        pasteReleaseService,
        pushSessionManager,
        secureKeyPairSerializer,
        secureStore,
        syncApi,
        syncInfoFactory,
        syncRoutingApi,
        serverEncryptPluginFactory,
        serverDecryptionPluginFactory,
        userDataPathProvider,
        wsMessageHandler,
        wsSessionManager,
    ) {

    override fun installModules(): Application.() -> Unit =
        {
            install(Compression) {
                gzip {
                    priority = 1.0
                }
                deflate {
                    priority = 10.0
                    minimumSize(1024)
                }
            }
            super.installModules()()
        }
}
