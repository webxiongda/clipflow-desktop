package com.clipflow.net.ws

import com.clipflow.app.AppControl
import com.clipflow.app.AppInfo
import com.clipflow.app.RatingPromptManager
import com.clipflow.config.CommonConfigManager
import com.clipflow.db.paste.PasteDao
import com.clipflow.db.sync.SyncRuntimeInfoDao
import com.clipflow.net.NetworkInterfaceService
import com.clipflow.net.PasteBonjourService
import com.clipflow.net.SyncApi
import com.clipflow.net.SyncInfoFactory
import com.clipflow.net.TelnetHelper
import com.clipflow.net.clientapi.SyncClientApi
import com.clipflow.net.routing.SyncRoutingApi
import com.clipflow.paste.CacheManager
import com.clipflow.paste.PasteboardService
import com.clipflow.path.UserDataPathProvider
import com.clipflow.platform.Platform
import com.clipflow.secure.SecureKeyPairSerializer
import com.clipflow.secure.SecureStore
import com.clipflow.sync.NearbyDeviceManager
import com.clipflow.sync.PendingExchangeLedger
import com.clipflow.sync.SyncDeviceManager
import com.clipflow.sync.SyncManager
import com.clipflow.sync.SyncResolver
import com.clipflow.sync.SyncResolverApi
import com.clipflow.sync.TokenCacheApi
import com.clipflow.utils.ioDispatcher
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.core.context.GlobalContext
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Verifies that WebSocket-related Koin singletons can be resolved without
 * circular dependency (StackOverflowError). This catches cycles like:
 * WsMessageHandler → SyncRoutingApi → SyncManager → SyncResolver → WsClientConnector → WsMessageHandler
 */
class WsModuleDependencyTest : KoinTest {

    @AfterTest
    fun tearDown() {
        GlobalContext.stopKoin()
    }

    @Test
    fun allWsComponentsResolvableWithoutCircularDependency() {
        val mockSyncManager =
            mockk<SyncManager>(relaxed = true) {
                every { realTimeSyncScope } returns CoroutineScope(ioDispatcher + SupervisorJob())
            }

        val testModule =
            module {
                // Stubs for dependencies outside the cycle
                single<AppControl> { mockk(relaxed = true) }
                single<CommonConfigManager> { mockk(relaxed = true) }
                single<AppInfo> { mockk(relaxed = true) { every { appInstanceId } returns "test" } }
                single<Platform> { mockk(relaxed = true) { every { isDesktop() } returns true } }
                single<NearbyDeviceManager> { mockk(relaxed = true) }
                single<NetworkInterfaceService> { mockk(relaxed = true) }
                single<PasteBonjourService> { mockk(relaxed = true) }
                single<PasteboardService> { mockk(relaxed = true) }
                single<RatingPromptManager> { mockk(relaxed = true) }
                single<SecureKeyPairSerializer> { mockk(relaxed = true) }
                single<SecureStore> { mockk(relaxed = true) }
                single<SyncApi> { SyncApi }
                single<SyncClientApi> { mockk(relaxed = true) }
                single<SyncDeviceManager> { mockk(relaxed = true) }
                single<SyncInfoFactory> { mockk(relaxed = true) }
                single<SyncRuntimeInfoDao> { mockk(relaxed = true) }
                single<TelnetHelper> { mockk(relaxed = true) }
                single<TokenCacheApi> { mockk(relaxed = true) }

                // The actual components under test — mirrors DesktopNetworkModule wiring
                single<CacheManager> { mockk(relaxed = true) }
                single<PasteDao> { mockk(relaxed = true) }
                single<UserDataPathProvider> { mockk(relaxed = true) }
                single<WsSessionManager> {
                    WsSessionManager()
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
                single<WsClientConnector> {
                    val wsHttpClient =
                        HttpClient(CIO) {
                            install(WebSockets) {
                                pingIntervalMillis = 30_000
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
                single<SyncResolverApi> {
                    SyncResolver(
                        appInfo = get(),
                        localPlatform = get(),
                        lazyPasteBonjourService = lazy { get() },
                        networkInterfaceService = get(),
                        pendingExchangeLedger = PendingExchangeLedger(),
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
                single<SyncManager> { mockSyncManager }
                single<SyncRoutingApi> { get<SyncManager>() }
            }

        GlobalContext.startKoin { modules(testModule) }

        // If any of these throw StackOverflowError, we have a circular dependency
        assertNotNull(get<WsSessionManager>())
        assertNotNull(get<WsMessageHandler>())
        assertNotNull(get<WsClientConnector>())
        assertNotNull(get<SyncResolverApi>())
    }
}
