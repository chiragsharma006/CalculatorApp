package com.calculator.app.ui.navigation

import com.calculator.app.domain.tools.UnitCategory

sealed interface AppScreen {
    data object Calculator : AppScreen
    data object ToolsHub : AppScreen
    data class UnitConverter(val category: UnitCategory = UnitCategory.CURRENCY) : AppScreen
    data object NumberSystem : AppScreen
    data object LoanEmi : AppScreen
    data object InvestmentSip : AppScreen
    data object Gst : AppScreen
    data object Discount : AppScreen
    data object Bmi : AppScreen
    data object Age : AppScreen
    data object DateCalculator : AppScreen
    data object TimeCalculator : AppScreen
}
