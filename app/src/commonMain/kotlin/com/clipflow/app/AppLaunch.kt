package com.clipflow.app

import kotlinx.coroutines.flow.StateFlow

interface AppLaunch {

    val appLaunchState: StateFlow<AppLaunchState>

    suspend fun launch(): AppLaunchState
}
