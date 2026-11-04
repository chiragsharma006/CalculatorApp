package com.calculator.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calculator.app.R
import com.calculator.app.ui.theme.LocalCalculatorColors

@Composable
fun AppInfoDialog(
    onDismiss: () -> Unit
) {
    val colors = LocalCalculatorColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_icon),
                    contentDescription = "App Icon",
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Column {
                    Text(
                        text = "Calculator",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Version 1.4.0 • All-in-One Suite",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.operatorKeyText
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "A modern scientific calculator, unit converter, and financial tool suite engineered for precision, speed, and comfort.",
                    fontSize = 13.5.sp,
                    color = colors.textSecondary,
                    lineHeight = 19.sp
                )

                HorizontalDivider(
                    color = colors.textSecondary.copy(alpha = 0.15f),
                    thickness = 1.dp
                )

                // Feature Group 1: Core Calculator
                InfoSectionHeader(
                    icon = Icons.Default.Calculate,
                    title = "Scientific Calculator",
                    tint = colors.operatorKeyText
                )
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    FeatureBullet(text = "Standard & Scientific modes with DEG / RAD angle toggle")
                    FeatureBullet(text = "Smart parenthesis validation & automatic bracket balancing on evaluation")
                    FeatureBullet(text = "High-visibility typography: boosted font sizes for numpad digits & scientific functions")
                    FeatureBullet(text = "Inverse functions (sin⁻¹, cos⁻¹, tan⁻¹), powers (xʸ) & factorials (n!)")
                    FeatureBullet(text = "Smart percentages (+10%, −15%, standalone %) & mathematical constants (π, e)")
                    FeatureBullet(text = "Persistent calculation history with Room database")
                }

                // Feature Group 2: Draggable Cursor
                InfoSectionHeader(
                    icon = Icons.Default.TouchApp,
                    title = "Interactive Native Cursor & Clipboard",
                    tint = Color(0xFF0EA5E9)
                )
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    FeatureBullet(text = "Native Android text editor cursor with precision loupe & teardrop handle")
                    FeatureBullet(text = "Smart cursor snapping that protects operator spacing & equation integrity")
                    FeatureBullet(text = "Universal Cut, Copy, and Paste support across the calculator and tools")
                    FeatureBullet(text = "Direct cursor positioning for effortless in-place editing across the app")
                }

                // Feature Group 3: Number Formatting
                InfoSectionHeader(
                    icon = Icons.Default.Language,
                    title = "Flexible Number Systems",
                    tint = Color(0xFF10B981)
                )
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    FeatureBullet(text = "Indian numbering format (Lakhs & Crores, e.g. 12,34,567)")
                    FeatureBullet(text = "International numbering format (Millions, e.g. 1,234,567)")
                    FeatureBullet(text = "Instant 3-dot menu toggle active everywhere in calculations and tools")
                }

                // Feature Group 4: Converters & Tools
                InfoSectionHeader(
                    icon = Icons.Default.Widgets,
                    title = "Converters & Tools Hub",
                    tint = Color(0xFF8B5CF6)
                )
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    FeatureBullet(text = "11 Unit Converters with live forex exchange rates (Currency, Length, Weight, Temp, Speed, Storage, etc.)")
                    FeatureBullet(text = "Non-colliding arc swap animation with 180° animated toggles & fluid card shuffle")
                    FeatureBullet(text = "Smart input replacement: entering digits seamlessly replaces default presets")
                    FeatureBullet(text = "Financial Calculators: Loan EMI, Investment (SIP), GST, Discount")
                    FeatureBullet(text = "Date & Time differences, duration add/subtract & Age calculator")
                    FeatureBullet(text = "Health & Utilities: BMI Calculator & Multi-Base Programmer Converter (HEX, DEC, OCT, BIN)")
                }

                // Feature Group 5: Themes
                InfoSectionHeader(
                    icon = Icons.Default.Palette,
                    title = "Themes & Display",
                    tint = Color(0xFFF59E0B)
                )
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    FeatureBullet(text = "Light Mode, Dark Pitch Black (battery-saving OLED), and System default")
                    FeatureBullet(text = "Adaptive landscape split-pane layouts for tablets and phones")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Close",
                    color = colors.operatorKeyText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        },
        containerColor = colors.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun InfoSectionHeader(icon: ImageVector, title: String, tint: Color) {
    val colors = LocalCalculatorColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(15.dp)
            )
        }
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
    }
}

@Composable
private fun FeatureBullet(text: String) {
    val colors = LocalCalculatorColors.current
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(start = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = colors.operatorKeyText,
            modifier = Modifier
                .size(15.dp)
                .padding(top = 2.dp)
        )
        Text(
            text = text,
            fontSize = 12.5.sp,
            color = colors.textSecondary,
            lineHeight = 17.sp
        )
    }
}
