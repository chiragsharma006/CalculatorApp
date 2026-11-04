package com.calculator.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.calculator.app.data.local.CalculationRecord
import com.calculator.app.data.preferences.AppThemeMode
import com.calculator.app.data.repository.CalculatorRepository
import com.calculator.app.data.repository.ICalculatorRepository
import com.calculator.app.domain.EvalResult
import com.calculator.app.domain.MathEvaluator
import com.calculator.app.ui.components.ClipboardHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalculatorViewModel(
    private val repository: ICalculatorRepository,
    private val externalScope: CoroutineScope? = null
) : ViewModel() {

    private val evaluator = MathEvaluator()

    private val scope: CoroutineScope
        get() = externalScope ?: viewModelScope

    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    init {
        // Collect history from database
        scope.launch {
            repository.history.collect { historyList ->
                _state.update { it.copy(historyList = historyList) }
            }
        }

        // Collect theme preference
        scope.launch {
            repository.themeMode.collect { themeMode ->
                _state.update { it.copy(themeMode = themeMode) }
            }
        }

        // Collect numbering system preference
        scope.launch {
            repository.numberingSystem.collect { system ->
                _state.update { it.copy(numberingSystem = system) }
            }
        }
    }

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number -> appendNumber(action.digit)
            is CalculatorAction.Decimal -> appendDecimal()
            is CalculatorAction.Operator -> appendOperator(action.symbol)
            is CalculatorAction.SquareRoot -> appendSquareRoot()
            is CalculatorAction.CubeRoot -> appendCubeRoot()
            is CalculatorAction.Factorial -> appendFactorial()
            is CalculatorAction.Percentage -> appendPercentage()
            is CalculatorAction.ParenthesisOpen -> appendParenthesisOpen()
            is CalculatorAction.ParenthesisClose -> appendParenthesisClose()
            is CalculatorAction.ConstantPi -> appendConstant("π")
            is CalculatorAction.ConstantE -> appendConstant("e")
            is CalculatorAction.Backspace -> handleBackspace()
            is CalculatorAction.MoveCursor -> {
                val current = _state.value.expression
                val sanitized = sanitizeCursorIndex(current, action.newIndex)
                _state.update {
                    it.copy(
                        cursorIndex = sanitized,
                        isResultCalculated = false
                    )
                }
            }
            is CalculatorAction.ClearAll -> handleClearAll()
            is CalculatorAction.Calculate -> handleCalculate()
            is CalculatorAction.Paste -> handlePaste(action.text)
            is CalculatorAction.Cut -> handleCut()
            is CalculatorAction.ToggleScientificMode -> {
                _state.update { it.copy(isScientificMode = !it.isScientificMode) }
            }
            is CalculatorAction.ToggleAngleMode -> {
                val newAngleMode = !_state.value.isDegreeMode
                _state.update { it.copy(isDegreeMode = newAngleMode) }
                updatePreview(_state.value.expression)
            }
            is CalculatorAction.ToggleInvMode -> {
                _state.update { it.copy(isInvMode = !it.isInvMode) }
            }
            is CalculatorAction.ScientificFunction -> {
                appendScientificFunction(action.function)
            }
            is CalculatorAction.Square -> {
                appendSquare()
            }
            is CalculatorAction.PowerCube -> {
                appendPowerCube()
            }
            is CalculatorAction.Reciprocal -> {
                appendReciprocal()
            }
            is CalculatorAction.ToggleHistorySheet -> {
                _state.update { it.copy(isHistoryVisible = action.show) }
            }
            is CalculatorAction.ToggleThemeDialog -> {
                _state.update { it.copy(isThemeDialogVisible = action.show) }
            }
            is CalculatorAction.ToggleAppInfoDialog -> {
                _state.update { it.copy(isAppInfoDialogVisible = action.show) }
            }
            is CalculatorAction.ToggleNumberFormatDialog -> {
                _state.update { it.copy(isNumberFormatDialogVisible = action.show) }
            }
            is CalculatorAction.ToggleTheme -> {
                val nextMode = when (_state.value.themeMode) {
                    AppThemeMode.LIGHT -> AppThemeMode.DARK
                    AppThemeMode.DARK -> AppThemeMode.LIGHT
                    AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
                }
                repository.setThemeMode(nextMode)
            }
            is CalculatorAction.SelectTheme -> {
                repository.setThemeMode(action.mode)
                _state.update { it.copy(isThemeDialogVisible = false) }
            }
            is CalculatorAction.SelectNumberingSystem -> {
                repository.setNumberingSystem(action.system)
                _state.update { it.copy(isNumberFormatDialogVisible = false) }
            }
            is CalculatorAction.HistoryItemClicked -> {
                handleHistoryItemClicked(action.record, action.reuseResult)
            }
            is CalculatorAction.DeleteHistoryItem -> {
                scope.launch {
                    repository.deleteCalculation(action.record)
                }
            }
            is CalculatorAction.ClearAllHistory -> {
                scope.launch {
                    repository.clearHistory()
                }
            }
        }
    }

    private fun insertAtCursor(textToInsert: String, updatePreviewAfter: Boolean = true) {
        if (_state.value.isResultCalculated) {
            _state.update {
                it.copy(
                    expression = textToInsert,
                    cursorIndex = textToInsert.length,
                    finalResult = "",
                    previewResult = "",
                    errorMessage = null,
                    isResultCalculated = false
                )
            }
            if (updatePreviewAfter) updatePreview(textToInsert)
            return
        }

        val current = _state.value.expression
        val cursor = sanitizeCursorIndex(current, _state.value.cursorIndex)
        val before = current.substring(0, cursor)
        val after = current.substring(cursor)

        val updated: String
        val newCursor: Int

        if (before.endsWith(" ") && after.trimStart().isNotEmpty() && isOperatorChar(after.trimStart().first())) {
            val trimmedBefore = before.trimEnd()
            updated = "$trimmedBefore$textToInsert $after"
            newCursor = trimmedBefore.length + textToInsert.length
        } else if (after.startsWith(" ") && before.trimEnd().isNotEmpty() && isOperatorChar(before.trimEnd().last())) {
            val trimmedAfter = after.trimStart()
            updated = "$before $textToInsert$trimmedAfter"
            newCursor = before.length + 1 + textToInsert.length
        } else {
            updated = before + textToInsert + after
            newCursor = cursor + textToInsert.length
        }

        _state.update {
            it.copy(
                expression = updated,
                cursorIndex = newCursor,
                errorMessage = null,
                isResultCalculated = false
            )
        }
        if (updatePreviewAfter) updatePreview(updated)
    }

    private fun appendNumber(digit: String) {
        if (_state.value.isResultCalculated) {
            _state.update {
                it.copy(
                    expression = digit,
                    cursorIndex = digit.length,
                    finalResult = "",
                    previewResult = "",
                    errorMessage = null,
                    isResultCalculated = false
                )
            }
            updatePreview(digit)
            return
        }

        val current = _state.value.expression
        val cursor = sanitizeCursorIndex(current, _state.value.cursorIndex)

        // If expression is "0" and cursor is at end, replace "0" with digit
        if (current == "0" && cursor == 1) {
            _state.update {
                it.copy(
                    expression = digit,
                    cursorIndex = digit.length,
                    errorMessage = null,
                    isResultCalculated = false
                )
            }
            updatePreview(digit)
            return
        }

        val before = current.substring(0, cursor)
        val after = current.substring(cursor)

        val updated: String
        val newCursor: Int

        // Protect against any operator whitespace edge-cases:
        // Case A: User inserts digit before an operator preceded by space (e.g. "5 " and "× ")
        if (before.endsWith(" ") && after.trimStart().isNotEmpty() && isOperatorChar(after.trimStart().first())) {
            val trimmedBefore = before.trimEnd()
            updated = "$trimmedBefore$digit $after"
            newCursor = trimmedBefore.length + digit.length
        }
        // Case B: User inserts digit after an operator before trailing space (e.g. "5 ×" and " ")
        else if (after.startsWith(" ") && before.trimEnd().isNotEmpty() && isOperatorChar(before.trimEnd().last())) {
            val trimmedAfter = after.trimStart()
            updated = "$before $digit$trimmedAfter"
            newCursor = before.length + 1 + digit.length
        }
        // Case C: User inserts digit between digit and space or space and digit (e.g. "5 " and "9") -> collapse stray whitespace between digits
        else if (before.endsWith(" ") && after.isNotEmpty() && after.first().isDigit()) {
            val trimmedBefore = before.trimEnd()
            updated = "$trimmedBefore$digit$after"
            newCursor = trimmedBefore.length + digit.length
        }
        else {
            updated = before + digit + after
            newCursor = cursor + digit.length
        }

        _state.update {
            it.copy(
                expression = updated,
                cursorIndex = newCursor,
                errorMessage = null,
                isResultCalculated = false
            )
        }
        updatePreview(updated)
    }

    private fun appendDecimal() {
        if (_state.value.isResultCalculated) {
            _state.update {
                it.copy(
                    expression = "0.",
                    cursorIndex = 2,
                    finalResult = "",
                    previewResult = "",
                    errorMessage = null,
                    isResultCalculated = false
                )
            }
            return
        }

        val current = _state.value.expression
        if (current.isEmpty()) {
            val updated = "0."
            _state.update {
                it.copy(
                    expression = updated,
                    cursorIndex = 2,
                    errorMessage = null,
                    isResultCalculated = false
                )
            }
            return
        }

        val cursor = sanitizeCursorIndex(current, _state.value.cursorIndex)
        val before = current.substring(0, cursor)
        val after = current.substring(cursor)

        // If before ends with space and after starts with operator:
        // Decimal belongs to the left operand
        if (before.endsWith(" ") && after.trimStart().isNotEmpty() && isOperatorChar(after.trimStart().first())) {
            val trimmedBefore = before.trimEnd()
            val numBefore = trimmedBefore.takeLastWhile { it.isDigit() || it == '.' }
            if (!numBefore.contains('.')) {
                val insertion = if (trimmedBefore.isNotEmpty() && trimmedBefore.last().isDigit()) "." else "0."
                val updated = "$trimmedBefore$insertion $after"
                val newCursor = trimmedBefore.length + insertion.length
                _state.update {
                    it.copy(
                        expression = updated,
                        cursorIndex = newCursor,
                        errorMessage = null,
                        isResultCalculated = false
                    )
                }
                updatePreview(updated)
            }
            return
        }

        // Check if last number segment already contains a dot
        val numBefore = before.takeLastWhile { it.isDigit() || it == '.' }
        val numAfter = after.takeWhile { it.isDigit() || it == '.' }
        val fullNum = numBefore + numAfter

        if (!fullNum.contains('.')) {
            val insertion = if (before.isNotEmpty() && before.last().isDigit()) "." else "0."
            val updated = before + insertion + after
            val newCursor = cursor + insertion.length
            _state.update {
                it.copy(
                    expression = updated,
                    cursorIndex = newCursor,
                    errorMessage = null,
                    isResultCalculated = false
                )
            }
            updatePreview(updated)
        }
    }

    private fun appendOperator(op: String) {
        val current = _state.value.expression

        if (_state.value.isResultCalculated && _state.value.finalResult.isNotEmpty()) {
            // Chain from previous calculation result
            val chained = _state.value.finalResult + " " + op + " "
            _state.update {
                it.copy(
                    expression = chained,
                    cursorIndex = chained.length,
                    finalResult = "",
                    errorMessage = null,
                    isResultCalculated = false
                )
            }
            return
        }

        val isMinus = op == "−" || op == "-"

        if (current.isEmpty()) {
            if (isMinus) {
                _state.update { it.copy(expression = "-", cursorIndex = 1, errorMessage = null) }
            }
            return
        }

        val cursor = _state.value.cursorIndex.coerceIn(0, current.length)
        val before = current.substring(0, cursor)
        val after = current.substring(cursor)

        if (after.isEmpty()) {
            val trimmed = current.trimEnd()

            // Check if trimmed ends with operator + negative sign e.g. "6 × -" or "6 ÷ -" or "2 ^ -"
            val hasTrailingNegativeAfterOp = trimmed.endsWith("× -") || trimmed.endsWith("* -") ||
                    trimmed.endsWith("÷ -") || trimmed.endsWith("/ -") ||
                    trimmed.endsWith("^ -") || trimmed.endsWith("(-")

            if (hasTrailingNegativeAfterOp) {
                if (isMinus) {
                    // Ignore extra minus
                    return
                } else {
                    // User pressed +, ×, ÷, ^: replace the operator combo with new op e.g. "6 × -" + "÷" -> "6 ÷ "
                    val withoutOp = trimmed.dropLast(3).trimEnd()
                    val updated = if (withoutOp.isEmpty()) "" else "$withoutOp $op "
                    _state.update { it.copy(expression = updated, cursorIndex = updated.length, errorMessage = null, isResultCalculated = false) }
                    return
                }
            }

            val endsWithMultiply = trimmed.endsWith("×") || trimmed.endsWith("*")
            val endsWithDivide = trimmed.endsWith("÷") || trimmed.endsWith("/")
            val endsWithPower = trimmed.endsWith("^")
            val endsWithPlus = trimmed.endsWith("+")
            val endsWithMinus = trimmed.endsWith("−") || trimmed.endsWith("-")
            val endsWithOpenParen = trimmed.endsWith("(")

            if (isMinus) {
                // When user types minus after multiply, divide, power, or open paren, append negative sign!
                // e.g. 6 × -3, 6 ÷ -2, 2 ^ -3, (-5)
                if (endsWithMultiply || endsWithDivide || endsWithPower || endsWithOpenParen) {
                    val updated = "$trimmed -"
                    _state.update { it.copy(expression = updated, cursorIndex = updated.length, errorMessage = null, isResultCalculated = false) }
                    return
                }
                // If previous is plus: 6 + and user taps -, replace with -
                if (endsWithPlus) {
                    val withoutLast = trimmed.dropLast(1).trimEnd()
                    val updated = "$withoutLast $op "
                    _state.update { it.copy(expression = updated, cursorIndex = updated.length, errorMessage = null, isResultCalculated = false) }
                    return
                }
                // If previous is minus: already minus, do nothing
                if (endsWithMinus) {
                    return
                }
            } else {
                // Non-minus operator (+, ×, ÷, ^)
                // If previous was an operator, replace it
                if (endsWithMultiply || endsWithDivide || endsWithPower || endsWithPlus || endsWithMinus) {
                    val withoutLast = trimmed.dropLast(1).trimEnd()
                    val updated = if (withoutLast.isEmpty()) "" else "$withoutLast $op "
                    _state.update { it.copy(expression = updated, cursorIndex = updated.length, errorMessage = null, isResultCalculated = false) }
                    return
                }
                if (endsWithOpenParen) {
                    return
                }
            }

            val updated = "$trimmed $op "
            _state.update { it.copy(expression = updated, cursorIndex = updated.length, errorMessage = null, isResultCalculated = false) }
        } else {
            // Cursor is in the middle of expression
            val trimmedBefore = before.trimEnd()
            val endsWithOp = trimmedBefore.endsWith("×") || trimmedBefore.endsWith("*") ||
                    trimmedBefore.endsWith("÷") || trimmedBefore.endsWith("/") ||
                    trimmedBefore.endsWith("^") || trimmedBefore.endsWith("+") ||
                    trimmedBefore.endsWith("−") || trimmedBefore.endsWith("-")

            val newBefore = if (endsWithOp) {
                val withoutLast = trimmedBefore.dropLast(1).trimEnd()
                if (withoutLast.isEmpty()) "" else "$withoutLast $op "
            } else {
                "$trimmedBefore $op "
            }
            val updated = newBefore + after.trimStart()
            val newCursor = newBefore.length
            _state.update { it.copy(expression = updated, cursorIndex = newCursor, errorMessage = null, isResultCalculated = false) }
        }
        updatePreview(_state.value.expression)
    }

    private fun appendSquareRoot() {
        insertAtCursor("√")
    }

    private fun appendCubeRoot() {
        insertAtCursor("³√")
    }

    private fun appendFactorial() {
        val current = _state.value.expression
        if (current.isEmpty()) return
        val cursor = sanitizeCursorIndex(current, _state.value.cursorIndex)
        if (cursor == 0) return
        val prevChar = current[cursor - 1]
        if (prevChar.isDigit() || prevChar == ')' || prevChar == 'π' || prevChar == 'e') {
            insertAtCursor("!")
        }
    }

    private fun appendPercentage() {
        if (_state.value.isResultCalculated && _state.value.finalResult.isNotEmpty()) {
            val chained = _state.value.finalResult + "%"
            _state.update {
                it.copy(
                    expression = chained,
                    cursorIndex = chained.length,
                    finalResult = "",
                    errorMessage = null,
                    isResultCalculated = false
                )
            }
            updatePreview(chained)
            return
        }
        val current = _state.value.expression
        if (current.isEmpty()) return
        val cursor = sanitizeCursorIndex(current, _state.value.cursorIndex)
        if (cursor == 0) return
        val prevChar = current[cursor - 1]
        if (prevChar.isDigit() || prevChar == ')' || prevChar == '!') {
            insertAtCursor("%")
        }
    }

    private fun appendConstant(symbol: String) {
        insertAtCursor(symbol)
    }

    private fun appendSymbol(symbol: String) {
        insertAtCursor(symbol)
    }

    private fun appendParenthesisOpen() {
        if (_state.value.isResultCalculated) {
            handleClearAll()
        }
        insertAtCursor("(")
    }

    private fun appendParenthesisClose() {
        if (_state.value.isResultCalculated) return

        val current = _state.value.expression
        if (current.isEmpty()) return

        val cursor = sanitizeCursorIndex(current, _state.value.cursorIndex)
        if (cursor == 0) return

        val before = current.substring(0, cursor)

        // Close bracket can only be given as input if total open brackets exceed total close brackets
        val totalOpen = current.count { it == '(' }
        val totalClose = current.count { it == ')' }
        if (totalOpen <= totalClose) return

        // And before the cursor there must be an unclosed open bracket
        val openBefore = before.count { it == '(' }
        val closeBefore = before.count { it == ')' }
        if (openBefore <= closeBefore) return

        // Must follow a valid operand (not '(', not an operator, and not '.')
        val lastChar = before.trimEnd().lastOrNull() ?: return
        if (lastChar == '(' || isOperatorChar(lastChar) || lastChar == '.') return

        insertAtCursor(")")
    }

    private fun handleBackspace() {
        val current = _state.value.expression
        if (current.isEmpty()) return

        val cursor = sanitizeCursorIndex(current, _state.value.cursorIndex)
        if (cursor == 0) return

        val before = current.substring(0, cursor)
        val after = current.substring(cursor)

        val multiCharTokens = listOf(
            "tanh⁻¹(", "cosh⁻¹(", "sinh⁻¹(",
            "tan⁻¹(", "cos⁻¹(", "sin⁻¹(",
            "^(-1)",
            "sinh(", "cosh(", "tanh(",
            "10^(", "sin(", "cos(", "tan(", "log(", "abs(",
            " + ", " − ", " - ", " × ", " * ", " ÷ ", " / ", " ^ ", " % ",
            "ln(", "e^(", "1/(",
            "^2", "^3", "(-", "³√(", "³√", "√("
        )

        var deleteCount = 1
        val matchedToken = multiCharTokens.firstOrNull { before.endsWith(it) }
        if (matchedToken != null) {
            deleteCount = matchedToken.length
        } else if (before.endsWith(" ")) {
            val trimmed = before.trimEnd()
            val spacesCount = before.length - trimmed.length
            if (trimmed.isNotEmpty() && (trimmed.last() in "+−-×*÷/^%")) {
                val withoutOp = trimmed.dropLast(1).trimEnd()
                deleteCount = before.length - withoutOp.length
            } else {
                deleteCount = spacesCount.coerceAtLeast(1)
            }
        }

        val newBefore = before.dropLast(deleteCount)
        val updated = newBefore + after
        val newCursor = (cursor - deleteCount).coerceAtLeast(0)

        _state.update {
            it.copy(
                expression = updated,
                cursorIndex = newCursor,
                finalResult = "",
                errorMessage = null,
                isResultCalculated = false
            )
        }
        updatePreview(updated)
    }

    private fun handleClearAll() {
        _state.update {
            it.copy(
                expression = "",
                cursorIndex = 0,
                previewResult = "",
                finalResult = "",
                errorMessage = null,
                isResultCalculated = false
            )
        }
    }

    private fun handlePaste(rawText: String) {
        val sanitized = ClipboardHelper.sanitizeCalculatorPaste(rawText)
        if (sanitized.isEmpty()) return

        if (_state.value.isResultCalculated) {
            _state.update {
                it.copy(
                    expression = sanitized,
                    cursorIndex = sanitized.length,
                    finalResult = "",
                    previewResult = "",
                    errorMessage = null,
                    isResultCalculated = false
                )
            }
            updatePreview(sanitized)
            return
        }

        insertAtCursor(sanitized, updatePreviewAfter = true)
    }

    private fun handleCut() {
        handleClearAll()
    }

    private fun appendScientificFunction(func: String) {
        insertAtCursor(func)
    }

    private fun appendSquare() {
        val current = _state.value.expression
        if (_state.value.isResultCalculated && _state.value.finalResult.isNotEmpty()) {
            val updated = _state.value.finalResult + "^2"
            _state.update {
                it.copy(
                    expression = updated,
                    cursorIndex = updated.length,
                    finalResult = "",
                    errorMessage = null,
                    isResultCalculated = false
                )
            }
            updatePreview(updated)
            return
        }
        if (current.isEmpty()) return
        insertAtCursor("^2")
    }

    private fun appendPowerCube() {
        val current = _state.value.expression
        if (_state.value.isResultCalculated && _state.value.finalResult.isNotEmpty()) {
            val updated = _state.value.finalResult + "^3"
            _state.update {
                it.copy(
                    expression = updated,
                    cursorIndex = updated.length,
                    finalResult = "",
                    errorMessage = null,
                    isResultCalculated = false
                )
            }
            updatePreview(updated)
            return
        }
        if (current.isEmpty()) return
        insertAtCursor("^3")
    }

    private fun appendReciprocal() {
        val current = _state.value.expression
        if (_state.value.isResultCalculated && _state.value.finalResult.isNotEmpty()) {
            val updated = _state.value.finalResult + "^(-1)"
            _state.update {
                it.copy(
                    expression = updated,
                    cursorIndex = updated.length,
                    finalResult = "",
                    errorMessage = null,
                    isResultCalculated = false
                )
            }
            updatePreview(updated)
            return
        }
        if (current.isEmpty()) {
            insertAtCursor("1/(")
            return
        }
        insertAtCursor("^(-1)")
    }

    private fun handleCalculate() {
        val current = _state.value.expression.trim()
        if (current.isEmpty()) return

        // Auto-balance open and close brackets so they are equal and produce no expression error
        val balancedExpression = autoBalanceParentheses(current)

        when (val res = evaluator.evaluate(balancedExpression, isDegreeMode = _state.value.isDegreeMode)) {
            is EvalResult.Success -> {
                val formattedResult = res.formatted
                _state.update {
                    it.copy(
                        expression = balancedExpression,
                        finalResult = formattedResult,
                        previewResult = "",
                        errorMessage = null,
                        isResultCalculated = true,
                        cursorIndex = balancedExpression.length
                    )
                }
                // Save to history database
                scope.launch {
                    repository.saveCalculation(balancedExpression, formattedResult)
                }
            }
            is EvalResult.Error -> {
                _state.update {
                    it.copy(
                        errorMessage = res.message,
                        finalResult = "",
                        isResultCalculated = false
                    )
                }
            }
        }
    }

    private fun updatePreview(expr: String) {
        val clean = expr.trim()
        if (clean.isEmpty() || clean.toDoubleOrNull() != null) {
            _state.update { it.copy(previewResult = "") }
            return
        }

        val balanced = autoBalanceParentheses(clean)

        when (val res = evaluator.evaluate(balanced, isDegreeMode = _state.value.isDegreeMode)) {
            is EvalResult.Success -> {
                _state.update { it.copy(previewResult = res.formatted) }
            }
            is EvalResult.Error -> {
                _state.update { it.copy(previewResult = "") }
            }
        }
    }

    private fun handleHistoryItemClicked(record: CalculationRecord, reuseResult: Boolean) {
        if (reuseResult) {
            // Append result to current expression
            val current = _state.value.expression
            val updated = if (current.isEmpty()) record.result else "$current ${record.result}"
            _state.update {
                it.copy(
                    expression = updated,
                    cursorIndex = updated.length,
                    isHistoryVisible = false,
                    isResultCalculated = false,
                    errorMessage = null
                )
            }
            updatePreview(updated)
        } else {
            // Load expression
            _state.update {
                it.copy(
                    expression = record.expression,
                    cursorIndex = record.expression.length,
                    finalResult = record.result,
                    previewResult = "",
                    isHistoryVisible = false,
                    isResultCalculated = true,
                    errorMessage = null
                )
            }
        }
    }

    class Factory(private val repository: ICalculatorRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
                return CalculatorViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        /**
         * Ensures that unclosed open parentheses are automatically closed by appending
         * the required number of ')' to make open and close bracket counts equal.
         */
        fun autoBalanceParentheses(expression: String): String {
            val openCount = expression.count { it == '(' }
            val closeCount = expression.count { it == ')' }
            return if (openCount > closeCount) {
                expression + ")".repeat(openCount - closeCount)
            } else {
                expression
            }
        }

        /**
         * Determines whether a character is an operator symbol.
         */
        fun isOperatorChar(c: Char): Boolean =
            c == '+' || c == '−' || c == '-' || c == '×' || c == '*' || c == '÷' || c == '/' || c == '^' || c == '%'

        /**
         * Ensures that the cursor cannot be positioned inside the visual padding spaces
         * surrounding operators (e.g. in "5 × ", cursor cannot sit between ' ' and '×' or between '×' and ' ').
         * If placed within the leading gap before an operator, it snaps cleanly before the spaces (after the left operand).
         * If placed within the trailing gap after an operator, it snaps cleanly after the spaces (before the right operand or end of text).
         */
        fun sanitizeCursorIndex(expression: String, index: Int): Int {
            if (expression.isEmpty()) return 0
            val clamped = index.coerceIn(0, expression.length)
            if (clamped <= 0 || clamped >= expression.length) return clamped

            // Check if cursor is right before an operator preceded by whitespace (e.g. at '×' in "5 |× ")
            if (isOperatorChar(expression[clamped])) {
                if (expression[clamped - 1] == ' ') {
                    var target = clamped - 1
                    while (target > 0 && expression[target - 1] == ' ') {
                        target--
                    }
                    return target
                }
            }

            // Check if cursor is on whitespace (e.g. "5 | × " or "5 × | ")
            if (expression[clamped] == ' ') {
                // Check if this whitespace leads into an operator
                var f = clamped
                while (f < expression.length && expression[f] == ' ') f++
                if (f < expression.length && isOperatorChar(expression[f])) {
                    var b = clamped
                    while (b > 0 && expression[b - 1] == ' ') b--
                    return b
                }

                // Check if this whitespace trails an operator
                var b = clamped
                while (b > 0 && expression[b - 1] == ' ') b--
                if (b > 0 && isOperatorChar(expression[b - 1])) {
                    var target = clamped
                    while (target < expression.length && expression[target] == ' ') target++
                    return target
                }
            }

            return clamped
        }
    }
}
