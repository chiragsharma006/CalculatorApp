package com.calculator.app.domain.tools

import java.math.BigInteger

enum class NumberBase(val radix: Int, val displayName: String, val prefix: String) {
    DECIMAL(10, "Decimal (DEC)", "10"),
    HEXADECIMAL(16, "Hexadecimal (HEX)", "0x"),
    OCTAL(8, "Octal (OCT)", "0o"),
    BINARY(2, "Binary (BIN)", "0b")
}

data class NumberSystemResult(
    val decimal: String,
    val hexadecimal: String,
    val octal: String,
    val binary: String,
    val isValid: Boolean = true,
    val errorMessage: String? = null
)

object NumberSystemEngine {

    fun isValidChar(char: Char, base: NumberBase): Boolean {
        return when (base) {
            NumberBase.BINARY -> char in '0'..'1'
            NumberBase.OCTAL -> char in '0'..'7'
            NumberBase.DECIMAL -> char in '0'..'9'
            NumberBase.HEXADECIMAL -> char in '0'..'9' || char in 'a'..'f' || char in 'A'..'F'
        }
    }

    fun convertAll(input: String, fromBase: NumberBase): NumberSystemResult {
        val clean = input.replace(" ", "").trim()
        if (clean.isEmpty()) {
            return NumberSystemResult(
                decimal = "0",
                hexadecimal = "0",
                octal = "0",
                binary = "0"
            )
        }

        return try {
            val bigInt = BigInteger(clean, fromBase.radix)
            NumberSystemResult(
                decimal = bigInt.toString(10),
                hexadecimal = bigInt.toString(16).uppercase(),
                octal = bigInt.toString(8),
                binary = formatBinaryWithSpaces(bigInt.toString(2))
            )
        } catch (e: Exception) {
            NumberSystemResult(
                decimal = "Invalid",
                hexadecimal = "Invalid",
                octal = "Invalid",
                binary = "Invalid",
                isValid = false,
                errorMessage = "Invalid input for ${fromBase.displayName}"
            )
        }
    }

    private fun formatBinaryWithSpaces(bin: String): String {
        val reversed = bin.reversed()
        val chunked = reversed.chunked(4).joinToString(" ")
        return chunked.reversed()
    }
}
