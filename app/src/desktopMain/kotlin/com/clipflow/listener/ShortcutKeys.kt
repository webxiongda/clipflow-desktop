package com.clipflow.listener

import kotlinx.coroutines.flow.StateFlow

interface ShortcutKeys {

    val shortcutKeysCore: StateFlow<ShortcutKeysCore>

    fun update(
        keyName: String,
        keys: List<KeyboardKey>,
    )
}
