package com.calculator.app.ui

import com.calculator.app.data.local.CalculationRecord
import com.calculator.app.data.preferences.AppThemeMode
import com.calculator.app.data.preferences.NumberingSystem

sealed interface CalculatorAction {
    data class Number(val digit: String) : CalculatorAction
    data object Decimal : CalculatorAction
    data class Operator(val symbol: String) : CalculatorAction
    data object SquareRoot : CalculatorAction
    data object CubeRoot : CalculatorAction
    data object Factorial : CalculatorAction
    data object Percentage : CalculatorAction
    data object ParenthesisOpen : CalculatorAction
    data object ParenthesisClose : CalculatorAction
    data object ConstantPi : CalculatorAction
    data object ConstantE : CalculatorAction
    data object Backspace : CalculatorAction
    data class MoveCursor(val newIndex: Int) : CalculatorAction
    data object ClearAll : CalculatorAction
    data object Calculate : CalculatorAction
    data class Paste(val text: String) : CalculatorAction
    data object Cut : CalculatorAction

    data object ToggleScientificMode : CalculatorAction
    data object ToggleAngleMode : CalculatorAction
    data object ToggleInvMode : CalculatorAction
    data class ScientificFunction(val function: String) : CalculatorAction
    data object Square : CalculatorAction
    data object PowerCube : CalculatorAction
    data object Reciprocal : CalculatorAction

    data class ToggleHistorySheet(val show: Boolean) : CalculatorAction
    data class ToggleThemeDialog(val show: Boolean) : CalculatorAction
    data class ToggleAppInfoDialog(val show: Boolean) : CalculatorAction
    data class ToggleNumberFormatDialog(val show: Boolean) : CalculatorAction
    data object ToggleTheme : CalculatorAction
    data class SelectTheme(val mode: AppThemeMode) : CalculatorAction
    data class SelectNumberingSystem(val system: NumberingSystem) : CalculatorAction
    data class HistoryItemClicked(val record: CalculationRecord, val reuseResult: Boolean = false) : CalculatorAction
    data class DeleteHistoryItem(val record: CalculationRecord) : CalculatorAction
    data object ClearAllHistory : CalculatorAction
}
