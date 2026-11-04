package com.calculator.app.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import kotlin.math.*

class MathEvaluator {

    companion object {
        const val PI_VALUE = Math.PI
        const val E_VALUE = Math.E
    }

    fun evaluate(expression: String, isDegreeMode: Boolean = true): EvalResult {
        val cleanExpr = expression.replace(",", "").trim()
        if (cleanExpr.isEmpty()) {
            return EvalResult.Error("Empty expression")
        }

        // If expression ends with an arithmetic operator (+, -, ×, ÷, *, /)
        if (isArithmeticOperator(cleanExpr.last())) {
            val candidate = cleanExpr.dropLast(1).trimEnd()
            if (candidate.isNotEmpty() && !isArithmeticOperator(candidate.last())) {
                try {
                    val tokens = tokenize(candidate)
                    if (hasBinaryOperation(tokens)) {
                        return evaluateDirect(candidate, isDegreeMode)
                    }
                } catch (_: Exception) {
                    // Fall through to Incomplete expression
                }
            }
            return EvalResult.Error("Incomplete expression")
        }

        return evaluateDirect(cleanExpr, isDegreeMode)
    }

    private fun isArithmeticOperator(c: Char): Boolean {
        return c == '+' || c == '-' || c == '−' || c == '*' || c == '×' || c == '/' || c == '÷'
    }

    private fun tokenCanEndOperand(token: Token): Boolean {
        return when (token.type) {
            TokenType.NUMBER,
            TokenType.RPAREN,
            TokenType.CONSTANT_PI,
            TokenType.CONSTANT_E,
            TokenType.FACTORIAL,
            TokenType.PERCENT -> true
            else -> false
        }
    }

    private fun hasBinaryOperation(tokens: List<Token>): Boolean {
        for (i in tokens.indices) {
            val token = tokens[i]
            when (token.type) {
                TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.POWER -> {
                    if (i > 0 && tokenCanEndOperand(tokens[i - 1])) {
                        return true
                    }
                }
                TokenType.PLUS, TokenType.MINUS -> {
                    if (i > 0 && tokenCanEndOperand(tokens[i - 1])) {
                        return true
                    }
                }
                else -> {}
            }
        }
        return false
    }

    private fun evaluateDirect(cleanExpr: String, isDegreeMode: Boolean = true): EvalResult {
        return try {
            val tokens = tokenize(cleanExpr)
            if (tokens.isEmpty()) {
                return EvalResult.Error("Empty expression")
            }
            val parser = Parser(tokens, isDegreeMode)
            val result = parser.parseExpression()
            if (parser.hasNext()) {
                return EvalResult.Error("Unexpected character: ${parser.peek().text}")
            }
            if (result.isNaN()) {
                return EvalResult.Error("Result is undefined")
            }
            if (result.isInfinite()) {
                return EvalResult.Error("Cannot divide by zero")
            }
            EvalResult.Success(result, formatResult(result))
        } catch (e: ArithmeticException) {
            EvalResult.Error(e.message ?: "Math error")
        } catch (e: IllegalArgumentException) {
            EvalResult.Error(e.message ?: "Invalid format")
        } catch (e: Exception) {
            EvalResult.Error("Invalid syntax")
        }
    }

    enum class TokenType {
        NUMBER, PLUS, MINUS, MULTIPLY, DIVIDE, POWER, PERCENT, FACTORIAL, SQRT, CBRT,
        LPAREN, RPAREN, CONSTANT_PI, CONSTANT_E,
        SIN, COS, TAN, ASIN, ACOS, ATAN,
        SINH, COSH, TANH, ASINH, ACOSH, ATANH,
        LN, LOG, ABS
    }

    data class Token(val type: TokenType, val text: String, val value: Double = 0.0)

