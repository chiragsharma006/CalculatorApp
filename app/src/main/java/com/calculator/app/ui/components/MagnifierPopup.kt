package com.calculator.app.ui.components

import android.os.Build
import androidx.compose.foundation.magnifier
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.TextLayoutResult

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
 * Attaches the Android native system magnifier (identical to the Finance tool text fields)
 * to the draggable cursor.
 *
 * Renders an OS-level magnifying glass window that magnifies the exact pixels on screen
 * and is never clipped by card borders, rounded corners, top app bars, or parent containers.
 */
fun Modifier.cursorMagnifier(
    enabled: Boolean,
    sourceCenter: () -> Offset
): Modifier {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        this.magnifier(
            sourceCenter = {
                if (enabled) sourceCenter() else Offset.Unspecified
            }
        )
    } else {
        this
    }
}
