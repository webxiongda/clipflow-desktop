package com.clipflow.task

import com.clipflow.db.paste.PasteDao
import com.clipflow.db.task.DelayedDeleteExtraInfo
import com.clipflow.db.task.PasteTask
import com.clipflow.db.task.TaskType
import com.clipflow.utils.DateUtils.nowEpochMilliseconds
import com.clipflow.utils.TaskUtils
import com.clipflow.utils.cpuDispatcher
import com.clipflow.utils.namedScope
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DelayedDeletePasteTaskExecutor(
    private val pasteDao: PasteDao,
    private val scope: CoroutineScope = namedScope(cpuDispatcher, "DelayedDeletePasteTaskExecutor"),
) : SingleTypeTaskExecutor {

    private val logger = KotlinLogging.logger {}

    override val taskType: Int = TaskType.DELAYED_DELETE_PASTE_TASK

    override suspend fun doExecuteTask(pasteTask: PasteTask): PasteTaskResult {
        val extraInfo = TaskUtils.getExtraInfo(pasteTask, DelayedDeleteExtraInfo::class)
        val remaining = extraInfo.executeAfter - nowEpochMilliseconds()

        pasteTask.pasteDataId?.let { pasteDataId ->
            scope.launch {
                if (remaining > 0) {
                    delay(remaining)
                }
                runCatching {
                    pasteDao.deletePasteData(pasteDataId)
                }.onFailure { e ->
                    logger.error(e) { "Delayed delete paste task failed for pasteDataId=$pasteDataId" }
                }
            }
        }

        return SuccessPasteTaskResult()
    }
}
