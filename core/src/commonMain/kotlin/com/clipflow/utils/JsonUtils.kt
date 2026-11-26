package com.clipflow.utils

import com.clipflow.paste.PasteData
import com.clipflow.paste.item.ColorPasteItem
import com.clipflow.paste.item.FilesPasteItem
import com.clipflow.paste.item.HtmlPasteItem
import com.clipflow.paste.item.ImagesPasteItem
import com.clipflow.paste.item.PasteItem
import com.clipflow.paste.item.RtfPasteItem
import com.clipflow.paste.item.TextPasteItem
import com.clipflow.paste.item.UrlPasteItem
import com.clipflow.presist.DirFileInfoTree
import com.clipflow.presist.FileInfoTree
import com.clipflow.presist.SingleFileInfoTree
import com.clipflow.serializer.Base64ByteArraySerializer
import com.clipflow.serializer.HtmlPasteItemSerializer
import com.clipflow.serializer.PasteDataSerializer
import com.clipflow.serializer.RtfPasteItemSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

expect fun getJsonUtils(): JsonUtils

interface JsonUtils {

    val JSON: Json

    fun createJSON(): Json =
        Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
            serializersModule =
                SerializersModule {
                    contextual(ByteArray::class, Base64ByteArraySerializer())
                    contextual(PasteData::class, PasteDataSerializer())

                    polymorphic(PasteItem::class) {
                        subclass(ColorPasteItem::class)
                        subclass(FilesPasteItem::class)
                        subclass(HtmlPasteItem::class, HtmlPasteItemSerializer())
                        subclass(ImagesPasteItem::class)
                        subclass(RtfPasteItem::class, RtfPasteItemSerializer())
                        subclass(TextPasteItem::class)
                        subclass(UrlPasteItem::class)
                    }

                    polymorphic(FileInfoTree::class) {
                        subclass(SingleFileInfoTree::class)
                        subclass(DirFileInfoTree::class)
                    }

                    extSerializerModule()
                }
        }

    fun SerializersModuleBuilder.extSerializerModule() {}
}
