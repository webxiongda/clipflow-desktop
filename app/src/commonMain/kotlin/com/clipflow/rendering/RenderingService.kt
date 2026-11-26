package com.clipflow.rendering

import com.clipflow.paste.PasteData

interface RenderingService<T> {

    suspend fun render(pasteData: PasteData)

    fun start()

    fun stop()

    fun restart() {
        stop()
        start()
    }
}
