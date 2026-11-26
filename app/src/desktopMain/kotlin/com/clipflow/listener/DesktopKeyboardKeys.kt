package com.clipflow.listener

import com.clipflow.platform.Platform

fun getDesktopKeyboardKeys(platform: Platform): KeyboardKeys =
    if (platform.isMacos()) {
        MacKeyboardKeys
    } else if (platform.isWindows()) {
        WindowsKeyboardKeys
    } else if (platform.isLinux()) {
        LinuxKeyboardKeys
    } else {
        throw IllegalStateException("Unsupported platform: $platform")
    }
