package com.clipflow.dto.paste

import com.clipflow.paste.item.PasteItem
import kotlinx.serialization.Serializable

@Serializable
data class SyncPasteCollection(
    val pasteItems: List<PasteItem>,
)
