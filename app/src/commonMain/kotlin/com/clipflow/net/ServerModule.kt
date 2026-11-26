package com.clipflow.net

import io.ktor.server.application.*

interface ServerModule {

    fun installModules(): Application.() -> Unit
}
