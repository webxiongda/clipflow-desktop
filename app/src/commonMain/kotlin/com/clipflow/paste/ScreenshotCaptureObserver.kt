package com.clipflow.paste

/**
 * Notified after a clipboard capture has been persisted. The desktop app uses this
 * to surface a screenshot preview bubble; headless builds and tests use the no-op
 * default so they need no UI dependencies.
 */
interface ScreenshotCaptureObserver {
    suspend fun onCaptured(pasteId: Long)

    object NoOp : ScreenshotCaptureObserver {
        override suspend fun onCaptured(pasteId: Long) = Unit
    }
}
