package com.calculator.app

import com.calculator.app.data.local.CalculationRecord
import com.calculator.app.data.preferences.AppThemeMode
import com.calculator.app.data.preferences.NumberingSystem
import com.calculator.app.data.repository.ICalculatorRepository
import com.calculator.app.ui.CalculatorAction
import com.calculator.app.ui.CalculatorViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FakeCalculatorRepository : ICalculatorRepository {
    private val _history = MutableStateFlow<List<CalculationRecord>>(emptyList())
    override val history: Flow<List<CalculationRecord>> = _history.asStateFlow()

    private val _themeMode = MutableStateFlow(AppThemeMode.DARK)
    override val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _numberingSystem = MutableStateFlow(NumberingSystem.INTERNATIONAL)
    override val numberingSystem: StateFlow<NumberingSystem> = _numberingSystem.asStateFlow()

    val savedCalculations = mutableListOf<CalculationRecord>()

    override suspend fun saveCalculation(expression: String, result: String) {
        val record = CalculationRecord(expression = expression, result = result, timestamp = 0L)
        savedCalculations.add(record)
        _history.value = listOf(record) + _history.value
    }

    override suspend fun deleteCalculation(record: CalculationRecord) {
        savedCalculations.remove(record)
        _history.value = _history.value.filter { it.id != record.id }
    }

    override suspend fun clearHistory() {
        savedCalculations.clear()
        _history.value = emptyList()
    }

    override fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
    }

    override fun setNumberingSystem(system: NumberingSystem) {
        _numberingSystem.value = system
    }
}

class CalculatorViewModelTest {

    private lateinit var repository: FakeCalculatorRepository
    private lateinit var viewModel: CalculatorViewModel

    @Before
    fun setUp() {
        repository = FakeCalculatorRepository()
        viewModel = CalculatorViewModel(repository, CoroutineScope(Dispatchers.Unconfined))
    }

    @Test
    fun testInitialState() {
        val state = viewModel.state.value
        assertEquals("", state.expression)
        assertEquals(0, state.cursorIndex)
        assertEquals("", state.finalResult)
        assertFalse(state.isResultCalculated)
    }

    @Test
    fun testAppendDigitsMovesCursorToEnd() {
        viewModel.onAction(CalculatorAction.Number("1"))
        viewModel.onAction(CalculatorAction.Number("2"))
        viewModel.onAction(CalculatorAction.Number("3"))
        viewModel.onAction(CalculatorAction.Number("4"))
        viewModel.onAction(CalculatorAction.Number("5"))

        val state = viewModel.state.value
        assertEquals("12345", state.expression)
        assertEquals(5, state.cursorIndex)
    }

    @Test
    fun testMoveCursorAndInsertDigitInMiddle() {
        viewModel.onAction(CalculatorAction.Number("1"))
        viewModel.onAction(CalculatorAction.Number("2"))
        viewModel.onAction(CalculatorAction.Number("3"))
        viewModel.onAction(CalculatorAction.Number("4"))
        viewModel.onAction(CalculatorAction.Number("5"))

        // Move cursor between '2' and '3' (index 2)
        viewModel.onAction(CalculatorAction.MoveCursor(2))
        assertEquals(2, viewModel.state.value.cursorIndex)

        // Insert '9' at cursor
        viewModel.onAction(CalculatorAction.Number("9"))
        assertEquals("129345", viewModel.state.value.expression)
        assertEquals(3, viewModel.state.value.cursorIndex)
    }

    @Test
    fun testBackspaceInMiddleDeletesCharBeforeCursor() {
        viewModel.onAction(CalculatorAction.Number("1"))
        viewModel.onAction(CalculatorAction.Number("2"))
        viewModel.onAction(CalculatorAction.Number("3"))
        viewModel.onAction(CalculatorAction.Number("4"))
        viewModel.onAction(CalculatorAction.Number("5"))

        // Move cursor to index 3 (after '3', before '4')
        viewModel.onAction(CalculatorAction.MoveCursor(3))

        // Backspace should delete '3'
        viewModel.onAction(CalculatorAction.Backspace)
        assertEquals("1245", viewModel.state.value.expression)
        assertEquals(2, viewModel.state.value.cursorIndex)
    }

    @Test
    fun testBackspaceAtStartDoesNothing() {
        viewModel.onAction(CalculatorAction.Number("1"))
        viewModel.onAction(CalculatorAction.Number("2"))
        viewModel.onAction(CalculatorAction.Number("3"))

        // Move cursor to index 0 (before '1')
        viewModel.onAction(CalculatorAction.MoveCursor(0))

        // Backspace should do nothing
        viewModel.onAction(CalculatorAction.Backspace)
        assertEquals("123", viewModel.state.value.expression)
        assertEquals(0, viewModel.state.value.cursorIndex)
    }

