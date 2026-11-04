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
            return TransformedText(text, OffsetMapping.Identity)
        }
        val result = NumberFormatter.formatExpression(text.text, system)
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return NumberFormatter.rawToFormattedIndex(offset, result)
            }

            override fun transformedToOriginal(offset: Int): Int {
                return NumberFormatter.formattedToRawIndex(offset, result)
            }
        }
        return TransformedText(AnnotatedString(result.formattedText), offsetMapping)
    }
}
