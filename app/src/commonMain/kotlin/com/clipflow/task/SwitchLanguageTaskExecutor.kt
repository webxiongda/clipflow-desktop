package com.clipflow.task

import com.clipflow.config.CommonConfigManager
import com.clipflow.db.task.PasteTask
import com.clipflow.db.task.SwitchLanguageInfo
import com.clipflow.db.task.TaskType
import com.clipflow.exception.ErrorCodeSupplier
import com.clipflow.exception.StandardErrorCode
import com.clipflow.net.clientapi.createFailureResult
import com.clipflow.paste.GuidePasteDataService
import com.clipflow.utils.DateUtils.nowEpochMilliseconds
import com.clipflow.utils.TaskUtils
import io.github.oshai.kotlinlogging.KotlinLogging

class SwitchLanguageTaskExecutor(
    private val configManager: CommonConfigManager,
    private val guidePasteDataService: GuidePasteDataService,
) : SingleTypeTaskExecutor {

    private val logger = KotlinLogging.logger {}

    override val taskType: Int = TaskType.SWITCH_LANGUAGE_TASK

    override suspend fun doExecuteTask(pasteTask: PasteTask): PasteTaskResult {
        val switchLanguageInfo = TaskUtils.getExtraInfo(pasteTask, SwitchLanguageInfo::class)
        val currentLanguage = configManager.getCurrentConfig().language

        if (switchLanguageInfo.language != currentLanguage) {
            logger.info {
                "Skip switch to ${switchLanguageInfo.language}, " +
                    "current language is $currentLanguage"
            }
            return SuccessPasteTaskResult()
        }

        return runCatching {
            guidePasteDataService.updateData()
            SuccessPasteTaskResult()
        }.getOrElse { e ->
            createFailurePasteTaskResult(
                switchLanguageInfo = switchLanguageInfo,
                errorMessage = "switch fail: ${e.message}",
            )
        }
    }

    private fun createFailurePasteTaskResult(
        switchLanguageInfo: SwitchLanguageInfo,
        errorCodeSupplier: ErrorCodeSupplier = StandardErrorCode.SWITCH_LANGUAGE_TASK_FAIL,
        errorMessage: String,
    ): PasteTaskResult =
        TaskUtils.createFailurePasteTaskResult(
            logger = logger,
            retryHandler = { false },
            startTime = nowEpochMilliseconds(),
            fails =
                listOf(
                    createFailureResult(
                        errorCodeSupplier,
                        errorMessage,
                    ),
                ),
            extraInfo = switchLanguageInfo,
        )
}
