package com.clipflow.i18n

import com.clipflow.config.CommonConfigManager
import com.clipflow.db.task.SwitchLanguageInfo
import com.clipflow.db.task.TaskDao
import com.clipflow.db.task.TaskType
import com.clipflow.i18n.DesktopGlobalCopywriter.Companion.EMPTY_STRING
import com.clipflow.i18n.SupportedLanguages.EN
import com.clipflow.task.TaskExecutor
import com.clipflow.ui.i18n.ComposeGlobalCopywriter
import com.clipflow.utils.DateTimeFormatOptions
import com.clipflow.utils.GlobalCoroutineScope.cpuCoroutineDispatcher
import com.clipflow.utils.getDateUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.Properties

class DesktopGlobalCopywriter(
    configManager: CommonConfigManager,
    private val lazyTaskExecutor: Lazy<TaskExecutor>,
    private val taskDao: TaskDao,
) : ComposeGlobalCopywriter(configManager, { DesktopCopywriter(it) }) {

    companion object {

        const val EMPTY_STRING = ""
    }

    private val logger = KotlinLogging.logger {}

    private val taskExecutor by lazy { lazyTaskExecutor.value }

    override fun onLanguageSwitched(language: String) {
        cpuCoroutineDispatcher.launch {
            taskExecutor.submitTask(
                taskDao.createTask(null, TaskType.SWITCH_LANGUAGE_TASK, SwitchLanguageInfo(language)),
            )
        }
    }

    override fun getText(
        id: String,
        vararg args: Any?,
    ): String {
        // Read once: a concurrent switchLanguage() must not mix languages within one lookup.
        val current = copywriter
        val text = current.getText(id, *args)
        return if (text == EMPTY_STRING && current.language() != EN) {
            logger.debug { "Missing text for id: $id in language: ${current.language()}" }
            val enText = enCopywriter.getText(id, *args) // Fallback to English if not found
            if (enText == EMPTY_STRING) {
                logger.warn { "Missing text for id: $id in English" }
                "[$id]"
            } else {
                enText
            }
        } else {
            text
        }
    }
}

class DesktopCopywriter(
    private val language: String,
) : Copywriter {

    private val logger = KotlinLogging.logger {}

    private val dateUtils = getDateUtils()

    private val properties: Properties = loadProperties()

    private var currentLanguage: String = EN

    private fun load(
        properties: Properties,
        language: String,
    ) {
        properties.load(
            DesktopCopywriter::class.java
                .getResourceAsStream("/i18n/$language.properties")
                ?.let {
                    InputStreamReader(
                        it,
                        StandardCharsets.UTF_8,
                    )
                } ?: (throw FileNotFoundException("No properties for $language")),
        )
    }

    private fun loadProperties(): Properties {
        val properties = Properties()
        currentLanguage =
            runCatching {
                load(properties, language)
                language
            }.getOrElse { e ->
                logger.error(e) { "Error loading $language properties" }
                load(properties, EN)
                EN
            }
        return properties
    }

    override fun language(): String = language

    override fun getText(
        id: String,
        vararg args: Any?,
    ): String {
        val value: String? = properties.getProperty(id)
        return value?.format(*args) ?: EMPTY_STRING
    }

    override fun getKeys(): Set<String> = properties.keys.map { it.toString() }.toSet()

    override fun getDate(
        date: LocalDateTime,
        options: DateTimeFormatOptions,
    ): String = dateUtils.getDateDesc(date, options, language)

    override fun getAbridge(): String = language
}
