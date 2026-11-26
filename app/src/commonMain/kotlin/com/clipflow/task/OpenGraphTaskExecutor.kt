package com.clipflow.task

import com.clipflow.db.paste.PasteDao
import com.clipflow.db.task.BaseExtraInfo
import com.clipflow.db.task.PasteTask
import com.clipflow.db.task.TaskType
import com.clipflow.exception.StandardErrorCode
import com.clipflow.net.clientapi.createFailureResult
import com.clipflow.rendering.RenderingService
import com.clipflow.utils.StripedMutex
import com.clipflow.utils.TaskUtils
import io.github.oshai.kotlinlogging.KotlinLogging

class OpenGraphTaskExecutor(
    lazyUrlRenderingService: Lazy<RenderingService<String>>,
    private val pasteDao: PasteDao,
) : SingleTypeTaskExecutor {

    private val logger = KotlinLogging.logger {}

    override val taskType: Int = TaskType.OPEN_GRAPH_TASK

    private val urlRenderingService = lazyUrlRenderingService.value

    private val mutex = StripedMutex()

    override suspend fun doExecuteTask(pasteTask: PasteTask): PasteTaskResult =
        runCatching {
            pasteTask.pasteDataId?.let { pasteDataId ->
                mutex.withLock(pasteDataId) {
                    pasteDao.getNoDeletePasteData(pasteDataId)?.let { pasteData ->
                        urlRenderingService.render(pasteData)
                    }
                }
            }
            SuccessPasteTaskResult()
        }.getOrElse {
            TaskUtils.createFailurePasteTaskResult(
                logger = logger,
                retryHandler = { false },
                startTime = pasteTask.modifyTime,
                fails =
                    listOf(
                        createFailureResult(
                            StandardErrorCode.OPEN_GRAPH_TASK_FAIL,
                            it,
                        ),
                    ),
                extraInfo = TaskUtils.getExtraInfo(pasteTask, BaseExtraInfo::class),
            )
        }
}
