package com.clipflow.headless

import com.clipflow.app.AppWindowManager

class HeadlessAppWindowManager : AppWindowManager() {

    override suspend fun toPaste() {
        // No-op in headless mode
    }
}