    @Test
    fun testBackspaceMultiCharFunctionToken() {
        viewModel.onAction(CalculatorAction.Number("5"))
        viewModel.onAction(CalculatorAction.Operator("+"))
        viewModel.onAction(CalculatorAction.ScientificFunction("sin("))
        viewModel.onAction(CalculatorAction.Number("4"))
        viewModel.onAction(CalculatorAction.Number("5"))

        assertEquals("5 + sin(45", viewModel.state.value.expression)
        assertEquals(10, viewModel.state.value.cursorIndex)

        // Move cursor to index 8 (immediately after "sin(")
        viewModel.onAction(CalculatorAction.MoveCursor(8))

        // Backspace should delete the entire "sin(" function token
        viewModel.onAction(CalculatorAction.Backspace)
        assertEquals("5 + 45", viewModel.state.value.expression)
        assertEquals(4, viewModel.state.value.cursorIndex)
    }

    @Test
    fun testBackspaceOperatorWithSpaces() {
        viewModel.onAction(CalculatorAction.Number("1"))
        viewModel.onAction(CalculatorAction.Number("2"))
        viewModel.onAction(CalculatorAction.Operator("+"))
        viewModel.onAction(CalculatorAction.Number("3"))
        viewModel.onAction(CalculatorAction.Number("4"))

        assertEquals("12 + 34", viewModel.state.value.expression)

        // Move cursor to index 5 (immediately after " + ")
        viewModel.onAction(CalculatorAction.MoveCursor(5))

        // Backspace should delete the operator and its surrounding spaces
        viewModel.onAction(CalculatorAction.Backspace)
        assertEquals("1234", viewModel.state.value.expression)
        assertEquals(2, viewModel.state.value.cursorIndex)
    }

    @Test
    fun testBackspacePowerToken() {
        viewModel.onAction(CalculatorAction.Number("6"))
        viewModel.onAction(CalculatorAction.Square)

        assertEquals("6^2", viewModel.state.value.expression)
        assertEquals(3, viewModel.state.value.cursorIndex)

        // Backspace should delete "^2"
        viewModel.onAction(CalculatorAction.Backspace)
        assertEquals("6", viewModel.state.value.expression)
        assertEquals(1, viewModel.state.value.cursorIndex)
    }

    @Test
    fun testDecimalAtCursorWithoutDot() {
        viewModel.onAction(CalculatorAction.Number("1"))
        viewModel.onAction(CalculatorAction.Number("2"))
        viewModel.onAction(CalculatorAction.Operator("+"))
        viewModel.onAction(CalculatorAction.Number("3"))
        viewModel.onAction(CalculatorAction.Number("4"))

        // Move cursor to index 1 (between '1' and '2')
        viewModel.onAction(CalculatorAction.MoveCursor(1))

        // Tap Decimal
        viewModel.onAction(CalculatorAction.Decimal)
        assertEquals("1.2 + 34", viewModel.state.value.expression)
        assertEquals(2, viewModel.state.value.cursorIndex)
    }

    @Test
    fun testDecimalAtCursorWithExistingDotIsRejected() {
        viewModel.onAction(CalculatorAction.Number("1"))
        viewModel.onAction(CalculatorAction.Number("2"))
        viewModel.onAction(CalculatorAction.Decimal)
        viewModel.onAction(CalculatorAction.Number("3"))

        assertEquals("12.3", viewModel.state.value.expression)

        // Move cursor to index 1 (between '1' and '2')
        viewModel.onAction(CalculatorAction.MoveCursor(1))

        // Tap Decimal again in the same number segment
        viewModel.onAction(CalculatorAction.Decimal)
        // Should be rejected
        assertEquals("12.3", viewModel.state.value.expression)
        assertEquals(1, viewModel.state.value.cursorIndex)
    }

    @Test
    fun testCalculateAndTapToReactivateEditing() {
        viewModel.onAction(CalculatorAction.Number("6"))
        viewModel.onAction(CalculatorAction.Operator("+"))
        viewModel.onAction(CalculatorAction.Number("3"))
        viewModel.onAction(CalculatorAction.Calculate)

        assertTrue(viewModel.state.value.isResultCalculated)
        assertEquals("9", viewModel.state.value.finalResult)

        // Tapping on expression at index 1 to correct mistake
        viewModel.onAction(CalculatorAction.MoveCursor(1))
        assertFalse(viewModel.state.value.isResultCalculated)
        assertEquals(1, viewModel.state.value.cursorIndex)

        // Inserting '0' at index 1
        viewModel.onAction(CalculatorAction.Number("0"))
        assertEquals("60 + 3", viewModel.state.value.expression)
        assertEquals(2, viewModel.state.value.cursorIndex)
    }

