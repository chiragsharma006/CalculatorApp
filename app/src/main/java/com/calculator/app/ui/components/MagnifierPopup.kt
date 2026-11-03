package com.calculator.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calculator.app.ui.theme.LocalCalculatorColors
import kotlin.math.roundToInt

/**
 * Safely queries the cursor rectangle from a [TextLayoutResult] without throwing
 * [IllegalArgumentException] when layout is stale or recomposition occurs before layout.
 */
fun TextLayoutResult?.getCursorRectSafe(offset: Int): Rect? {
    if (this == null) return null
    val textLen = layoutInput.text.length
    if (textLen < 0) return null
    val safeOffset = offset.coerceIn(0, textLen)
    return try {
        getCursorRect(safeOffset)
    } catch (_: Throwable) {
        null
    }
}

/**
 * Floating magnified loupe bubble that appears directly above the cursor while dragging.
 * Enlarges the surrounding digits and renders a high-visibility cursor bar in the center
 * so users can place the cursor with pixel-perfect confidence.
 */
@Composable
fun MagnifierPopup(
    visible: Boolean,
    displayText: String,
    cursorIndex: Int,
    cursorX: Float,
    containerWidth: Float,
    modifier: Modifier = Modifier,
    cursorColor: Color = LocalCalculatorColors.current.equalsKeyBg,
    yOffset: Dp = (-58).dp
) {
    val colors = LocalCalculatorColors.current
    val density = LocalDensity.current

    var popupWidthPx by remember { mutableFloatStateOf(0f) }

    val textLength = displayText.length
    val clampedCursor = if (textLength > 0) cursorIndex.coerceIn(0, textLength) else 0
    val windowRadius = 4
    val start = (clampedCursor - windowRadius).coerceAtLeast(0)
    val end = (clampedCursor + windowRadius).coerceAtMost(textLength)

    val beforeText = if (textLength > 0 && start in 0..clampedCursor && clampedCursor <= textLength) {
        displayText.substring(start, clampedCursor)
    } else ""
    val afterText = if (textLength > 0 && clampedCursor in 0..end && end <= textLength) {
        displayText.substring(clampedCursor, end)
    } else ""
    val hasMoreBefore = start > 0
    val hasMoreAfter = end < textLength

    val marginPx = with(density) { 8.dp.toPx() }
    val yOffsetPx = with(density) { yOffset.toPx() }.roundToInt()
    val estimatedWidthPx = with(density) { 110.dp.toPx() }
    val effectiveWidth = if (popupWidthPx > 0) popupWidthPx else estimatedWidthPx

    val idealTargetX = cursorX - (effectiveWidth / 2f)
    val targetXPx = if (containerWidth > 0) {
        idealTargetX.coerceIn(marginPx, (containerWidth - effectiveWidth - marginPx).coerceAtLeast(marginPx))
    } else {
        idealTargetX.coerceAtLeast(marginPx)
    }

    // Pointer offset relative to the popup box
    val pointerWidthPx = with(density) { 12.dp.toPx() }
    val pointerMarginPx = with(density) { 14.dp.toPx() }
    val idealPointerX = cursorX - targetXPx - (pointerWidthPx / 2f)
    val pointerXPx = idealPointerX.coerceIn(
        pointerMarginPx,
        (effectiveWidth - pointerMarginPx - pointerWidthPx).coerceAtLeast(pointerMarginPx)
    )

    AnimatedVisibility(
        visible = visible && displayText.isNotEmpty(),
        enter = fadeIn(tween(120)) + scaleIn(tween(120), initialScale = 0.85f),
        exit = fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.85f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(targetXPx.roundToInt(), yOffsetPx) }
                .onSizeChanged { popupWidthPx = it.width.toFloat() }
        ) {
            Column {
                // Main Loupe Bubble
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = colors.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 10.dp,
                    border = BorderStroke(1.5.dp, cursorColor.copy(alpha = 0.7f)),
                    modifier = Modifier.shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(22.dp),
                        spotColor = cursorColor.copy(alpha = 0.45f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Text slice before cursor
                        Text(
                            text = (if (hasMoreBefore) "…" else "") + beforeText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = colors.textPrimary
                        )

                        // Center magnified vertical cursor bar
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 2.5.dp)
                                .width(3.dp)
                                .height(26.dp)
                                .background(cursorColor, RoundedCornerShape(1.5.dp))
                        )

                        // Text slice after cursor
                        Text(
                            text = afterText + (if (hasMoreAfter) "…" else ""),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = colors.textPrimary
                        )
                    }
                }

                // Small downward pointer triangle pointing directly toward cursor
                Canvas(
                    modifier = Modifier
                        .offset { IntOffset(pointerXPx.roundToInt(), 0) }
                        .size(width = 12.dp, height = 6.dp)
                ) {
                    val path = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width / 2f, size.height)
                        close()
                    }
                    drawPath(path, color = cursorColor.copy(alpha = 0.85f))
                }
            }
        }
    }
}
