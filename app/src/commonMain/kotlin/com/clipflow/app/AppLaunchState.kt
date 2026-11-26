package com.clipflow.app

interface AppLaunchState {

    val acquiredLock: Boolean

    val firstLaunch: Boolean
}
