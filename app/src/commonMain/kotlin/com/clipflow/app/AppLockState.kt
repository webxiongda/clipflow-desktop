package com.clipflow.app

interface AppLockState {

    val acquiredLock: Boolean

    val firstLaunch: Boolean
}
