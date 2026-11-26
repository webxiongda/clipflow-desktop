package com.clipflow.headless

import coil3.PlatformContext
import com.clipflow.app.AppTokenApi
import com.clipflow.app.AppWindowManager
import com.clipflow.app.DesktopRatingPromptManager
import com.clipflow.app.RatingPromptManager
import com.clipflow.app.UserAttentionService
import com.clipflow.i18n.DesktopGlobalCopywriter
import com.clipflow.i18n.GlobalCopywriter
import com.clipflow.notification.NotificationManager
import com.clipflow.sound.SoundService
import com.clipflow.sync.TokenCache
import com.clipflow.sync.TokenCacheApi
import com.clipflow.ui.base.UISupport
import org.koin.dsl.module

fun headlessUiModule() =
    module {
        single<AppTokenApi> { HeadlessAppTokenService() }
        single<AppWindowManager> { HeadlessAppWindowManager() }
        single<GlobalCopywriter> { DesktopGlobalCopywriter(get(), lazy { get() }, get()) }
        single<NotificationManager> { HeadlessNotificationManager(get()) }
        // The coil ImageLoader/MemoryCache singles in desktopAppModule resolve
        // PlatformContext, which is otherwise only bound by uiModule; without this
        // binding any lazy resolution of them in headless mode crashes with
        // NoDefinitionFoundException.
        single<PlatformContext> { PlatformContext.INSTANCE }
        single<RatingPromptManager> { DesktopRatingPromptManager() }
        single<SoundService> { HeadlessSoundService() }
        single<TokenCacheApi> { TokenCache }
        single<UISupport> { HeadlessUISupport(get()) }
        single<UserAttentionService> { HeadlessUserAttentionService() }
    }

fun headlessViewModelModule() = module {}