    @Test
    fun testCubeRootAndHyperbolicTokens() {
        viewModel.onAction(CalculatorAction.CubeRoot)
        viewModel.onAction(CalculatorAction.Number("8"))
        assertEquals("³√8", viewModel.state.value.expression)

        viewModel.onAction(CalculatorAction.Calculate)
        assertEquals("2", viewModel.state.value.finalResult)

        // Test backspacing multi-char token sinh(
        viewModel.onAction(CalculatorAction.ClearAll)
        viewModel.onAction(CalculatorAction.ScientificFunction("sinh("))
        assertEquals("sinh(", viewModel.state.value.expression)
        viewModel.onAction(CalculatorAction.Backspace)
        assertEquals("", viewModel.state.value.expression)
    }

    @Test
    fun testClearAllResetsCursor() {
        viewModel.onAction(CalculatorAction.Number("1"))
        viewModel.onAction(CalculatorAction.Number("2"))
        viewModel.onAction(CalculatorAction.Number("3"))
        assertEquals(3, viewModel.state.value.cursorIndex)

        viewModel.onAction(CalculatorAction.ClearAll)
        assertEquals("", viewModel.state.value.expression)
        assertEquals(0, viewModel.state.value.cursorIndex)
    }

    @Test
    fun testSanitizeCursorPositionPreventsLandingInOperatorGap() {
        val expr = "5 × "
        // Index 2 is between space and × -> snaps to 1 (after 5)
        assertEquals(1, CalculatorViewModel.sanitizeCursorIndex(expr, 2))
        // Index 3 is between × and trailing space -> snaps to 4 (after space)
        assertEquals(4, CalculatorViewModel.sanitizeCursorIndex(expr, 3))

        val exprWithOperand = "5 × 3"
        // Index 2 snaps to 1
        assertEquals(1, CalculatorViewModel.sanitizeCursorIndex(exprWithOperand, 2))
        // Index 3 snaps to 4 (before 3)
        assertEquals(4, CalculatorViewModel.sanitizeCursorIndex(exprWithOperand, 3))
    }

    @Test
    fun testCursorDraggedBeforeOperatorDoesNotCreateBrokenEquation() {
        // User inputs 5, then ×
        viewModel.onAction(CalculatorAction.Number("5"))
        viewModel.onAction(CalculatorAction.Operator("×"))
        assertEquals("5 × ", viewModel.state.value.expression)

        // User drags cursor before operator (e.g. at index 2 or 1)
        viewModel.onAction(CalculatorAction.MoveCursor(2))
        // Verify cursor snapped out of the gap to right after '5'
        assertEquals(1, viewModel.state.value.cursorIndex)

        // User types 9: it MUST attach to the left operand (producing "59 × ", NEVER "5 9× ")
        viewModel.onAction(CalculatorAction.Number("9"))
        assertEquals("59 × ", viewModel.state.value.expression)
        assertEquals(2, viewModel.state.value.cursorIndex)

        // Move to the end and enter 2
        viewModel.onAction(CalculatorAction.MoveCursor(viewModel.state.value.expression.length))
        viewModel.onAction(CalculatorAction.Number("2"))
        assertEquals("59 × 2", viewModel.state.value.expression)

        // Calculate
        viewModel.onAction(CalculatorAction.Calculate)
        assertEquals("118", viewModel.state.value.finalResult)
    }

    @Test
    fun testCursorDraggedAfterOperatorInputsNextOperand() {
        viewModel.onAction(CalculatorAction.Number("5"))
        viewModel.onAction(CalculatorAction.Operator("×"))
        // Position after operator
        viewModel.onAction(CalculatorAction.MoveCursor(3))
        assertEquals(4, viewModel.state.value.cursorIndex)

        viewModel.onAction(CalculatorAction.Number("9"))
        assertEquals("5 × 9", viewModel.state.value.expression)

        viewModel.onAction(CalculatorAction.Calculate)
        assertEquals("45", viewModel.state.value.finalResult)
    }

