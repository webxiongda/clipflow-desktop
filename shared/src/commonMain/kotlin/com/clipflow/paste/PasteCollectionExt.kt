package com.clipflow.paste

import com.clipflow.paste.item.PasteCoordinate
import com.clipflow.paste.item.bindItem
import com.clipflow.paste.item.clear
import com.clipflow.path.UserDataPathProvider

fun PasteCollection.clear(
    clearResource: Boolean = true,
    userDataPathProvider: UserDataPathProvider,
) {
    pasteItems.forEach {
        it.clear(clearResource, userDataPathProvider)
    }
}

fun PasteCollection.bindItems(
    pasteCoordinate: PasteCoordinate,
    destinationPath: String? = null,
): PasteCollection =
    PasteCollection(
        pasteItems.map {
            it.bindItem(pasteCoordinate, destinationPath)
        },
    )
