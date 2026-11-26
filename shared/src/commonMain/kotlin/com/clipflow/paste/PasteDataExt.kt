package com.clipflow.paste

import com.clipflow.paste.item.UrlPasteItem
import com.clipflow.paste.item.clear
import com.clipflow.paste.item.clearRenderingFiles
import com.clipflow.path.UserDataPathProvider

fun PasteData.clear(userDataPathProvider: UserDataPathProvider) {
    (pasteAppearItem as? UrlPasteItem)?.clearRenderingFiles(
        pasteCoordinate = getPasteCoordinate(),
        userDataPathProvider = userDataPathProvider,
    )
    pasteAppearItem?.clear(
        userDataPathProvider = userDataPathProvider,
    )
    pasteCollection.clear(
        userDataPathProvider = userDataPathProvider,
    )
    userDataPathProvider.deletePasteDirectories(this)
}