    private fun tokenize(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        val s = input.replace("×", "*").replace("÷", "/").replace("−", "-")

        while (i < s.length) {
            val c = s[i]
            when {
                c.isWhitespace() -> {
                    i++
                }
                s.startsWith("sinh⁻¹", i) -> {
                    if (shouldInsertImplicitMultiply(tokens)) tokens.add(Token(TokenType.MULTIPLY, "*"))
                    tokens.add(Token(TokenType.ASINH, "sinh⁻¹"))
                    i += 6
                }
                s.startsWith("asinh", i) -> {
                    if (shouldInsertImplicitMultiply(tokens)) tokens.add(Token(TokenType.MULTIPLY, "*"))
                    tokens.add(Token(TokenType.ASINH, "asinh"))
                    i += 5
                }
                s.startsWith("cosh⁻¹", i) -> {
                    if (shouldInsertImplicitMultiply(tokens)) tokens.add(Token(TokenType.MULTIPLY, "*"))
                    tokens.add(Token(TokenType.ACOSH, "cosh⁻¹"))
                    i += 6
                }
                s.startsWith("acosh", i) -> {
                    if (shouldInsertImplicitMultiply(tokens)) tokens.add(Token(TokenType.MULTIPLY, "*"))
                    tokens.add(Token(TokenType.ACOSH, "acosh"))
                    i += 5
                }
                s.startsWith("tanh⁻¹", i) -> {
                    if (shouldInsertImplicitMultiply(tokens)) tokens.add(Token(TokenType.MULTIPLY, "*"))
                    tokens.add(Token(TokenType.ATANH, "tanh⁻¹"))
                    i += 6
                }
                s.startsWith("atanh", i) -> {
                    if (shouldInsertImplicitMultiply(tokens)) tokens.add(Token(TokenType.MULTIPLY, "*"))
                    tokens.add(Token(TokenType.ATANH, "atanh"))
                    i += 5
                }
                s.startsWith("sinh", i) -> {
                    if (shouldInsertImplicitMultiply(tokens)) tokens.add(Token(TokenType.MULTIPLY, "*"))
                    tokens.add(Token(TokenType.SINH, "sinh"))
                    i += 4
                }
                s.startsWith("cosh", i) -> {
                    if (shouldInsertImplicitMultiply(tokens)) tokens.add(Token(TokenType.MULTIPLY, "*"))
                    tokens.add(Token(TokenType.COSH, "cosh"))
                    i += 4
                }
                s.startsWith("tanh", i) -> {
                    if (shouldInsertImplicitMultiply(tokens)) tokens.add(Token(TokenType.MULTIPLY, "*"))
                    tokens.add(Token(TokenType.TANH, "tanh"))
                    i += 4
                }
                s.startsWith("sin⁻¹", i) -> {
                    if (shouldInsertImplicitMultiply(tokens)) tokens.add(Token(TokenType.MULTIPLY, "*"))
                    tokens.add(Token(TokenType.ASIN, "sin⁻¹"))
                    i += 5
                }
                s.startsWith("asin", i) -> {
                    if (shouldInsertImplicitMultiply(tokens)) tokens.add(Token(TokenType.MULTIPLY, "*"))
                    tokens.add(Token(TokenType.ASIN, "asin"))
                    i += 4
                }
                s.startsWith("cos⁻¹", i) -> {
                    if (shouldInsertImplicitMultiply(tokens)) tokens.add(Token(TokenType.MULTIPLY, "*"))
                    tokens.add(Token(TokenType.ACOS, "cos⁻¹"))
                    i += 5
                }
                s.startsWith("acos", i) -> {
                    if (shouldInsertImplicitMultiply(tokens)) tokens.add(Token(TokenType.MULTIPLY, "*"))
                    tokens.add(Token(TokenType.ACOS, "acos"))
                    i += 4
                }
                s.startsWith("tan⁻¹", i) -> {
                    if (shouldInsertImplicitMultiply(tokens)) tokens.add(Token(TokenType.MULTIPLY, "*"))
                    tokens.add(Token(TokenType.ATAN, "tan⁻¹"))
                    i += 5
                }
                s.startsWith("atan", i) -> {
                    if (shouldInsertImplicitMultiply(tokens)) tokens.add(Token(TokenType.MULTIPLY, "*"))
                    tokens.add(Token(TokenType.ATAN, "atan"))
                    i += 4
                }
                s.startsWith("sin", i) -> {
                    if (shouldInsertImplicitMultiply(tokens)) tokens.add(Token(TokenType.MULTIPLY, "*"))
                    tokens.add(Token(TokenType.SIN, "sin"))
                    i += 3
                }
                s.startsWith("cos", i) -> {
                    if (shouldInsertImplicitMultiply(tokens)) tokens.add(Token(TokenType.MULTIPLY, "*"))
                    tokens.add(Token(TokenType.COS, "cos"))
                    i += 3
                }
                s.startsWith("tan", i) -> {
                    if (shouldInsertImplicitMultiply(tokens)) tokens.add(Token(TokenType.MULTIPLY, "*"))
                    tokens.add(Token(TokenType.TAN, "tan"))
                    i += 3
                }
                s.startsWith("log", i) -> {
                    if (shouldInsertImplicitMultiply(tokens)) tokens.add(Token(TokenType.MULTIPLY, "*"))
                    tokens.add(Token(TokenType.LOG, "log"))
                    i += 3
                }
                s.startsWith("ln", i) -> {
                    if (shouldInsertImplicitMultiply(tokens)) tokens.add(Token(TokenType.MULTIPLY, "*"))
                    tokens.add(Token(TokenType.LN, "ln"))
                    i += 2
                }
                s.startsWith("abs", i) -> {
                    if (shouldInsertImplicitMultiply(tokens)) tokens.add(Token(TokenType.MULTIPLY, "*"))
                    tokens.add(Token(TokenType.ABS, "abs"))
                    i += 3
                }
                c == '+' -> {
                    tokens.add(Token(TokenType.PLUS, "+"))
                    i++
                }
                c == '-' -> {
                    tokens.add(Token(TokenType.MINUS, "-"))
                    i++
                }
                c == '*' -> {
                    tokens.add(Token(TokenType.MULTIPLY, "*"))
                    i++
                }
                c == '/' -> {
                    tokens.add(Token(TokenType.DIVIDE, "/"))
                    i++
                }
                c == '^' -> {
                    tokens.add(Token(TokenType.POWER, "^"))
                    i++
                }
                c == '%' -> {
                    tokens.add(Token(TokenType.PERCENT, "%"))
                    i++
                }
                c == '!' -> {
                    tokens.add(Token(TokenType.FACTORIAL, "!"))
                    i++
                }
                s.startsWith("³√", i) -> {
                    if (shouldInsertImplicitMultiply(tokens)) {
                        tokens.add(Token(TokenType.MULTIPLY, "*"))
                    }
                    tokens.add(Token(TokenType.CBRT, "³√"))
                    i += 2
                }
                s.startsWith("∛", i) -> {
                    if (shouldInsertImplicitMultiply(tokens)) {
                        tokens.add(Token(TokenType.MULTIPLY, "*"))
                    }
                    tokens.add(Token(TokenType.CBRT, "∛"))
                    i += 1
                }
                s.startsWith("cbrt", i) -> {
                    if (shouldInsertImplicitMultiply(tokens)) {
                        tokens.add(Token(TokenType.MULTIPLY, "*"))
                    }
                    tokens.add(Token(TokenType.CBRT, "cbrt"))
                    i += 4
                }
                c == '√' -> {
                    if (shouldInsertImplicitMultiply(tokens)) {
                        tokens.add(Token(TokenType.MULTIPLY, "*"))
                    }
                    tokens.add(Token(TokenType.SQRT, "√"))
                    i++
                }
                c == '(' -> {
                    if (shouldInsertImplicitMultiply(tokens)) {
                        tokens.add(Token(TokenType.MULTIPLY, "*"))
                    }
                    tokens.add(Token(TokenType.LPAREN, "("))
                    i++
                }
                c == ')' -> {
                    tokens.add(Token(TokenType.RPAREN, ")"))
                    i++
                }
                c == 'π' || (c == 'p' && i + 1 < s.length && s[i + 1] == 'i') -> {
                    if (shouldInsertImplicitMultiply(tokens)) {
                        tokens.add(Token(TokenType.MULTIPLY, "*"))
                    }
                    tokens.add(Token(TokenType.CONSTANT_PI, "π", PI_VALUE))
                    i += if (c == 'p') 2 else 1
                }
                c == 'e' -> {
                    if (shouldInsertImplicitMultiply(tokens)) {
                        tokens.add(Token(TokenType.MULTIPLY, "*"))
                    }
                    tokens.add(Token(TokenType.CONSTANT_E, "e", E_VALUE))
                    i++
                }
                c.isDigit() || c == '.' -> {
                    if (tokens.isNotEmpty()) {
                        val last = tokens.last()
                        if (last.type == TokenType.NUMBER) {
                            throw IllegalArgumentException("Invalid syntax: missing operator between numbers")
                        }
                    }
                    if (shouldInsertImplicitMultiply(tokens)) {
                        tokens.add(Token(TokenType.MULTIPLY, "*"))
                    }
                    val start = i
                    var hasDot = false
                    while (i < s.length && (s[i].isDigit() || s[i] == '.')) {
                        if (s[i] == '.') {
                            if (hasDot) throw IllegalArgumentException("Multiple decimal points in number")
                            hasDot = true
                        }
                        i++
                    }
                    val numStr = s.substring(start, i)
                    val value = numStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: $numStr")
                    tokens.add(Token(TokenType.NUMBER, numStr, value))
                }
                else -> {
                    throw IllegalArgumentException("Unknown character: '$c'")
                }
            }
        }
        return tokens
    }

