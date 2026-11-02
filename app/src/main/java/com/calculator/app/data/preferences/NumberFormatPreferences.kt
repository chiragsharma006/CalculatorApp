package com.calculator.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NumberFormatPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("calculator_number_prefs", Context.MODE_PRIVATE)

    private val _numberingSystem = MutableStateFlow(loadSavedSystem())
    val numberingSystem: StateFlow<NumberingSystem> = _numberingSystem.asStateFlow()

    private fun loadSavedSystem(): NumberingSystem {
        val name = prefs.getString(KEY_NUMBERING_SYSTEM, NumberingSystem.INTERNATIONAL.name)
            ?: NumberingSystem.INTERNATIONAL.name
        return try {
            NumberingSystem.valueOf(name)
        } catch (e: Exception) {
            NumberingSystem.INTERNATIONAL
        }
    }

    fun setNumberingSystem(system: NumberingSystem) {
        prefs.edit().putString(KEY_NUMBERING_SYSTEM, system.name).apply()
        _numberingSystem.value = system
    }

    companion object {
        private const val KEY_NUMBERING_SYSTEM = "key_numbering_system"
    }
}
