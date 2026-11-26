package com.clipflow.image

import com.clipflow.paste.item.PasteFileCoordinate
import com.clipflow.utils.Loader
import okio.Path

interface VideoThumbnailLoader : Loader<PasteFileCoordinate, Path> {

    fun getThumbnailPath(pasteFileCoordinate: PasteFileCoordinate): Path
}
