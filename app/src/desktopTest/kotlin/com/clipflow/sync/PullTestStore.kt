package com.clipflow.sync

import com.clipflow.app.AppInfo
import com.clipflow.config.CommonConfigManager
import com.clipflow.config.TestAppConfig
import com.clipflow.db.DriverFactory
import com.clipflow.db.TestDriverFactory
import com.clipflow.db.createDatabase
import com.clipflow.db.paste.SqlPasteDao
import com.clipflow.db.sync.SyncRuntimeInfoDao
import com.clipflow.paste.PasteCollection
import com.clipflow.paste.PasteData
import com.clipflow.paste.PasteReleaseService
import com.clipflow.paste.PasteType
import com.clipflow.paste.item.CreatePasteItemHelper.createTextPasteItem
import com.clipflow.paste.item.DefaultPasteItemReader
import com.clipflow.task.TaskBuilder
import com.clipflow.task.TaskSubmitter
import com.clipflow.utils.getJsonUtils
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk

internal class PullTestStore(
    val driverFactory: DriverFactory = TestDriverFactory(),
) {
    val json = getJsonUtils().JSON
    val database = createDatabase(driverFactory)
    val tasks = mockk<TaskBuilder>(relaxed = true)
    val submitter =
        object : TaskSubmitter {
            override suspend fun submit(block: suspend TaskBuilder.() -> Unit) {
                tasks.block()
            }
        }
    val dao =
        SqlPasteDao(
            AppInfo("peer", "1", "1", "test"),
            database,
            DefaultPasteItemReader(),
            mockk(relaxed = true),
            submitter,
            mockk(relaxed = true),
        )
    var peerExists = true
    private val syncDao =
        mockk<SyncRuntimeInfoDao>().also {
            coEvery { it.getSyncRuntimeInfo(any()) } answers { if (peerExists) mockk(relaxed = true) else null }
        }

    fun cursor() = PastePullCursorManager(dao, syncDao)

    val config =
        mockk<CommonConfigManager>().also {
            every { it.getCurrentConfig() } returns TestAppConfig()
        }

    fun release(cursor: PastePullCursorManager) =
        PasteReleaseService(
            config,
            mockk(relaxed = true),
            database,
            mockk(relaxed = true),
            dao,
            DefaultPasteItemReader(),
            emptyList(),
            cursor,
            mockk(relaxed = true),
            syncDao,
            submitter,
            mockk(relaxed = true),
        )
}

internal fun pullTestPaste(
    id: Long,
    time: Long = 0L,
): PasteData {
    val item = createTextPasteItem(text = "paste-$id")
    return PasteData(
        id = id,
        appInstanceId = "peer",
        pasteAppearItem = item,
        pasteCollection = PasteCollection(emptyList()),
        pasteType = PasteType.TEXT_TYPE.type,
        size = item.size,
        hash = item.hash,
        createTime = time,
    )
}
