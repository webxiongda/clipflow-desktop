package com.clipflow.paste.item

import com.clipflow.paste.PasteType
import com.clipflow.paste.item.CreatePasteItemHelper.createHtmlPasteItem
import com.clipflow.paste.item.PasteItem.Companion.getExtraInfoFromJson
import com.clipflow.paste.item.PasteItem.Companion.parseStoredIdentifiers
import com.clipflow.paste.item.PasteItemProperties.BACKGROUND
import com.clipflow.serializer.HtmlPasteItemSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put

@Serializable(with = HtmlPasteItemSerializer::class)
class HtmlPasteItem(
    override val identifiers: List<String>,
    override val hash: String,
    override val html: String,
    override val size: Long,
    override val extraInfo: JsonObject? = null,
) : PasteItem,
    PasteHtml {

    constructor(jsonObject: JsonObject) : this(
        identifiers = parseStoredIdentifiers(jsonObject["identifiers"]!!.jsonPrimitive.content),
        hash = jsonObject["hash"]!!.jsonPrimitive.content,
        size = jsonObject["size"]!!.jsonPrimitive.long,
        html = jsonObject["html"]!!.jsonPrimitive.content,
        extraInfo = getExtraInfoFromJson(jsonObject),
    )

    override fun getBackgroundColor(): Int =
        extraInfo?.let { json ->
            runCatching {
                json[BACKGROUND]?.jsonPrimitive?.int
            }.getOrNull()
        } ?: 0

    override fun copy(extraInfo: JsonObject?): HtmlPasteItem =
        createHtmlPasteItem(
            identifiers = identifiers,
            html = html,
            extraInfo = extraInfo,
        )

    override fun getPasteType(): PasteType = PasteType.HTML_TYPE

    override fun isValid(): Boolean =
        hash.isNotEmpty() &&
            size > 0 &&
            html.isNotEmpty()

    override fun toStoredJson(): String =
        buildJsonObject {
            put("type", getPasteType().type)
            put("identifiers", identifiers.joinToString(","))
            put("hash", hash)
            put("size", size)
            put("html", html)
            extraInfo?.let { put("extraInfo", it) }
        }.toString()
}
