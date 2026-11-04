package com.calculator.app

import com.calculator.app.domain.EvalResult
import com.calculator.app.domain.MathEvaluator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MathEvaluatorTest {

    private lateinit var evaluator: MathEvaluator

    @Before
    fun setUp() {
        evaluator = MathEvaluator()
    }

    private fun assertEvaluatesTo(expression: String, expected: Double, delta: Double = 1e-5, isDegreeMode: Boolean = true) {
        val result = evaluator.evaluate(expression, isDegreeMode)
        assertTrue("Expected Success for '$expression' but got: $result", result is EvalResult.Success)
        val success = result as EvalResult.Success
        assertEquals("Expression '$expression' evaluated to ${success.value}, expected $expected", expected, success.value, delta)
    }

    private fun assertEvaluationError(expression: String, isDegreeMode: Boolean = true) {
        val result = evaluator.evaluate(expression, isDegreeMode)
        assertTrue("Expected Error for '$expression' but got: $result", result is EvalResult.Error)
    }

    @Test
    fun testBasicArithmetic() {
        assertEvaluatesTo("1 + 2", 3.0)
        assertEvaluatesTo("10 - 4", 6.0)
        assertEvaluatesTo("6 * 7", 42.0)
        assertEvaluatesTo("6 × 7", 42.0)
        assertEvaluatesTo("20 / 4", 5.0)
        assertEvaluatesTo("20 ÷ 4", 5.0)
    }

    @Test
    fun testOperatorPrecedence() {
        assertEvaluatesTo("2 + 3 * 4", 14.0)
        assertEvaluatesTo("(2 + 3) * 4", 20.0)
        assertEvaluatesTo("20 - 4 / 2", 18.0)
        assertEvaluatesTo("100 - 20 * 3 + 10 / 2", 45.0)
    }

    @Test
    fun testPowerCalculations() {
        assertEvaluatesTo("2 ^ 3", 8.0)
        assertEvaluatesTo("5 ^ 0", 1.0)
        assertEvaluatesTo("4 ^ 0.5", 2.0)
        assertEvaluatesTo("2 ^ 3 ^ 2", 512.0)
        assertEvaluatesTo("2 ^ -1", 0.5)
        assertEvaluatesTo("(2 + 3) ^ 2", 25.0)
    }

    @Test
    fun testFactorial() {
        assertEvaluatesTo("0!", 1.0)
        assertEvaluatesTo("1!", 1.0)
        assertEvaluatesTo("5!", 120.0)
        assertEvaluatesTo("6!", 720.0)
        assertEvaluatesTo("(3 + 2)!", 120.0)
        assertEvaluatesTo("3! + 4!", 30.0)
        assertEvaluatesTo("2 * 4!", 48.0)
    }

    @Test
    fun testPercentage() {
        assertEvaluatesTo("50%", 0.5)
        assertEvaluatesTo("200 * 10%", 20.0)
        assertEvaluatesTo("100 + 10%", 110.0)
        assertEvaluatesTo("100 - 20%", 80.0)
        assertEvaluatesTo("50 / 10%", 500.0)
        assertEvaluatesTo("(50 + 50)%", 1.0)
    }

    @Test
    fun testConstantsPiAndE() {
        assertEvaluatesTo("π", Math.PI)
        assertEvaluatesTo("e", Math.E)
        assertEvaluatesTo("2π", 2 * Math.PI)
        assertEvaluatesTo("2 * π", 2 * Math.PI)
        assertEvaluatesTo("π * 2", 2 * Math.PI)
        assertEvaluatesTo("π(2)", 2 * Math.PI)
        assertEvaluatesTo("e ^ 2", Math.E * Math.E)
    }

    @Test
    fun testParenthesesAndImplicitMultiplication() {
        assertEvaluatesTo("2(3 + 4)", 14.0)
        assertEvaluatesTo("(2 + 1)(3 + 1)", 12.0)
        assertEvaluatesTo("((2 + 3) * (4 + 1))", 25.0)
        assertEvaluatesTo("3(2)^2", 12.0)
    }

    @Test
    fun testSquareRoot() {
        assertEvaluatesTo("√9", 3.0)
        assertEvaluatesTo("2√16", 8.0)
        assertEvaluatesTo("√(16 + 9)", 5.0)
        assertEvaluatesTo("√2 ^ 2", 2.0)
    }

    @Test
    fun testNegativeNumbersAndUnaryMinus() {
        assertEvaluatesTo("-5 + 10", 5.0)
        assertEvaluatesTo("10 + -5", 5.0)
        assertEvaluatesTo("5 * -3", -15.0)
        assertEvaluatesTo("6 * -3", -18.0)
        assertEvaluatesTo("6 × -3", -18.0)
        assertEvaluatesTo("6 / -3", -2.0)
        assertEvaluatesTo("6 ÷ -2", -3.0)
        assertEvaluatesTo("2 ^ -3", 0.125)
        assertEvaluatesTo("-6 * -3", 18.0)
        assertEvaluatesTo("(-2) ^ 2", 4.0)
        assertEvaluatesTo("-2 ^ 2", -4.0)
    }

    @Test
    fun testScientificTrigonometryAndLogarithms() {
        // Degree mode trigonometry
        assertEvaluatesTo("sin(30)", 0.5, isDegreeMode = true)
        assertEvaluatesTo("sin(90)", 1.0, isDegreeMode = true)
        assertEvaluatesTo("sin(180)", 0.0, isDegreeMode = true)
        assertEvaluatesTo("cos(60)", 0.5, isDegreeMode = true)
        assertEvaluatesTo("cos(0)", 1.0, isDegreeMode = true)
        assertEvaluatesTo("cos(90)", 0.0, isDegreeMode = true)
        assertEvaluatesTo("tan(45)", 1.0, isDegreeMode = true)

        // Radians mode trigonometry
        assertEvaluatesTo("sin(π / 2)", 1.0, isDegreeMode = false)
        assertEvaluatesTo("cos(π)", -1.0, isDegreeMode = false)

        // Inverse trigonometry
        assertEvaluatesTo("asin(0.5)", 30.0, isDegreeMode = true)
        assertEvaluatesTo("sin⁻¹(0.5)", 30.0, isDegreeMode = true)
        assertEvaluatesTo("acos(0.5)", 60.0, isDegreeMode = true)
        assertEvaluatesTo("cos⁻¹(0.5)", 60.0, isDegreeMode = true)
        assertEvaluatesTo("atan(1)", 45.0, isDegreeMode = true)
        assertEvaluatesTo("tan⁻¹(1)", 45.0, isDegreeMode = true)

        // Logarithms and exponentials
        assertEvaluatesTo("log(100)", 2.0)
        assertEvaluatesTo("log(1000)", 3.0)
        assertEvaluatesTo("ln(e)", 1.0)
        assertEvaluatesTo("ln(e ^ 2)", 2.0)

        // Absolute value
        assertEvaluatesTo("abs(-25)", 25.0)
        assertEvaluatesTo("abs(14)", 14.0)

        // Implicit multiplication with functions
        assertEvaluatesTo("2sin(30)", 1.0, isDegreeMode = true)
        assertEvaluatesTo("4log(100)", 8.0)
        assertEvaluatesTo("3abs(-5)", 15.0)
    }

    @Test
    fun testHyperbolicFunctions() {
        assertEvaluatesTo("sinh(0)", 0.0)
        assertEvaluatesTo("cosh(0)", 1.0)
        assertEvaluatesTo("tanh(0)", 0.0)
        assertEvaluatesTo("asinh(0)", 0.0)
        assertEvaluatesTo("sinh⁻¹(0)", 0.0)
        assertEvaluatesTo("acosh(1)", 0.0)
        assertEvaluatesTo("cosh⁻¹(1)", 0.0)
        assertEvaluatesTo("atanh(0)", 0.0)
        assertEvaluatesTo("tanh⁻¹(0)", 0.0)
        assertEvaluatesTo("2sinh(0)", 0.0)
        assertEvaluatesTo("3cosh(0)", 3.0)
    }

    @Test
    fun testCubeRoot() {
        assertEvaluatesTo("³√8", 2.0)
        assertEvaluatesTo("³√27", 3.0)
        assertEvaluatesTo("³√(-8)", -2.0)
        assertEvaluatesTo("³√(125)", 5.0)
        assertEvaluatesTo("2³√8", 4.0)
        assertEvaluatesTo("cbrt(64)", 4.0)
        assertEvaluatesTo("∛27", 3.0)
    }

    @Test
    fun testTrailingArithmeticOperators() {
        // User examples: 6+3- = 9, 6+3+ = 9, 5+9+7+2+ = 23, 5*6* = 30, 10/5* = 2
        assertEvaluatesTo("6+3+", 9.0)
        assertEvaluatesTo("6+3-", 9.0)
        assertEvaluatesTo("6 + 3 +", 9.0)
        assertEvaluatesTo("6 + 3 -", 9.0)
        assertEvaluatesTo("6 + 3 ×", 9.0)
        assertEvaluatesTo("6 + 3 ÷", 9.0)
        assertEvaluatesTo("5+9+7+2+", 23.0)
        assertEvaluatesTo("5 + 9 + 7 + 2 +", 23.0)
        assertEvaluatesTo("5*6*", 30.0)
        assertEvaluatesTo("5 * 6 *", 30.0)
        assertEvaluatesTo("5 × 6 ×", 30.0)
        assertEvaluatesTo("10/5*", 2.0)
        assertEvaluatesTo("10 / 5 *", 2.0)
        assertEvaluatesTo("10 ÷ 5 ÷", 2.0)
        assertEvaluatesTo("10 ÷ 5 ×", 2.0)
        assertEvaluatesTo("6 * -3 +", -18.0)
        assertEvaluatesTo("6 × -3 -", -18.0)
        assertEvaluatesTo("(2 + 3) * 4 +", 20.0)
    }

    @Test
    fun testErrorHandling() {
        assertEvaluationError("10 / 0")
        assertEvaluationError("5 ÷ 0")
        assertEvaluationError("(-5)!")
        assertEvaluationError("√(-4)")
        assertEvaluationError("((2 + 3)")
        assertEvaluationError("")
        assertEvaluationError("   ")

        // Incomplete single operands with operator: 5*=, 5+=, 5/=, 5-=
        assertEvaluationError("5*")
        assertEvaluationError("5+")
        assertEvaluationError("5/")
        assertEvaluationError("5-")
        assertEvaluationError("5 *")
        assertEvaluationError("5 +")
        assertEvaluationError("5 /")
        assertEvaluationError("5 -")
        assertEvaluationError("5 ×")
        assertEvaluationError("5 ÷")
        assertEvaluationError("6 * -")
        assertEvaluationError("6 × -")
        assertEvaluationError("6 / -")
        assertEvaluationError("6 + +")
        assertEvaluationError("^")
        assertEvaluationError("%")
        assertEvaluationError("!")
        assertEvaluationError("5^")
        assertEvaluationError("5 ^")
        assertEvaluationError("+")
        assertEvaluationError("-")
        assertEvaluationError("*")
        assertEvaluationError("/")
        // Two numbers separated only by whitespace without an operator are invalid syntax
        assertEvaluationError("5 9")
        assertEvaluationError("5 9×")
        assertEvaluationError("5 9 × 2")
    }

    @Test
    fun testValidEquationsWithOperatorSpacing() {
        assertEvaluatesTo("59 × 2", 118.0)
        assertEvaluatesTo("5 × 9", 45.0)
    }

    @Test
    fun testFormatting() {
        assertEquals("5", evaluator.formatResult(5.0))
        assertEquals("5.5", evaluator.formatResult(5.5))
        assertEquals("0", evaluator.formatResult(0.0))
        assertEquals("120", evaluator.formatResult(120.0))
    }
}