    @Test
    fun testCloseBracketRejectedWhenEmptyOrNoMatchingOpenBracket() {
        // When expression is empty, closing bracket is ignored
        viewModel.onAction(CalculatorAction.ParenthesisClose)
        assertEquals("", viewModel.state.value.expression)

        // When expression has no open bracket, closing bracket is ignored
        viewModel.onAction(CalculatorAction.Number("5"))
        viewModel.onAction(CalculatorAction.Operator("+"))
        viewModel.onAction(CalculatorAction.Number("3"))
        assertEquals("5 + 3", viewModel.state.value.expression)

        viewModel.onAction(CalculatorAction.ParenthesisClose)
        assertEquals("5 + 3", viewModel.state.value.expression)
    }

    @Test
    fun testCloseBracketAcceptedWhenMatchingOpenBracketExists() {
        viewModel.onAction(CalculatorAction.ParenthesisOpen)
        viewModel.onAction(CalculatorAction.Number("5"))
        viewModel.onAction(CalculatorAction.Operator("+"))
        viewModel.onAction(CalculatorAction.Number("3"))
        assertEquals("(5 + 3", viewModel.state.value.expression)

        // Close bracket should be accepted
        viewModel.onAction(CalculatorAction.ParenthesisClose)
        assertEquals("(5 + 3)", viewModel.state.value.expression)

        // Extra close bracket when all open brackets are closed should be rejected
        viewModel.onAction(CalculatorAction.ParenthesisClose)
        assertEquals("(5 + 3)", viewModel.state.value.expression)
    }

    @Test
    fun testCloseBracketRejectedImmediatelyAfterOpenBracketOrOperator() {
        viewModel.onAction(CalculatorAction.ParenthesisOpen)
        assertEquals("(", viewModel.state.value.expression)

        // Cannot close bracket immediately after '(' (no empty '()')
        viewModel.onAction(CalculatorAction.ParenthesisClose)
        assertEquals("(", viewModel.state.value.expression)

        // Cannot close bracket immediately after an operator like '+'
        viewModel.onAction(CalculatorAction.Number("5"))
        viewModel.onAction(CalculatorAction.Operator("+"))
        assertEquals("(5 + ", viewModel.state.value.expression)

        viewModel.onAction(CalculatorAction.ParenthesisClose)
        assertEquals("(5 + ", viewModel.state.value.expression)
    }

    @Test
    fun testCalculateAutoBalancesUnclosedBrackets() {
        // User types "(5 + 3" without closing parenthesis
        viewModel.onAction(CalculatorAction.ParenthesisOpen)
        viewModel.onAction(CalculatorAction.Number("5"))
        viewModel.onAction(CalculatorAction.Operator("+"))
        viewModel.onAction(CalculatorAction.Number("3"))
        assertEquals("(5 + 3", viewModel.state.value.expression)

        // Pressing Calculate auto-balances brackets so open and close bracket counts are equal
        viewModel.onAction(CalculatorAction.Calculate)
        assertEquals("(5 + 3)", viewModel.state.value.expression)
        assertEquals("8", viewModel.state.value.finalResult)
        assertNull(viewModel.state.value.errorMessage)
        assertTrue(viewModel.state.value.isResultCalculated)

        // Verify nested unclosed brackets: "((2 + 3) * 4" -> "((2 + 3) * 4)"
        viewModel.onAction(CalculatorAction.ClearAll)
        viewModel.onAction(CalculatorAction.ParenthesisOpen)
        viewModel.onAction(CalculatorAction.ParenthesisOpen)
        viewModel.onAction(CalculatorAction.Number("2"))
        viewModel.onAction(CalculatorAction.Operator("+"))
        viewModel.onAction(CalculatorAction.Number("3"))
        viewModel.onAction(CalculatorAction.ParenthesisClose)
        viewModel.onAction(CalculatorAction.Operator("×"))
        viewModel.onAction(CalculatorAction.Number("4"))
        assertEquals("((2 + 3) × 4", viewModel.state.value.expression)

        viewModel.onAction(CalculatorAction.Calculate)
        assertEquals("((2 + 3) × 4)", viewModel.state.value.expression)
        assertEquals("20", viewModel.state.value.finalResult)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun testLivePreviewAutoBalancesUnclosedBrackets() {
        viewModel.onAction(CalculatorAction.ParenthesisOpen)
        viewModel.onAction(CalculatorAction.Number("9"))
        viewModel.onAction(CalculatorAction.Operator("×"))
        viewModel.onAction(CalculatorAction.Number("5"))

        // Preview should calculate 45 even before closing bracket is pressed
        assertEquals("45", viewModel.state.value.previewResult)
    }
}
