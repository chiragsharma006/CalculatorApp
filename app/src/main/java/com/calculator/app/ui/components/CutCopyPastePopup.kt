package com.calculator.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.calculator.app.ui.theme.LocalCalculatorColors

/**
 * Calculates position for a floating action popup relative to the window.
 * Positions directly above [anchorOffset], or flips below if too close to top edge.
 * Clamps within screen margins so it never gets clipped.
 */
class FloatingActionPositionProvider(
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

        val marginPx = with(density) { 16.dp.roundToPx() }
        // Center horizontally on anchor, clamped to window margins
        val maxX = (windowSize.width - popupContentSize.width - marginPx).coerceAtLeast(marginPx)
        val x = (targetX - popupContentSize.width / 2).coerceIn(marginPx, maxX)

        // Float above anchor, or below if too near top edge
        val clearancePx = with(density) { 36.dp.roundToPx() }
        val yAbove = targetY - popupContentSize.height - clearancePx
        val y = if (yAbove < marginPx + 40) {
            (targetY + clearancePx).coerceAtMost(windowSize.height - popupContentSize.height - marginPx)
        } else {
            yAbove
        }

        return IntOffset(x, y)
    }
}

/**
 * A floating CUT / COPY / PASTE action toolbar popup.
 *
 * Appears as an elevated pill above the cursor or touch target when the user
 * taps the cursor handle or long-presses on input text across the app.
 */
@Composable
fun CutCopyPastePopup(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    anchorOffset: Offset,
    onCut: (() -> Unit)? = null,
    onCopy: (() -> Unit)? = null,
    onPaste: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (!visible) return
    val density = LocalDensity.current
    val colors = LocalCalculatorColors.current
    val positionProvider = remember(anchorOffset, density) {
        FloatingActionPositionProvider(anchorOffset, density)
    }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(
            focusable = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.operatorKeyText.copy(alpha = 0.35f)),
            modifier = modifier
                .shadow(12.dp, RoundedCornerShape(22.dp))
        ) {
            Row(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var hasPrevious = false

                if (onCut != null) {
                    PopupActionButton(
                        icon = Icons.Default.ContentCut,
                        label = "Cut",
                        onClick = {
                            onCut()
                            onDismissRequest()
                        }
                    )
                    hasPrevious = true
                }

                if (onCopy != null) {
                    if (hasPrevious) {
                        VerticalDivider(
                            modifier = Modifier
                                .height(18.dp)
                                .padding(horizontal = 2.dp),
                            color = colors.textSecondary.copy(alpha = 0.25f)
                        )
                    }
                    PopupActionButton(
                        icon = Icons.Default.ContentCopy,
                        label = "Copy",
                        onClick = {
                            onCopy()
                            onDismissRequest()
                        }
                    )
                    hasPrevious = true
                }

                if (onPaste != null) {
                    if (hasPrevious) {
                        VerticalDivider(
                            modifier = Modifier
                                .height(18.dp)
                                .padding(horizontal = 2.dp),
                            color = colors.textSecondary.copy(alpha = 0.25f)
                        )
                    }
                    PopupActionButton(
                        icon = Icons.Default.ContentPaste,
                        label = "Paste",
                        onClick = {
                            onPaste()
                            onDismissRequest()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PopupActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    Row(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = colors.operatorKeyText),
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = colors.operatorKeyText,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary
        )
    }
}
