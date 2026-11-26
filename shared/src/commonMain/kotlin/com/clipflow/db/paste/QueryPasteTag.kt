package com.clipflow.db.paste

import com.clipflow.paste.PasteTag
import kotlinx.coroutines.flow.Flow

interface QueryPasteTag {

    fun getAllTagsFlow(): Flow<List<PasteTag>>
}
