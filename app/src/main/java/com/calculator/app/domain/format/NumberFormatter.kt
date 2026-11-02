package com.calculator.app.domain.format

import com.calculator.app.data.preferences.NumberingSystem
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs

data class FormattedExpressionResult(
    val formattedText: String,
    val rawToFormattedMap: IntArray,
    val formattedToRawMap: IntArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FormattedExpressionResult
        return formattedText == other.formattedText
    }

    override fun hashCode(): Int {
        return formattedText.hashCode()
    }
}

object NumberFormatter {

    /**
     * Formats an integer string (digits only) according to the specified numbering system.
     */
    fun formatIntegerString(digits: String, system: NumberingSystem): String {
        if (digits.length <= 3) return digits

        return when (system) {
            NumberingSystem.INTERNATIONAL -> {
                val sb = StringBuilder()
                val len = digits.length
                for (i in 0 until len) {
                    if (i > 0 && (len - i) % 3 == 0) {
                        sb.append(',')
                    }
                    sb.append(digits[i])
                }
                sb.toString()
            }
            NumberingSystem.INDIAN -> {
                val lastThree = digits.takeLast(3)
                val rest = digits.dropLast(3)
                val restLen = rest.length
                val sb = StringBuilder()
                for (i in 0 until restLen) {
                    if (i > 0 && (restLen - i) % 2 == 0) {
                        sb.append(',')
                    }
                    sb.append(rest[i])
                }
                if (rest.isNotEmpty()) {
                    sb.append(',')
                }
                sb.append(lastThree)
                sb.toString()
            }
        }
    }

    /**
     * Formats a numeric string (which may have negative sign or decimal point) with grouping separators.
     */
    fun formatNumber(numberStr: String, system: NumberingSystem): String {
        if (numberStr.isEmpty()) return ""
        if (numberStr == "Error" || numberStr.contains("Infinity") || numberStr.contains("NaN")) {
            return numberStr
        }
        if (numberStr.contains("× 10^") || numberStr.contains("e", ignoreCase = true)) {
            return numberStr
        }

        val isNegative = numberStr.startsWith("-")
        val raw = if (isNegative) numberStr.substring(1) else numberStr

        val parts = raw.split('.', limit = 2)
        val integerPart = parts[0]
        val fractionalPart = if (parts.size > 1) "." + parts[1] else ""

        val formattedInt = if (integerPart.isNotEmpty() && integerPart.all { it.isDigit() }) {
            formatIntegerString(integerPart, system)
        } else {
            integerPart
        }

        return (if (isNegative) "-" else "") + formattedInt + fractionalPart
    }

    /**
     * Formats a double value according to the specified numbering system.
     */
    fun formatDouble(value: Double, system: NumberingSystem, maxFractionDigits: Int = 8): String {
        if (value.isNaN()) return "Error"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"

        val bd = BigDecimal(value.toString())
        val rounded = bd.setScale(maxFractionDigits, RoundingMode.HALF_UP).stripTrailingZeros()
        return formatNumber(rounded.toPlainString(), system)
    }

    /**
     * Formats currency amounts with exact 2 decimal places and proper commas.
     */
    fun formatCurrency(value: Double, system: NumberingSystem, decimals: Int = 2): String {
        if (value.isNaN() || value.isInfinite()) return "0.00"

        val bd = BigDecimal(value.toString()).setScale(decimals, RoundingMode.HALF_UP)
        val plain = bd.toPlainString()
        return formatNumber(plain, system)
    }

    /**
     * Formats whole numbers (e.g. for counters, days, weeks).
     */
    fun formatInteger(value: Long, system: NumberingSystem): String {
        return formatNumber(value.toString(), system)
    }

    /**
     * Formats all numeric literals inside an expression string and builds bidirectional index mappings
     * so cursor position and touch-to-cursor placement remain 100% accurate.
     */
    fun formatExpression(raw: String, system: NumberingSystem): FormattedExpressionResult {
        if (raw.isEmpty()) {
            return FormattedExpressionResult("", intArrayOf(0), intArrayOf(0))
        }

        val formattedSb = StringBuilder()
        val rawToFormatted = IntArray(raw.length + 1)
        val formattedToRaw = ArrayList<Int>()

        var i = 0
        while (i < raw.length) {
            val ch = raw[i]

            // Check if this is the start of a number literal (ASCII digit '0'..'9')
            if (ch.isDigit()) {
                val start = i
                var dotFound = false
                var end = start

                while (end < raw.length) {
                    val c = raw[end]
                    if (c.isDigit()) {
                        end++
                    } else if (c == '.' && !dotFound) {
                        dotFound = true
                        end++
                    } else {
                        break
                    }
                }

                val numStr = raw.substring(start, end)
                val dotIndexInNum = numStr.indexOf('.')
                val intPart = if (dotIndexInNum >= 0) numStr.substring(0, dotIndexInNum) else numStr
                val intLen = intPart.length

                // Format the integer part digit-by-digit while tracking exact indices
                for (dIdx in 0 until intLen) {
                    val currentRawIndex = start + dIdx

                    // Determine if comma is needed before this digit
                    val needsComma = when (system) {
                        NumberingSystem.INTERNATIONAL -> {
                            dIdx > 0 && (intLen - dIdx) % 3 == 0
                        }
                        NumberingSystem.INDIAN -> {
                            if (intLen <= 3) false
                            else {
                                val restLen = intLen - 3
                                if (dIdx < restLen) {
                                    dIdx > 0 && (restLen - dIdx) % 2 == 0
                                } else if (dIdx == restLen) {
                                    true
                                } else {
                                    false
                                }
                            }
                        }
                    }

                    if (needsComma) {
                        formattedToRaw.add(currentRawIndex)
                        formattedSb.append(',')
                    }

                    rawToFormatted[currentRawIndex] = formattedSb.length
                    formattedToRaw.add(currentRawIndex)
                    formattedSb.append(intPart[dIdx])
                }

                // Append any fractional part (including dot)
                if (dotIndexInNum >= 0) {
                    for (fIdx in dotIndexInNum until numStr.length) {
                        val currentRawIndex = start + fIdx
                        rawToFormatted[currentRawIndex] = formattedSb.length
                        formattedToRaw.add(currentRawIndex)
                        formattedSb.append(numStr[fIdx])
                    }
                }

                i = end
            } else {
                // Non-digit character (operators, brackets, functions, spaces, constants)
                rawToFormatted[i] = formattedSb.length
                formattedToRaw.add(i)
                formattedSb.append(ch)
                i++
            }
        }

        // Map the end-of-string cursor position
        rawToFormatted[raw.length] = formattedSb.length
        formattedToRaw.add(raw.length)

        return FormattedExpressionResult(
            formattedText = formattedSb.toString(),
            rawToFormattedMap = rawToFormatted,
            formattedToRawMap = formattedToRaw.toIntArray()
        )
    }

    /**
     * Translates a cursor index in the raw expression to its corresponding position in the formatted string.
     */
    fun rawToFormattedIndex(rawIndex: Int, result: FormattedExpressionResult): Int {
        val map = result.rawToFormattedMap
        if (map.isEmpty()) return 0
        val clamped = rawIndex.coerceIn(0, map.size - 1)
        return map[clamped]
    }

    /**
     * Translates a cursor index from the formatted string back to its position in the raw expression.
     */
    fun formattedToRawIndex(formattedIndex: Int, result: FormattedExpressionResult): Int {
        val map = result.formattedToRawMap
        if (map.isEmpty()) return 0
        val clamped = formattedIndex.coerceIn(0, map.size - 1)
        return map[clamped]
    }
}
