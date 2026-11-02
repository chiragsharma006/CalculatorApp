package com.calculator.app.ui

import com.calculator.app.data.local.CalculationRecord
import com.calculator.app.data.preferences.AppThemeMode
import com.calculator.app.data.preferences.NumberingSystem

data class CalculatorState(
    val expression: String = "",
    val cursorIndex: Int = 0,
    val previewResult: String = "",
    val finalResult: String = "",
    val errorMessage: String? = null,
    val isResultCalculated: Boolean = false,
    val isHistoryVisible: Boolean = false,
    val isThemeDialogVisible: Boolean = false,
    val isAppInfoDialogVisible: Boolean = false,
    val isNumberFormatDialogVisible: Boolean = false,
    val isScientificMode: Boolean = false,
    val isDegreeMode: Boolean = true,
    val isInvMode: Boolean = false,
    val historyList: List<CalculationRecord> = emptyList(),
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val numberingSystem: NumberingSystem = NumberingSystem.INTERNATIONAL
)
