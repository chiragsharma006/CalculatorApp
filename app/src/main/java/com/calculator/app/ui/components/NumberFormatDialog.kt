package com.calculator.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calculator.app.data.preferences.NumberingSystem
import com.calculator.app.ui.theme.LocalCalculatorColors

@Composable
fun NumberFormatDialog(
    selectedSystem: NumberingSystem,
    onSelectSystem: (NumberingSystem) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalCalculatorColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Numbering System",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Choose how numbers are formatted and grouped across the app:",
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                NumberFormatOptionItem(
                    title = "International",
                    example = "1,000,000",
                    description = "Thousands, Millions, Billions",
                    selected = selectedSystem == NumberingSystem.INTERNATIONAL,
                    onClick = { onSelectSystem(NumberingSystem.INTERNATIONAL) }
                )

                NumberFormatOptionItem(
                    title = "Indian",
                    example = "10,00,000",
                    description = "Hundreds, Thousands, Lakhs, Crores",
                    selected = selectedSystem == NumberingSystem.INDIAN,
                    onClick = { onSelectSystem(NumberingSystem.INDIAN) }
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
private fun NumberFormatOptionItem(
    title: String,
    example: String,
    description: String,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Surface(
                    color = colors.operatorKeyBg.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = example,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.operatorKeyText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = description,
                fontSize = 12.sp,
                color = colors.textSecondary
            )
        }
    }
}