    private fun shouldInsertImplicitMultiply(tokens: List<Token>): Boolean {
        if (tokens.isEmpty()) return false
        val last = tokens.last()
        return when (last.type) {
            TokenType.NUMBER,
            TokenType.RPAREN,
            TokenType.CONSTANT_PI,
            TokenType.CONSTANT_E,
            TokenType.FACTORIAL,
            TokenType.PERCENT -> true
            else -> false
        }
    }

    private data class ParsedValue(val value: Double, val isPercentage: Boolean = false)

    private class Parser(private val tokens: List<Token>, private val isDegreeMode: Boolean = true) {
        private var pos = 0

        fun hasNext(): Boolean = pos < tokens.size

        fun peek(): Token = tokens[pos]

        private fun consume(): Token = tokens[pos++]

        private fun match(vararg types: TokenType): Token? {
            if (hasNext() && types.contains(peek().type)) {
                return consume()
            }
            return null
        }

        fun parseExpression(): Double {
            return parseAdditive().value
        }

        // Additive -> Multiplicative ( ('+' | '-') Multiplicative )*
        private fun parseAdditive(): ParsedValue {
            var left = parseMultiplicative()

            while (hasNext()) {
                val op = match(TokenType.PLUS, TokenType.MINUS) ?: break
                val right = parseMultiplicative()

                val newLeft = if (op.type == TokenType.PLUS) {
                    if (right.isPercentage) {
                        left.value + (left.value * right.value)
                    } else {
                        left.value + right.value
                    }
                } else {
                    if (right.isPercentage) {
                        left.value - (left.value * right.value)
                    } else {
                        left.value - right.value
                    }
                }
                left = ParsedValue(newLeft, false)
            }
            return left
        }

