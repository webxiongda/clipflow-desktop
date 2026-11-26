package com.clipflow.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import com.clipflow.app.generated.resources.Res
import com.clipflow.app.generated.resources.Subset_RobotoCondensed_Medium
import org.jetbrains.compose.resources.Font

@Composable
actual fun robotoFontFamily(): FontFamily =
    FontFamily(
        Font(Res.font.Subset_RobotoCondensed_Medium),
    )
