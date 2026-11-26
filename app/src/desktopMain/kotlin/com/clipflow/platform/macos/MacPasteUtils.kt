package com.clipflow.platform.macos

import com.clipflow.listener.KeyboardKey
import com.clipflow.listener.ShortcutKeys
import com.clipflow.platform.macos.api.MacosApi
import com.sun.jna.Memory

class MacPasteUtils(
    private val shortcutKeys: ShortcutKeys,
) {

    private var memory: Memory? = null

    private var keys: List<KeyboardKey>? = null

    @Synchronized
    private fun getPasteMemory(): Pair<Memory, Int> {
        val currentKeys = shortcutKeys.shortcutKeysCore.value.keys["paste"] ?: emptyList()
        if (memory == null || this.keys != currentKeys) {
            this.keys = currentKeys
            memory = createMemory(currentKeys)
        }
        return Pair(memory!!, currentKeys.size)
    }

    private fun createMemory(keys: List<KeyboardKey>): Memory {
        val memorySize = keys.size * Int.SIZE_BYTES
        val newMemory = Memory(memorySize.toLong())

        keys.map { it.rawCode }.toIntArray().let { keyCodes ->
            keyCodes.forEachIndexed { index, value ->
                newMemory.setInt((index * Int.SIZE_BYTES).toLong(), value)
            }
        }
        return newMemory
    }

    fun simulatePasteCommand() {
        val (memory, size) = getPasteMemory()
        MacosApi.INSTANCE.simulatePasteCommand(memory, size)
    }
}
