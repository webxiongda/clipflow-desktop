package com.clipflow.app

import com.clipflow.utils.GlobalCoroutineScope.mainCoroutineDispatcher
import kotlinx.coroutines.launch

class DesktopAppTokenService(
    private val appWindowManager: DesktopAppWindowManager,
) : AppTokenService() {

    override fun preShowToken() {
        mainCoroutineDispatcher.launch {
            appWindowManager.showMainWindow(WindowTrigger.SYSTEM)
        }
    }

    override fun preShowPairingCode() {
        mainCoroutineDispatcher.launch {
            appWindowManager.showMainWindow(WindowTrigger.SYSTEM)
        }
    }
}
