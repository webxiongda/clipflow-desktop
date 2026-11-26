package com.clipflow.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize

@Composable
fun ImageResolution(
    imageSize: IntSize?,
    modifier: Modifier = Modifier,
) {
    if (imageSize == null || imageSize.width <= 0 || imageSize.height <= 0) {
        return
    }

    ImageInfoLabel(
        text = "${imageSize.width} × ${imageSize.height}",
        modifier = modifier,
    )
}
