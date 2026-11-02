package com.calculator.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calculator.app.R
import com.calculator.app.data.preferences.AppThemeMode
import com.calculator.app.ui.theme.LocalCalculatorColors

@Composable
fun ThemeSelectionDialog(
    selectedTheme: AppThemeMode,
    onSelectTheme: (AppThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalCalculatorColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.choose_theme),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ThemeOptionItem(
                    title = stringResource(R.string.theme_system),
                    subtitle = "Matches your device setting",
                    selected = selectedTheme == AppThemeMode.SYSTEM,
                    onClick = { onSelectTheme(AppThemeMode.SYSTEM) }
                )
                ThemeOptionItem(
                    title = stringResource(R.string.theme_light),
                    subtitle = "Clean and bright look",
                    selected = selectedTheme == AppThemeMode.LIGHT,
                    onClick = { onSelectTheme(AppThemeMode.LIGHT) }
                )
                ThemeOptionItem(
                    title = stringResource(R.string.theme_dark),
                    subtitle = "Deep pure black AMOLED theme",
                    selected = selectedTheme == AppThemeMode.DARK,
                    onClick = { onSelectTheme(AppThemeMode.DARK) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Done",
                    color = colors.operatorKeyText,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        containerColor = colors.surface
    )
}

@Composable
private fun ThemeOptionItem(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalCalculatorColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = colors.operatorKeyText,
                unselectedColor = colors.textSecondary
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = colors.textSecondary
            )
        }
    }
}
