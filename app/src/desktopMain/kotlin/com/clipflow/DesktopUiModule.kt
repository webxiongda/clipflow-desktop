package com.clipflow

import coil3.PlatformContext
import com.clipflow.app.AppFileChooser
import com.clipflow.app.AppSize
import com.clipflow.app.AppTokenApi
import com.clipflow.app.AppWindowManager
import com.clipflow.app.DesktopAppFileChooser
import com.clipflow.app.DesktopAppSize
import com.clipflow.app.DesktopAppTokenService
import com.clipflow.app.DesktopAppWindowManager
import com.clipflow.app.DesktopRatingPromptManager
import com.clipflow.app.DesktopUserAttentionService
import com.clipflow.app.RatingPromptManager
import com.clipflow.app.UserAttentionService
import com.clipflow.app.getDesktopAppWindowManager
import com.clipflow.i18n.DesktopGlobalCopywriter
import com.clipflow.i18n.GlobalCopywriter
import com.clipflow.image.DesktopIconColorExtractor
import com.clipflow.image.coil.AppSourceIconTransformer
import com.clipflow.image.coil.DefaultAppSourceIconTransformer
import com.clipflow.listener.ActiveGraphicsDevice
import com.clipflow.listener.DesktopGlobalListener
import com.clipflow.listener.DesktopShortKeysAction
import com.clipflow.listener.DesktopShortcutKeys
import com.clipflow.listener.DesktopShortcutKeysListener
import com.clipflow.listener.DesktopShortcutKeysLoader
import com.clipflow.listener.GlobalListener
import com.clipflow.listener.ShortcutKeys
import com.clipflow.listener.ShortcutKeysAction
import com.clipflow.listener.ShortcutKeysListener
import com.clipflow.listener.ShortcutKeysLoader
import com.clipflow.notification.NotificationManager
import com.clipflow.sound.DesktopSoundService
import com.clipflow.sound.SoundService
import com.clipflow.sync.TokenCache
import com.clipflow.sync.TokenCacheApi
import com.clipflow.ui.DesktopScreenProvider
import com.clipflow.ui.NavigationManager
import com.clipflow.ui.ScreenProvider
import com.clipflow.ui.base.DesktopNotificationManager
import com.clipflow.ui.base.DesktopUISupport
import com.clipflow.ui.base.MenuHelper
import com.clipflow.ui.base.SmartImageDisplayStrategy
import com.clipflow.ui.base.UISupport
import com.clipflow.ui.devices.DesktopDeviceScopeFactory
import com.clipflow.ui.devices.DesktopSyncScopeFactory
import com.clipflow.ui.devices.DeviceScopeFactory
import com.clipflow.ui.devices.SyncScopeFactory
import com.clipflow.ui.settings.DesktopStoragePathManager
import com.clipflow.ui.settings.StoragePathManager
import com.clipflow.ui.theme.DesktopThemeDetector
import com.clipflow.ui.theme.ThemeDetector
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import com.github.kwhat.jnativehook.mouse.NativeMouseListener
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

fun desktopUiModule(): Module =
    module {
        // region App & window
        single { DesktopAppFileChooser(get()) } bind AppFileChooser::class
        single<AppSize> { get<DesktopAppSize>() }
        single<AppTokenApi> { DesktopAppTokenService(get()) }
        single<AppWindowManager> { get<DesktopAppWindowManager>() }
        single<DesktopAppSize> { DesktopAppSize(get(), get()) }
        single<DesktopAppWindowManager> {
            getDesktopAppWindowManager(
                get(),
                get(),
                lazy { get() },
                lazy { get() },
                lazy { get() },
                lazy { get() },
                lazy { get() },
                get(),
                get(),
            )
        }
        single<RatingPromptManager> { DesktopRatingPromptManager() }
        single<UserAttentionService> { DesktopUserAttentionService(get<DesktopAppWindowManager>()) }
        // endregion

        // region Input & listeners
        single<ActiveGraphicsDevice> { get<DesktopAppSize>() }
        single<DesktopShortcutKeysListener> { DesktopShortcutKeysListener(get(), get()) }
        single<GlobalListener> { DesktopGlobalListener(get(), get(), get(), get()) }
        single<NativeKeyListener> { get<DesktopShortcutKeysListener>() }
        single<NativeMouseListener> { get<DesktopAppSize>() }
        single<ShortcutKeys> { DesktopShortcutKeys(get(), get(), get()) }
        single<ShortcutKeysAction> {
            DesktopShortKeysAction(get(), get(), lazy { get() }, get(), get(), get(), get(), get(), get(), get())
        }
        single<ShortcutKeysListener> { get<DesktopShortcutKeysListener>() }
        single<ShortcutKeysLoader> { DesktopShortcutKeysLoader(get(), get()) }
        // endregion

        // region UI screens & navigation
        single<DesktopScreenProvider> { DesktopScreenProvider(get(), get(), get(), get(), get(), get()) }
        single<DeviceScopeFactory> { DesktopDeviceScopeFactory() }
        single<GlobalCopywriter> { DesktopGlobalCopywriter(get(), lazy { get() }, get()) }
        single<MenuHelper> { MenuHelper(get(), get(), get(), get(), get(), get()) }
        single<NavigationManager> { get<DesktopScreenProvider>() }
        single<ScreenProvider> { get<DesktopScreenProvider>() }
        single<SmartImageDisplayStrategy> { SmartImageDisplayStrategy() }
        single<StoragePathManager> { DesktopStoragePathManager() }
        single<SyncScopeFactory> { DesktopSyncScopeFactory() }
        single<ThemeDetector> { DesktopThemeDetector(get()) }
        single<UISupport> {
            DesktopUISupport(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get<DesktopAppWindowManager>(),
            )
        }
        // endregion

        // region Misc
        single<AppSourceIconTransformer> { DefaultAppSourceIconTransformer }
        single<DesktopIconColorExtractor> { DesktopIconColorExtractor(get()) }
        single<NotificationManager> { DesktopNotificationManager(get(), get(), get(), get()) }
        single<PlatformContext> { PlatformContext.INSTANCE }
        single<SoundService> { DesktopSoundService(get()) }
        single<TokenCacheApi> { TokenCache }
        // endregion
    }
