package com.calculator.app.domain.tools

enum class UnitCategory(val displayName: String) {
    CURRENCY("Currency"),
    LENGTH("Length"),
    AREA("Area"),
    VOLUME("Volume"),
    WEIGHT("Weight / Mass"),
    TEMPERATURE("Temperature"),
    SPEED("Speed"),
    PRESSURE("Pressure"),
    POWER("Power"),
    DATA_STORAGE("Data Storage"),
    DATA_SPEED("Data Speed")
}

data class UnitItem(
    val id: String,
    val name: String,
    val symbol: String,
    val ratioToBase: Double = 1.0 // Ratio where value_in_base = value * ratioToBase
)
