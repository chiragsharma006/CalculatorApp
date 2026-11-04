package com.calculator.app.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.calculator.app.data.preferences.NumberingSystem
import com.calculator.app.domain.format.NumberFormatter

/**
 * VisualTransformation that formats numbers within expressions using the user's
 * chosen [system] (International or Indian numbering system), while preserving
 * accurate bidirectional cursor mapping for Android's native text editor and magnifier.
 */
class ExpressionVisualTransformation(
    private val system: NumberingSystem
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        if (text.text.isEmpty()) {
            val emptyOffsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int = 0
                override fun transformedToOriginal(offset: Int): Int = 0
            }
            // Keep the same trailing thin space (\u2009) in empty state so the initial
            // cursor sits at the exact same position as after entering digits, preventing cursor jumps.
            return TransformedText(AnnotatedString("\u2009"), emptyOffsetMapping)
        }
        val result = NumberFormatter.formatExpression(text.text, system)
        // Append a thin space (\u2009) so that when the cursor is at the very end of the expression,
        // it is treated as an internal position (identical to when positioned between characters),
        // preventing Android Compose's text layout from clamping or misaligning the cursor bar with the drop handle.
        val transformed = result.formattedText + "\u2009"
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, text.text.length)
                val mapped = NumberFormatter.rawToFormattedIndex(clamped, result)
                return mapped.coerceAtMost(result.formattedText.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, transformed.length)
                if (clamped >= result.formattedText.length) {
                    return text.text.length
                }
                return NumberFormatter.formattedToRawIndex(clamped, result)
            }
        }
        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}
