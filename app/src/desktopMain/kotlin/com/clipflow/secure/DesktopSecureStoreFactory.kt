package com.clipflow.secure

import com.clipflow.app.AppInfo
import com.clipflow.db.secure.SecureIO
import com.clipflow.path.AppPathProvider
import com.clipflow.platform.Platform

class DesktopSecureStoreFactory(
    private val appInfo: AppInfo,
    private val appPathProvider: AppPathProvider,
    private val platform: Platform,
    private val secureKeyPairSerializer: SecureKeyPairSerializer,
    private val secureIO: SecureIO,
) : SecureStoreFactory {

    override fun createSecureStore(): SecureStore = getSecureStoreFactory().createSecureStore()

    private fun getSecureStoreFactory(): SecureStoreFactory =
        if (platform.isMacos()) {
            MacosSecureStoreFactory(
                appInfo,
                appPathProvider,
                secureKeyPairSerializer,
                secureIO,
            )
        } else if (platform.isWindows()) {
            WindowsSecureStoreFactory(
                appPathProvider,
                secureKeyPairSerializer,
                secureIO,
            )
        } else if (platform.isLinux()) {
            LinuxSecureStoreFactory(
                appPathProvider,
                secureKeyPairSerializer,
                secureIO,
            )
        } else {
            throw IllegalStateException("Unsupported platform: ${platform.name}")
        }
}
