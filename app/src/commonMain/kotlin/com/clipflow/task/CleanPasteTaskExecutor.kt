package com.clipflow.task

import com.clipflow.clean.CleanTime
import com.clipflow.config.CommonConfigManager
import com.clipflow.db.paste.PasteDao
import com.clipflow.db.task.BaseExtraInfo
import com.clipflow.db.task.PasteTask
import com.clipflow.db.task.TaskType
import com.clipflow.exception.StandardErrorCode
import com.clipflow.net.clientapi.createFailureResult
import com.clipflow.paste.PasteType
import com.clipflow.utils.TaskUtils
import com.clipflow.utils.getDateUtils
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CleanPasteTaskExecutor(
    private val pasteDao: PasteDao,
    private val configManager: CommonConfigManager,
) : SingleTypeTaskExecutor {

    private val logger: KLogger = KotlinLogging.logger {}

    private val dateUtils = getDateUtils()

    override val taskType: Int = TaskType.CLEAN_PASTE_TASK

    private val cleanLock = Mutex()

    override suspend fun doExecuteTask(pasteTask: PasteTask): PasteTaskResult {
        val config = configManager.getCurrentConfig()
        if (config.enableExpirationCleanup) {
            runCatching {
                cleanLock.withLock {
                    val imageCleanTimeIndex = config.imageCleanTimeIndex
                    val imageCleanTime = CleanTime.entries[imageCleanTimeIndex]
                    val imageCleanTimeInstant = dateUtils.getOffsetDay(days = -imageCleanTime.days)
                    pasteDao.markDeleteByCleanTime(imageCleanTimeInstant, PasteType.IMAGE_TYPE.type)

                    val fileCleanTimeIndex = config.fileCleanTimeIndex
                    val fileCleanTime = CleanTime.entries[fileCleanTimeIndex]
                    val fileCleanTimeInstant = dateUtils.getOffsetDay(days = -fileCleanTime.days)
                    pasteDao.markDeleteByCleanTime(fileCleanTimeInstant, PasteType.FILE_TYPE.type)
                }
            }.onFailure {
                val baseExtraInfo = TaskUtils.getExtraInfo(pasteTask, BaseExtraInfo::class)
                return TaskUtils.createFailurePasteTaskResult(
                    logger = logger,
                    retryHandler = { baseExtraInfo.executionHistories.size < 2 },
                    startTime = pasteTask.modifyTime,
                    fails = listOf(createFailureResult(StandardErrorCode.CLEAN_PASTE_TASK_FAIL, it)),
                    extraInfo = baseExtraInfo,
                )
            }
        }

        if (config.enableThresholdCleanup) {
            runCatching {
                cleanLock.withLock {
                    val allSize = pasteDao.getSize(allOrTagged = true)
                    val taggedSize = pasteDao.getSize(allOrTagged = false)
                    val untaggedSize = allSize - taggedSize
                    if (untaggedSize > config.maxStorage * 1024 * 1024) {
                        val cleanSize = untaggedSize * config.cleanupPercentage / 100
                        deleteStorageOfApproximateSize(cleanSize)
                    }
                }
            }.onFailure {
                val baseExtraInfo = TaskUtils.getExtraInfo(pasteTask, BaseExtraInfo::class)
                return TaskUtils.createFailurePasteTaskResult(
                    logger = logger,
                    retryHandler = { baseExtraInfo.executionHistories.size < 2 },
                    startTime = pasteTask.modifyTime,
                    fails = listOf(createFailureResult(StandardErrorCode.CLEAN_PASTE_TASK_FAIL, it)),
                    extraInfo = baseExtraInfo,
                )
            }
        }

        return SuccessPasteTaskResult()
    }

    private suspend fun deleteStorageOfApproximateSize(cleanSize: Long) {
        val cutoffTime = pasteDao.findCleanTimeByCumulativeSize(cleanSize) ?: return
        pasteDao.markDeleteByCleanTime(cutoffTime)
    }
}
