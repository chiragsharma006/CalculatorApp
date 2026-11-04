package com.calculator.app

import androidx.compose.ui.text.AnnotatedString
import com.calculator.app.data.preferences.NumberingSystem
import com.calculator.app.domain.format.NumberFormatter
import com.calculator.app.ui.components.ExpressionVisualTransformation
import org.junit.Assert.*
import org.junit.Test

class NumberFormatterTest {

    @Test
    fun testInternationalIntegerFormatting() {
        assertEquals("0", NumberFormatter.formatIntegerString("0", NumberingSystem.INTERNATIONAL))
        assertEquals("999", NumberFormatter.formatIntegerString("999", NumberingSystem.INTERNATIONAL))
        assertEquals("1,000", NumberFormatter.formatIntegerString("1000", NumberingSystem.INTERNATIONAL))
        assertEquals("10,00,000", NumberFormatter.formatIntegerString("1000000", NumberingSystem.INDIAN))
        assertEquals("100,000", NumberFormatter.formatIntegerString("100000", NumberingSystem.INTERNATIONAL))
        assertEquals("1,000,000", NumberFormatter.formatIntegerString("1000000", NumberingSystem.INTERNATIONAL))
        assertEquals("12,345,678", NumberFormatter.formatIntegerString("12345678", NumberingSystem.INTERNATIONAL))
    }

    @Test
    fun testIndianIntegerFormatting() {
        assertEquals("0", NumberFormatter.formatIntegerString("0", NumberingSystem.INDIAN))
        assertEquals("999", NumberFormatter.formatIntegerString("999", NumberingSystem.INDIAN))
        assertEquals("1,000", NumberFormatter.formatIntegerString("1000", NumberingSystem.INDIAN))
        assertEquals("10,000", NumberFormatter.formatIntegerString("10000", NumberingSystem.INDIAN))
        assertEquals("1,00,000", NumberFormatter.formatIntegerString("100000", NumberingSystem.INDIAN)) // 1 Lakh
        assertEquals("10,00,000", NumberFormatter.formatIntegerString("1000000", NumberingSystem.INDIAN)) // 10 Lakh
        assertEquals("1,00,00,000", NumberFormatter.formatIntegerString("10000000", NumberingSystem.INDIAN)) // 1 Crore
        assertEquals("1,23,45,678", NumberFormatter.formatIntegerString("12345678", NumberingSystem.INDIAN))
    }

    @Test
    fun testFormatNumberWithDecimalsAndNegative() {
        assertEquals("10,00,000.5", NumberFormatter.formatNumber("1000000.5", NumberingSystem.INDIAN))
        assertEquals("1,000,000.5", NumberFormatter.formatNumber("1000000.5", NumberingSystem.INTERNATIONAL))
        assertEquals("-10,00,000.50", NumberFormatter.formatNumber("-1000000.50", NumberingSystem.INDIAN))
        assertEquals("-1,000,000.50", NumberFormatter.formatNumber("-1000000.50", NumberingSystem.INTERNATIONAL))
    }

    @Test
    fun testFormatExpression() {
        val raw = "1000000 + 50000"
        val indianRes = NumberFormatter.formatExpression(raw, NumberingSystem.INDIAN)
        assertEquals("10,00,000 + 50,000", indianRes.formattedText)

        val intlRes = NumberFormatter.formatExpression(raw, NumberingSystem.INTERNATIONAL)
        assertEquals("1,000,000 + 50,000", intlRes.formattedText)

        // Check cursor mapping for end of string
        val rawEnd = raw.length
        val mappedEnd = NumberFormatter.rawToFormattedIndex(rawEnd, indianRes)
        assertEquals(indianRes.formattedText.length, mappedEnd)

        // Reverse map
        val reverseRaw = NumberFormatter.formattedToRawIndex(mappedEnd, indianRes)
        assertEquals(rawEnd, reverseRaw)
    }

    @Test
    fun testFormatExpressionWithFunctions() {
        val raw = "sin(45) + 100000"
        val indianRes = NumberFormatter.formatExpression(raw, NumberingSystem.INDIAN)
        assertEquals("sin(45) + 1,00,000", indianRes.formattedText)
    }

    @Test
    fun testExpressionVisualTransformationOffsetMapping() {
        val transform = ExpressionVisualTransformation(NumberingSystem.INTERNATIONAL)
        val rawText = "888"
        val result = transform.filter(AnnotatedString(rawText))
        val offsetMapping = result.offsetMapping

        // Original "888" -> Transformed "888\u2009"
        assertEquals("888\u2009", result.text.text)

        // Offset 0: before first 8
        assertEquals(0, offsetMapping.originalToTransformed(0))
        assertEquals(0, offsetMapping.transformedToOriginal(0))

        // Offset 1: between 1st and 2nd 8
        assertEquals(1, offsetMapping.originalToTransformed(1))
        assertEquals(1, offsetMapping.transformedToOriginal(1))

        // Offset 2: between 2nd and 3rd 8 (Image 1 case)
        assertEquals(2, offsetMapping.originalToTransformed(2))
        assertEquals(2, offsetMapping.transformedToOriginal(2))

        // Offset 3: after 3rd 8 (Image 2 case) - maps before trailing \u2009
        assertEquals(3, offsetMapping.originalToTransformed(3))
        assertEquals(3, offsetMapping.transformedToOriginal(3))

        // Clicking on or after the trailing space also resolves to the end of the raw text
        assertEquals(3, offsetMapping.transformedToOriginal(4))

        // Empty text case: ensures initial cursor position aligns with single digits
        val emptyResult = transform.filter(AnnotatedString(""))
        assertEquals("\u2009", emptyResult.text.text)
        assertEquals(0, emptyResult.offsetMapping.originalToTransformed(0))
        assertEquals(0, emptyResult.offsetMapping.transformedToOriginal(0))
        assertEquals(0, emptyResult.offsetMapping.transformedToOriginal(1))
    }
}