        // Multiplicative -> Unary ( ('*' | '/') Unary )*
        private fun parseMultiplicative(): ParsedValue {
            var left = parseUnary()

            while (hasNext()) {
                val op = match(TokenType.MULTIPLY, TokenType.DIVIDE) ?: break
                val right = parseUnary()
                val newVal = if (op.type == TokenType.MULTIPLY) {
                    left.value * right.value
                } else {
                    if (right.value == 0.0) {
                        throw ArithmeticException("Cannot divide by zero")
                    }
                    left.value / right.value
                }
                left = ParsedValue(newVal, false)
            }
            return left
        }

        // Unary -> ('+' | '-') Unary | Power
        private fun parseUnary(): ParsedValue {
            if (match(TokenType.PLUS) != null) {
                return parseUnary()
            }
            if (match(TokenType.MINUS) != null) {
                val operand = parseUnary()
                return ParsedValue(-operand.value, operand.isPercentage)
            }
            return parsePower()
        }

        // Power -> Postfix ( '^' Unary )?
        private fun parsePower(): ParsedValue {
            val base = parsePostfix()

            if (match(TokenType.POWER) != null) {
                val exponent = parseUnary()
                if (base.value < 0 && exponent.value != exponent.value.toLong().toDouble()) {
                    throw ArithmeticException("Negative base with non-integer power")
                }
                val res = base.value.pow(exponent.value)
                if (res.isNaN()) {
                    throw ArithmeticException("Invalid power calculation")
                }
                return ParsedValue(res, false)
            }
            return base
        }

