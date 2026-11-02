package com.calculator.app.data.preferences

import androidx.compose.runtime.compositionLocalOf

enum class NumberingSystem(
    val title: String,
    val example: String,
    val description: String
) {
    INTERNATIONAL(
        title = "International",
        example = "1,000,000",
        description = "Thousands, Millions, Billions"
    ),
    INDIAN(
        title = "Indian",
        example = "10,00,000",
        description = "Hundreds, Thousands, Lakhs, Crores"
    )
}

/**
 * CompositionLocal to access the user's chosen NumberingSystem across the entire app.
 */
val LocalNumberingSystem = compositionLocalOf { NumberingSystem.INTERNATIONAL }
