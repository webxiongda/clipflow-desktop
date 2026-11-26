package com.clipflow.ui.model

import com.clipflow.app.DesktopAppWindowManager
import com.clipflow.db.paste.SearchPasteData
import com.clipflow.paste.PasteCollection
import com.clipflow.paste.PasteData
import com.clipflow.paste.PasteType
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ScreenshotPreviewViewModelTest {

    private class FakeSearchPasteData(
        private val results: List<PasteData>,
    ) : SearchPasteData {
        var lastTypeFilter: List<Int> = emptyList()
        var lastLimit: Int = 0

        override suspend fun searchPasteData(
            searchTerms: List<String>,
            local: Boolean?,
            pasteTypeList: List<Int>,
            sort: Boolean,
            tag: Long?,
            limit: Int,
        ): List<PasteData> {
            lastTypeFilter = pasteTypeList
            lastLimit = limit
            return results
        }

        override fun searchPasteDataFlow(
            searchTerms: List<String>,
            local: Boolean?,
            pasteTypeList: List<Int>,
            sort: Boolean,
            tag: Long?,
            limit: Int,
        ): Flow<List<PasteData>> = throw UnsupportedOperationException()

        override suspend fun searchBySource(source: String): List<PasteData> = emptyList()
    }

    private fun image(id: Long) =
        PasteData(
            id = id,
            appInstanceId = "test-instance",
            pasteCollection = PasteCollection(emptyList()),
            pasteType = PasteType.IMAGE_TYPE.type,
            size = 1,
            hash = "hash-$id",
        )

    private fun viewModel(search: SearchPasteData) =
        ScreenshotPreviewViewModel(
            appWindowManager = mockk(relaxed = true),
            searchPasteData = search,
            backgroundDispatcher = UnconfinedTestDispatcher(),
        )

    @Test
    fun `reload keeps newest first and focuses the just-captured screenshot`() =
        runTest {
            val search = FakeSearchPasteData(listOf(image(1), image(2), image(3)))
            val model = viewModel(search)

            model.reload(focusId = 3)

            assertEquals(listOf(1L, 2L, 3L), model.images.value.map { it.id })
            assertEquals(3L, model.currentId.value)
            assertEquals(listOf(PasteType.IMAGE_TYPE.type), search.lastTypeFilter)
            assertEquals(ScreenshotPreviewViewModel.HISTORY_LIMIT, search.lastLimit)
        }

    @Test
    fun `next walks toward older screenshots and stops at the oldest`() =
        runTest {
            val model = viewModel(FakeSearchPasteData(listOf(image(1), image(2), image(3))))
            model.reload(focusId = 1)

            assertTrue(model.next())
            assertEquals(2L, model.currentId.value)
            assertTrue(model.next())
            assertEquals(3L, model.currentId.value)
            assertFalse(model.next())
            assertEquals(3L, model.currentId.value)
        }

    @Test
    fun `prev walks toward the newest screenshot and stops at the newest`() =
        runTest {
            val model = viewModel(FakeSearchPasteData(listOf(image(1), image(2), image(3))))
            model.reload(focusId = 3)

            assertTrue(model.prev())
            assertEquals(2L, model.currentId.value)
            assertTrue(model.prev())
            assertEquals(1L, model.currentId.value)
            assertFalse(model.prev())
        }

    @Test
    fun `unknown focus id falls back to the newest screenshot`() =
        runTest {
            val model = viewModel(FakeSearchPasteData(listOf(image(1), image(2))))

            model.reload(focusId = 999)

            assertEquals(1L, model.currentId.value)
        }

    @Test
    fun `hide closes the preview window`() {
        val windowManager = mockk<DesktopAppWindowManager>(relaxed = true)
        val model =
            ScreenshotPreviewViewModel(
                appWindowManager = windowManager,
                searchPasteData = FakeSearchPasteData(emptyList()),
                backgroundDispatcher = UnconfinedTestDispatcher(),
            )

        model.hide()

        verify { windowManager.hideScreenshotPreviewWindow() }
    }
}