        // Postfix -> Primary ( '!' | '%' )*
        private fun parsePostfix(): ParsedValue {
            var value = parsePrimary()
            var isPercent = false

            while (hasNext()) {
                when {
                    match(TokenType.FACTORIAL) != null -> {
                        value = computeFactorial(value)
                        isPercent = false
                    }
                    match(TokenType.PERCENT) != null -> {
                        value /= 100.0
                        isPercent = true
                    }
                    else -> break
                }
            }
            return ParsedValue(value, isPercent)
        }

        // Primary -> NUMBER | CONSTANT | '√' Unary | '(' Expression ')'
        private fun parsePrimary(): Double {
            if (!hasNext()) {
                throw IllegalArgumentException("Incomplete expression")
            }

            val token = peek()
            return when (token.type) {
                TokenType.NUMBER -> {
                    consume()
                    token.value
                }
                TokenType.CONSTANT_PI, TokenType.CONSTANT_E -> {
                    consume()
                    token.value
                }
                TokenType.SQRT -> {
                    consume()
                    val operand = parseUnary()
                    if (operand.value < 0) {
                        throw ArithmeticException("Square root of negative number")
                    }
                    sqrt(operand.value)
                }
                TokenType.CBRT -> {
                    consume()
                    val operand = parseUnary()
                    Math.cbrt(operand.value)
                }
                TokenType.SIN -> {
                    consume()
                    val operand = parseUnary()
                    val angle = if (isDegreeMode) Math.toRadians(operand.value) else operand.value
                    val res = sin(angle)
                    if (isDegreeMode && abs(res) < 1e-15) 0.0 else res
                }
                TokenType.COS -> {
                    consume()
                    val operand = parseUnary()
                    val angle = if (isDegreeMode) Math.toRadians(operand.value) else operand.value
                    val res = cos(angle)
                    if (isDegreeMode && abs(res) < 1e-15) 0.0 else res
                }
                TokenType.TAN -> {
                    consume()
                    val operand = parseUnary()
                    if (isDegreeMode) {
                        val deg = ((operand.value % 360.0) + 360.0) % 360.0
                        if (abs(deg - 90.0) < 1e-9 || abs(deg - 270.0) < 1e-9) {
                            throw ArithmeticException("Tangent undefined")
                        }
                    }
                    val angle = if (isDegreeMode) Math.toRadians(operand.value) else operand.value
                    val res = tan(angle)
                    if (abs(res) < 1e-15) 0.0 else res
                }
                TokenType.ASIN -> {
                    consume()
                    val operand = parseUnary()
                    if (operand.value < -1.0 || operand.value > 1.0) {
                        throw ArithmeticException("Domain error: asin argument must be in [-1, 1]")
                    }
                    val rad = asin(operand.value)
                    if (isDegreeMode) Math.toDegrees(rad) else rad
                }
                TokenType.ACOS -> {
                    consume()
                    val operand = parseUnary()
                    if (operand.value < -1.0 || operand.value > 1.0) {
                        throw ArithmeticException("Domain error: acos argument must be in [-1, 1]")
                    }
                    val rad = acos(operand.value)
                    if (isDegreeMode) Math.toDegrees(rad) else rad
                }
                TokenType.ATAN -> {
                    consume()
                    val operand = parseUnary()
                    val rad = atan(operand.value)
                    if (isDegreeMode) Math.toDegrees(rad) else rad
                }
                TokenType.SINH -> {
                    consume()
                    val operand = parseUnary()
                    sinh(operand.value)
                }
                TokenType.COSH -> {
                    consume()
                    val operand = parseUnary()
                    cosh(operand.value)
                }
                TokenType.TANH -> {
                    consume()
                    val operand = parseUnary()
                    tanh(operand.value)
                }
                TokenType.ASINH -> {
                    consume()
                    val operand = parseUnary()
                    val x = operand.value
                    ln(x + sqrt(x * x + 1.0))
                }
                TokenType.ACOSH -> {
                    consume()
                    val operand = parseUnary()
                    val x = operand.value
                    if (x < 1.0) {
                        throw ArithmeticException("Domain error: acosh argument must be >= 1")
                    }
                    ln(x + sqrt(x * x - 1.0))
                }
                TokenType.ATANH -> {
                    consume()
                    val operand = parseUnary()
                    val x = operand.value
                    if (x <= -1.0 || x >= 1.0) {
                        throw ArithmeticException("Domain error: atanh argument must be in (-1, 1)")
                    }
                    0.5 * ln((1.0 + x) / (1.0 - x))
                }
                TokenType.LOG -> {
                    consume()
                    val operand = parseUnary()
                    if (operand.value <= 0) {
                        throw ArithmeticException("Domain error: log argument must be positive")
                    }
                    log10(operand.value)
                }
                TokenType.LN -> {
                    consume()
                    val operand = parseUnary()
                    if (operand.value <= 0) {
                        throw ArithmeticException("Domain error: ln argument must be positive")
                    }
                    ln(operand.value)
                }
                TokenType.ABS -> {
                    consume()
                    val operand = parseUnary()
                    abs(operand.value)
                }
                TokenType.LPAREN -> {
                    consume()
                    val expr = parseExpression()
                    if (match(TokenType.RPAREN) == null) {
                        throw IllegalArgumentException("Missing closing parenthesis ')'")
                    }
                    expr
                }
                else -> {
                    throw IllegalArgumentException("Unexpected token: ${token.text}")
                }
            }
        }

