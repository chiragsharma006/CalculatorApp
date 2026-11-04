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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.calculator.app.ui.theme.LocalCalculatorColors

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
 * Calculates window position for the floating magnified loupe popup.
 * Floats directly above [anchorOffset], clamped within window margins so it is never clipped.
 */
class MagnifierPositionProvider(
    private val anchorOffset: Offset,
    private val density: Density
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val targetX = anchorBounds.left + anchorOffset.x.toInt()
        val targetY = anchorBounds.top + anchorOffset.y.toInt()

        val marginPx = with(density) { 10.dp.roundToPx() }
        val maxX = (windowSize.width - popupContentSize.width - marginPx).coerceAtLeast(marginPx)
        val x = (targetX - popupContentSize.width / 2).coerceIn(marginPx, maxX)

        // Float directly above anchor
        val clearancePx = with(density) { 16.dp.roundToPx() }
        val yAbove = targetY - popupContentSize.height - clearancePx
        val y = if (yAbove < marginPx + 40) {
            (targetY + clearancePx + 24).coerceAtMost(windowSize.height - popupContentSize.height - marginPx)
        } else {
            yAbove
        }

        return IntOffset(x, y)
    }
}

/**
 * Floating magnified loupe bubble that appears directly above the cursor while dragging.
 * Renders in a separate window popup so it is never clipped by card borders or parent containers.
 * Enlarges the surrounding digits and renders a high-visibility cursor bar in the center
 * so users can place the cursor with pixel-perfect confidence.
 */
@Composable
fun MagnifierPopup(
    visible: Boolean,
    displayText: String,
    cursorIndex: Int,
    anchorOffset: Offset,
    modifier: Modifier = Modifier,
    cursorColor: Color = LocalCalculatorColors.current.equalsKeyBg
) {
    if (!visible || displayText.isEmpty()) return

    val colors = LocalCalculatorColors.current
    val density = LocalDensity.current

    val positionProvider = remember(anchorOffset, density) {
        MagnifierPositionProvider(anchorOffset, density)
    }

    val clampedCursor = cursorIndex.coerceIn(0, displayText.length)
    val windowRadius = 4
    val start = (clampedCursor - windowRadius).coerceAtLeast(0)
    val end = (clampedCursor + windowRadius).coerceAtMost(displayText.length)

    val beforeText = displayText.substring(start, clampedCursor)
    val afterText = displayText.substring(clampedCursor, end)
    val hasMoreBefore = start > 0
    val hasMoreAfter = end < displayText.length

    Popup(
        popupPositionProvider = positionProvider,
        properties = PopupProperties(
            focusable = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(100)) + scaleIn(tween(100), initialScale = 0.85f),
            exit = fadeOut(tween(100)) + scaleOut(tween(100), targetScale = 0.85f),
            modifier = modifier
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main Loupe Capsule
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = colors.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 10.dp,
                    border = BorderStroke(1.5.dp, cursorColor.copy(alpha = 0.85f)),
                    modifier = Modifier.shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(22.dp),
                        spotColor = cursorColor.copy(alpha = 0.5f)
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
                    modifier = Modifier.size(width = 12.dp, height = 6.dp)
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
