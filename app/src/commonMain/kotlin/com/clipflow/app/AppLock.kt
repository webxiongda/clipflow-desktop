package com.clipflow.app

interface AppLock {

    fun acquireLock(): AppLockState

    fun releaseLock()

    fun resetFirstLaunchFlag()
}
