package com.clipflow.utils

expect fun createPlatformLock(): PlatformLock

interface PlatformLock {
    fun lock()

    fun unlock()

    fun <T> withLock(action: () -> T): T
}
