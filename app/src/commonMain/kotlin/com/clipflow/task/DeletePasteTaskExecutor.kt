package com.clipflow.task

import com.clipflow.db.paste.PasteDao
import com.clipflow.db.task.BaseExtraInfo
import com.clipflow.db.task.PasteTask
import com.clipflow.db.task.TaskType
import com.clipflow.exception.StandardErrorCode
import com.clipflow.net.clientapi.createFailureResult
import com.clipflow.utils.StripedMutex
import com.clipflow.utils.TaskUtils
import io.github.oshai.kotlinlogging.KotlinLogging

class DeletePasteTaskExecutor(
    private val pasteDao: PasteDao,
) : SingleTypeTaskExecutor {

    private val logger = KotlinLogging.logger {}

    override val taskType: Int = TaskType.DELETE_PASTE_TASK

    private val mutex = StripedMutex()

    override suspend fun doExecuteTask(pasteTask: PasteTask): PasteTaskResult =
        runCatching {
            pasteTask.pasteDataId?.let { pasteDataId ->
                mutex.withLock(pasteDataId) {
                    pasteDao.deletePasteData(pasteDataId)
                }
            }
            SuccessPasteTaskResult()
        }.getOrElse {
            TaskUtils.createFailurePasteTaskResult(
                logger = logger,
                retryHandler = { false },
                startTime = pasteTask.modifyTime,
                fails = listOf(createFailureResult(StandardErrorCode.DELETE_TASK_FAIL, it)),
                extraInfo = TaskUtils.getExtraInfo(pasteTask, BaseExtraInfo::class),
            )
        }
}
