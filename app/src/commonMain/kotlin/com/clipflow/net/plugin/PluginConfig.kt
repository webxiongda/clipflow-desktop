package com.clipflow.net.plugin

import com.clipflow.secure.SecureStore
import io.ktor.utils.io.*

@KtorDsl
class PluginConfig(
    val secureStore: SecureStore,
)