        private fun computeFactorial(n: Double): Double {
            if (n < 0) {
                throw ArithmeticException("Factorial of negative number")
            }
            val rounded = n.roundToLong()
            if (abs(n - rounded) < 1e-12) {
                if (rounded > 170) {
                    throw ArithmeticException("Factorial overflow (max 170!)")
                }
                var fact = 1.0
                for (i in 2..rounded) {
                    fact *= i
                }
                return fact
            } else {
                return gamma(n + 1.0)
            }
        }

        private fun gamma(z: Double): Double {
            if (z < 0.5) {
                return Math.PI / (sin(Math.PI * z) * gamma(1.0 - z))
            }
            val zAdj = z - 1.0
            val p = doubleArrayOf(
                676.5203681218851,
                -1259.1392167224028,
                771.32342877765313,
                -176.61502916214059,
                12.507343278686905,
                -0.138571095836524,
                9.9843695780195716e-6,
                1.5056327351493116e-7
            )
            var x = 0.99999999999980993
            for (i in p.indices) {
                x += p[i] / (zAdj + i + 1)
            }
            val t = zAdj + p.size - 0.5
            return sqrt(2 * Math.PI) * t.pow(zAdj + 0.5) * exp(-t) * x
        }
    }

    fun formatResult(value: Double): String {
        if (value.isNaN()) return "Error"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"

        val rounded = value.roundToLong()
        if (abs(value - rounded.toDouble()) < 1e-11 && abs(value) < 1e14) {
            return rounded.toString()
        }

        val bd = BigDecimal(value.toString())
        val roundedBd = bd.setScale(10, RoundingMode.HALF_UP).stripTrailingZeros()
        val plain = roundedBd.toPlainString()

        if (abs(value) >= 1e12 || (abs(value) > 0 && abs(value) < 1e-6)) {
            val expFormat = String.format(Locale.US, "%.6e", value)
            return expFormat.replace("e+", " × 10^").replace("e", " × 10^")
        }

        return plain
    }
}
