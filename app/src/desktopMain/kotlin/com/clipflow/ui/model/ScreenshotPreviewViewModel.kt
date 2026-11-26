package com.clipflow.ui.model

import androidx.lifecycle.ViewModel
import com.clipflow.app.DesktopAppWindowManager
import com.clipflow.db.paste.SearchPasteData
import com.clipflow.paste.PasteData
import com.clipflow.paste.PasteType
import com.clipflow.utils.ioDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * State for the screenshot preview bubble.
 *
 * After a screenshot lands in the clipboard history, [reload] fetches the newest
 * images and the bubble shows them newest-first. [next] walks toward older
 * screenshots ("回溯看到上一张") and [prev] walks back toward the newest one.
 * Indexes are stable across new arrivals because the highlighted item is tracked
 * by paste id, not by position.
 */
class ScreenshotPreviewViewModel(
    private val appWindowManager: DesktopAppWindowManager,
    private val searchPasteData: SearchPasteData,
    private val backgroundDispatcher: CoroutineDispatcher = ioDispatcher,
) : ViewModel() {

    companion object {
        /** How many recent images the bubble can page back through. */
        const val HISTORY_LIMIT = 20
    }

    private val _images = MutableStateFlow<List<PasteData>>(emptyList())
    val images: StateFlow<List<PasteData>> = _images.asStateFlow()

    private val _currentId = MutableStateFlow<Long?>(null)
    val currentId: StateFlow<Long?> = _currentId.asStateFlow()

    /**
     * Loads the newest images and points the bubble at [focusId] when it is in the
     * list (the screenshot just captured), otherwise at the newest image.
     */
    suspend fun reload(focusId: Long? = null) {
        val loaded =
            withContext(backgroundDispatcher) {
                searchPasteData
                    .searchPasteData(
                        searchTerms = emptyList(),
                        pasteTypeList = listOf(PasteType.IMAGE_TYPE.type),
                        limit = HISTORY_LIMIT,
                    ).filter { it.isValid() }
            }
        _images.value = loaded
        _currentId.value =
            if (focusId != null && loaded.any { it.id == focusId }) {
                focusId
            } else {
                loaded.firstOrNull()?.id
            }
    }

    /** Moves toward an older screenshot. Returns false when already at the oldest. */
    fun next(): Boolean {
        val list = _images.value
        val id = _currentId.value
        val index = list.indexOfFirst { it.id == id }
        if (index < 0 || index >= list.lastIndex) return false
        _currentId.value = list[index + 1].id
        return true
    }

    /** Moves toward the newest screenshot. Returns false when already at the newest. */
    fun prev(): Boolean {
        val list = _images.value
        val id = _currentId.value
        val index = list.indexOfFirst { it.id == id }
        if (index <= 0) return false
        _currentId.value = list[index - 1].id
        return true
    }

    fun hide() {
        appWindowManager.hideScreenshotPreviewWindow()
    }
}
