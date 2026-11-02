package com.calculator.app.data.repository

import com.calculator.app.data.local.CalculationDao
import com.calculator.app.data.local.CalculationRecord
import com.calculator.app.data.preferences.AppThemeMode
import com.calculator.app.data.preferences.NumberFormatPreferences
import com.calculator.app.data.preferences.NumberingSystem
import com.calculator.app.data.preferences.ThemePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ICalculatorRepository {
    val history: Flow<List<CalculationRecord>>
    val themeMode: StateFlow<AppThemeMode>
    val numberingSystem: StateFlow<NumberingSystem>
    suspend fun saveCalculation(expression: String, result: String)
    suspend fun deleteCalculation(record: CalculationRecord)
    suspend fun clearHistory()
    fun setThemeMode(mode: AppThemeMode)
    fun setNumberingSystem(system: NumberingSystem)
}

class CalculatorRepository(
    private val calculationDao: CalculationDao,
    private val themePreferences: ThemePreferences,
    private val numberFormatPreferences: NumberFormatPreferences
) : ICalculatorRepository {
    override val history: Flow<List<CalculationRecord>> = calculationDao.getAllHistory()

    override val themeMode: StateFlow<AppThemeMode> = themePreferences.themeMode

    override val numberingSystem: StateFlow<NumberingSystem> = numberFormatPreferences.numberingSystem

    override suspend fun saveCalculation(expression: String, result: String) {
        val record = CalculationRecord(
            expression = expression,
            result = result,
            timestamp = System.currentTimeMillis()
        )
        calculationDao.insertCalculation(record)
    }

    override suspend fun deleteCalculation(record: CalculationRecord) {
        calculationDao.deleteCalculation(record)
    }

    override suspend fun clearHistory() {
        calculationDao.clearAll()
    }

    override fun setThemeMode(mode: AppThemeMode) {
        themePreferences.setThemeMode(mode)
    }

    override fun setNumberingSystem(system: NumberingSystem) {
        numberFormatPreferences.setNumberingSystem(system)
    }
}
