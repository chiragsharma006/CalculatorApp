package com.calculator.app

import com.calculator.app.data.preferences.NumberingSystem
import com.calculator.app.domain.format.NumberFormatter
import org.junit.Assert.*
import org.junit.Test

class NumberFormatterTest {

    @Test
    fun testInternationalIntegerFormatting() {
        assertEquals("0", NumberFormatter.formatIntegerString("0", NumberingSystem.INTERNATIONAL))
        assertEquals("999", NumberFormatter.formatIntegerString("999", NumberingSystem.INTERNATIONAL))
        assertEquals("1,000", NumberFormatter.formatIntegerString("1000", NumberingSystem.INTERNATIONAL))
        assertEquals("10,000", NumberFormatter.formatIntegerString("10000", NumberingSystem.INTERNATIONAL))
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
}
