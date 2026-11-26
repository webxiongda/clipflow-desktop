package com.clipflow.ui.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.clipflow.ui.theme.ClipFlowBrand

@Composable
expect fun ClipFlowLogoView(
    size: Dp,
    modifier: Modifier = Modifier,
    enableDebugToggle: Boolean = false,
)

/** The ClipFlow logo: the complete white glyph on the brand-gradient tile. */
@Composable
fun ClipFlowLogo(
    size: Dp,
    modifier: Modifier = Modifier,
) {
    LogoTile(size, modifier) { glyphModifier ->
        ClipFlowGlyph(progress = { 1f }, color = Color.White, modifier = glyphModifier)
    }
}

/** [ClipFlowLogo] whose glyph traces itself once each time it enters the composition. */
@Composable
fun AnimatedClipFlowLogo(
    size: Dp,
    modifier: Modifier = Modifier,
) {
    LogoTile(size, modifier) { glyphModifier ->
        AnimatedClipFlowGlyph(color = Color.White, modifier = glyphModifier)
    }
}

@Composable
private fun LogoTile(
    size: Dp,
    modifier: Modifier,
    glyph: @Composable (Modifier) -> Unit,
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(size / 4))
                .background(ClipFlowBrand.gradient)
                .size(size)
                .semantics { contentDescription = "ClipFlow Logo" },
    ) {
        glyph(
            Modifier
                .fillMaxSize()
                .padding(size * 0.1f),
        )
    }
}
