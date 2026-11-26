package com.clipflow.utils

import com.clipflow.db.task.BaseExtraInfo
import com.clipflow.db.task.DelayedDeleteExtraInfo
import com.clipflow.db.task.PasteTaskExtraInfo
import com.clipflow.db.task.PullExtraInfo
import com.clipflow.db.task.SwitchLanguageInfo
import com.clipflow.db.task.SyncExtraInfo
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.overwriteWith
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Extended JSON instance that includes PasteTaskExtraInfo polymorphic registration,
 * building on top of core's JSON serializer modules.
 */
object SharedJsonUtils {

    val JSON: Json =
        Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
            serializersModule =
                getJsonUtils().JSON.serializersModule.overwriteWith(
                    SerializersModule {
                        polymorphic(PasteTaskExtraInfo::class) {
                            subclass(BaseExtraInfo::class)
                            subclass(SyncExtraInfo::class)
                            subclass(PullExtraInfo::class)
                            subclass(SwitchLanguageInfo::class)
                            subclass(DelayedDeleteExtraInfo::class)
                        }
                    },
                )
        }
}
