package com.calculator.app

import android.app.Application
import com.calculator.app.data.local.CalculatorDatabase
import com.calculator.app.data.preferences.NumberFormatPreferences
import com.calculator.app.data.preferences.ThemePreferences
import com.calculator.app.data.repository.CalculatorRepository

class CalculatorApplication : Application() {

    lateinit var repository: CalculatorRepository
        private set

    override fun onCreate() {
        super.onCreate()

        val database = CalculatorDatabase.getDatabase(this)
        val themePreferences = ThemePreferences(this)
        val numberFormatPreferences = NumberFormatPreferences(this)
        repository = CalculatorRepository(database.calculationDao(), themePreferences, numberFormatPreferences)
    }
}
