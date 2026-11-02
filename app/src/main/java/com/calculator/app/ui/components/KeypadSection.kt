package com.calculator.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calculator.app.ui.CalculatorAction
import com.calculator.app.ui.theme.LocalCalculatorColors

@Composable
fun KeypadSection(
    onAction: (CalculatorAction) -> Unit,
    isScientificMode: Boolean,
    isDegreeMode: Boolean,
    isInvMode: Boolean,
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false
) {
    val colors = LocalCalculatorColors.current
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp

    if (isLandscape) {
        // ==========================================
        // LANDSCAPE KEYPAD: Adaptive 5 rows
        // ==========================================
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .animateContentSize(animationSpec = tween(300, easing = FastOutSlowInEasing))
        ) {
            // Keypad Grid: Animated transition between Scientific & Standard
            AnimatedContent(
                targetState = isScientificMode,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220, delayMillis = 40, easing = LinearOutSlowInEasing)) +
                     scaleIn(initialScale = 0.97f, animationSpec = tween(250, easing = FastOutSlowInEasing)))
                        .togetherWith(
                            fadeOut(animationSpec = tween(150, easing = FastOutLinearInEasing)) +
                            scaleOut(targetScale = 0.99f, animationSpec = tween(150, easing = FastOutLinearInEasing))
                        )
                },
                contentAlignment = Alignment.Center,
                label = "LandscapeKeypadGridTransition",
                modifier = Modifier.fillMaxSize()
            ) { targetScientific ->
                if (targetScientific) {
                    // ==========================================
                    // SCIENTIFIC LANDSCAPE: 7 Columns x 5 Rows
                    // ==========================================
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Row 1: [sin (sin⁻¹)], [cos (cos⁻¹)], [tan (tan⁻¹)], [AC], [(], [)], [÷]
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = if (isInvMode) "sin⁻¹" else "sin",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = if (isInvMode) 11.5.sp else 13.sp,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.ScientificFunction(if (isInvMode) "sin⁻¹(" else "sin(")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = if (isInvMode) "cos⁻¹" else "cos",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = if (isInvMode) 11.5.sp else 13.sp,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.ScientificFunction(if (isInvMode) "cos⁻¹(" else "cos(")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = if (isInvMode) "tan⁻¹" else "tan",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = if (isInvMode) 11.5.sp else 13.sp,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.ScientificFunction(if (isInvMode) "tan⁻¹(" else "tan(")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "AC",
                                backgroundColor = colors.clearKeyBg,
                                contentColor = colors.clearKeyText,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.ClearAll) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "(",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 18.sp,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.ParenthesisOpen) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = ")",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 18.sp,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.ParenthesisClose) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "÷",
                                backgroundColor = colors.operatorKeyBg,
                                contentColor = colors.operatorKeyText,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.Operator("÷")) }
                            )
                        }

                        // Row 2: [sinh (sinh⁻¹)], [cosh (cosh⁻¹)], [tanh (tanh⁻¹)], [7], [8], [9], [×]
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = if (isInvMode) "sinh⁻¹" else "sinh",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = if (isInvMode) 10.5.sp else 12.sp,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.ScientificFunction(if (isInvMode) "sinh⁻¹(" else "sinh(")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = if (isInvMode) "cosh⁻¹" else "cosh",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = if (isInvMode) 10.5.sp else 12.sp,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.ScientificFunction(if (isInvMode) "cosh⁻¹(" else "cosh(")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = if (isInvMode) "tanh⁻¹" else "tanh",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = if (isInvMode) 10.5.sp else 12.sp,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.ScientificFunction(if (isInvMode) "tanh⁻¹(" else "tanh(")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "7",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.Number("7")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "8",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.Number("8")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "9",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.Number("9")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "×",
                                backgroundColor = colors.operatorKeyBg,
                                contentColor = colors.operatorKeyText,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.Operator("×")) }
                            )
                        }

                        // Row 3: [ln (eˣ)], [log (10ˣ)], [|x| (1/x)], [4], [5], [6], [−]
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = if (isInvMode) "eˣ" else "ln",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 13.sp,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.ScientificFunction(if (isInvMode) "e^(" else "ln(")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = if (isInvMode) "10ˣ" else "log",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = if (isInvMode) 11.5.sp else 13.sp,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.ScientificFunction(if (isInvMode) "10^(" else "log(")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = if (isInvMode) "1/x" else "|x|",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 13.sp,
                                shapeCornerRadius = 10.dp,
                                onClick = { if (isInvMode) onAction(CalculatorAction.Reciprocal) else onAction(CalculatorAction.ScientificFunction("abs(")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "4",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.Number("4")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "5",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.Number("5")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "6",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.Number("6")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "−",
                                backgroundColor = colors.operatorKeyBg,
                                contentColor = colors.operatorKeyText,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.Operator("−")) }
                            )
                        }

                        // Row 4: [e], [π], [x² (x³)], [1], [2], [3], [+]
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "e",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 14.sp,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.ConstantE) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "π",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 14.sp,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.ConstantPi) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = if (isInvMode) "x³" else "x²",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 13.sp,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(if (isInvMode) CalculatorAction.PowerCube else CalculatorAction.Square) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "1",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.Number("1")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "2",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.Number("2")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "3",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.Number("3")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "+",
                                backgroundColor = colors.operatorKeyBg,
                                contentColor = colors.operatorKeyText,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.Operator("+")) }
                            )
                        }

                        // Row 5: [√ (³√)], [^], [!], [0], [.], [%], [=]
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = if (isInvMode) "³√" else "√",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 14.sp,
                                shapeCornerRadius = 10.dp,
                                onClick = { if (isInvMode) onAction(CalculatorAction.CubeRoot) else onAction(CalculatorAction.SquareRoot) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "^",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 16.sp,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.Operator("^")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "!",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 15.sp,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.Factorial) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "0",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.Number("0")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = ".",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.Decimal) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "%",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.Percentage) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "=",
                                backgroundColor = colors.equalsKeyBg,
                                contentColor = colors.equalsKeyText,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                shapeCornerRadius = 10.dp,
                                onClick = { onAction(CalculatorAction.Calculate) }
                            )
                        }
                    }
                } else {
                    // STANDARD LANDSCAPE: 5 Rows of 4 Columns
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Row 1: AC, (, ), ÷
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "AC",
                                backgroundColor = colors.clearKeyBg,
                                contentColor = colors.clearKeyText,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                shapeCornerRadius = 12.dp,
                                onClick = { onAction(CalculatorAction.ClearAll) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "(",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 19.sp,
                                shapeCornerRadius = 12.dp,
                                onClick = { onAction(CalculatorAction.ParenthesisOpen) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = ")",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 19.sp,
                                shapeCornerRadius = 12.dp,
                                onClick = { onAction(CalculatorAction.ParenthesisClose) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "÷",
                                backgroundColor = colors.operatorKeyBg,
                                contentColor = colors.operatorKeyText,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium,
                                shapeCornerRadius = 12.dp,
                                onClick = { onAction(CalculatorAction.Operator("÷")) }
                            )
                        }

                        // Row 2: 7, 8, 9, ×
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "7",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 12.dp,
                                onClick = { onAction(CalculatorAction.Number("7")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "8",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 12.dp,
                                onClick = { onAction(CalculatorAction.Number("8")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "9",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 12.dp,
                                onClick = { onAction(CalculatorAction.Number("9")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "×",
                                backgroundColor = colors.operatorKeyBg,
                                contentColor = colors.operatorKeyText,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium,
                                shapeCornerRadius = 12.dp,
                                onClick = { onAction(CalculatorAction.Operator("×")) }
                            )
                        }

                        // Row 3: 4, 5, 6, −
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "4",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 12.dp,
                                onClick = { onAction(CalculatorAction.Number("4")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "5",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 12.dp,
                                onClick = { onAction(CalculatorAction.Number("5")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "6",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 12.dp,
                                onClick = { onAction(CalculatorAction.Number("6")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "−",
                                backgroundColor = colors.operatorKeyBg,
                                contentColor = colors.operatorKeyText,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium,
                                shapeCornerRadius = 12.dp,
                                onClick = { onAction(CalculatorAction.Operator("−")) }
                            )
                        }

                        // Row 4: 1, 2, 3, +
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "1",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 12.dp,
                                onClick = { onAction(CalculatorAction.Number("1")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "2",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 12.dp,
                                onClick = { onAction(CalculatorAction.Number("2")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "3",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 12.dp,
                                onClick = { onAction(CalculatorAction.Number("3")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "+",
                                backgroundColor = colors.operatorKeyBg,
                                contentColor = colors.operatorKeyText,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium,
                                shapeCornerRadius = 12.dp,
                                onClick = { onAction(CalculatorAction.Operator("+")) }
                            )
                        }

                        // Row 5: 0, ., %, =
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "0",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 12.dp,
                                onClick = { onAction(CalculatorAction.Number("0")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = ".",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold,
                                shapeCornerRadius = 12.dp,
                                onClick = { onAction(CalculatorAction.Decimal) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "%",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 12.dp,
                                onClick = { onAction(CalculatorAction.Percentage) }
                            )
                            CalculatorButton(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                text = "=",
                                backgroundColor = colors.equalsKeyBg,
                                contentColor = colors.equalsKeyText,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                shapeCornerRadius = 12.dp,
                                onClick = { onAction(CalculatorAction.Calculate) }
                            )
                        }
                    }
                }
            }
        }
    } else {
        // ==========================================
        // PORTRAIT KEYPAD: Standard / Scientific
        // ==========================================
        val sciRowSpacing = when {
            screenHeightDp < 620 -> 4.dp
            screenHeightDp < 680 -> 5.dp
            else -> 6.dp
        }
        val sciColSpacing = 4.dp
        val stdColSpacing = 10.dp

        val keypadHeight = when {
            screenHeightDp < 620 -> 330.dp
            screenHeightDp < 680 -> 360.dp
            screenHeightDp < 760 -> 390.dp
            else -> 420.dp
        }

        val sciButtonHeight = (keypadHeight - 4.dp - sciRowSpacing * 6) / 7
        val stdButtonHeight = (keypadHeight - 4.dp - stdColSpacing * 4) / 5

        BoxWithConstraints(
            modifier = modifier
                .fillMaxWidth()
                .height(keypadHeight)
                .padding(horizontal = 10.dp, vertical = 2.dp)
        ) {
            val totalWidth = maxWidth
            val sciColWidth = (totalWidth - sciColSpacing * 4) / 5
            val stdColWidth = (totalWidth - stdColSpacing * 3) / 4

            // Keypad Grid: Smooth Animated Transition between Scientific & Standard
            AnimatedContent(
                targetState = isScientificMode,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(200, delayMillis = 30, easing = LinearOutSlowInEasing)) +
                     scaleIn(initialScale = 0.98f, animationSpec = tween(200, easing = FastOutSlowInEasing)))
                        .togetherWith(
                            fadeOut(animationSpec = tween(120, easing = FastOutLinearInEasing)) +
                            scaleOut(targetScale = 0.99f, animationSpec = tween(120, easing = FastOutLinearInEasing))
                        )
                },
                contentAlignment = Alignment.Center,
                label = "PortraitKeypadTransition",
                modifier = Modifier.fillMaxSize()
            ) { targetScientific ->
                if (targetScientific) {
                    val rowSpacing = sciRowSpacing
                    val colSpacing = sciColSpacing

                    // ==========================================
                    // SCIENTIFIC PORTRAIT: 5 Columns x 7 Rows
                    // ==========================================
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(rowSpacing)
                    ) {
                        // Row 1: [sin (sin⁻¹)], [cos (cos⁻¹)], [tan (tan⁻¹)], [ln (eˣ)], [log (10ˣ)]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(colSpacing)
                        ) {
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = if (isInvMode) "sin⁻¹" else "sin",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = if (isInvMode) 11.5.sp else 13.5.sp,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.ScientificFunction(if (isInvMode) "sin⁻¹(" else "sin(")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = if (isInvMode) "cos⁻¹" else "cos",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = if (isInvMode) 11.5.sp else 13.5.sp,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.ScientificFunction(if (isInvMode) "cos⁻¹(" else "cos(")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = if (isInvMode) "tan⁻¹" else "tan",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = if (isInvMode) 11.5.sp else 13.5.sp,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.ScientificFunction(if (isInvMode) "tan⁻¹(" else "tan(")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = if (isInvMode) "eˣ" else "ln",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 13.sp,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.ScientificFunction(if (isInvMode) "e^(" else "ln(")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = if (isInvMode) "10ˣ" else "log",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = if (isInvMode) 11.5.sp else 13.sp,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.ScientificFunction(if (isInvMode) "10^(" else "log(")) }
                            )
                        }

                        // Row 2: [sinh (sinh⁻¹)], [cosh (cosh⁻¹)], [tanh (tanh⁻¹)], [|x| (1/x)], [x² (x³)]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(colSpacing)
                        ) {
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = if (isInvMode) "sinh⁻¹" else "sinh",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = if (isInvMode) 10.5.sp else 12.sp,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.ScientificFunction(if (isInvMode) "sinh⁻¹(" else "sinh(")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = if (isInvMode) "cosh⁻¹" else "cosh",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = if (isInvMode) 10.5.sp else 12.sp,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.ScientificFunction(if (isInvMode) "cosh⁻¹(" else "cosh(")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = if (isInvMode) "tanh⁻¹" else "tanh",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = if (isInvMode) 10.5.sp else 12.sp,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.ScientificFunction(if (isInvMode) "tanh⁻¹(" else "tanh(")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = if (isInvMode) "1/x" else "|x|",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 13.sp,
                                shapeCornerRadius = 11.dp,
                                onClick = { if (isInvMode) onAction(CalculatorAction.Reciprocal) else onAction(CalculatorAction.ScientificFunction("abs(")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = if (isInvMode) "x³" else "x²",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 13.sp,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(if (isInvMode) CalculatorAction.PowerCube else CalculatorAction.Square) }
                            )
                        }

                        // Row 3: [e], [AC], [(], [)], [÷]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(colSpacing)
                        ) {
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = "e",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 18.sp,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.ConstantE) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = "AC",
                                backgroundColor = colors.clearKeyBg,
                                contentColor = colors.clearKeyText,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.ClearAll) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = "(",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 25.sp,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.ParenthesisOpen) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = ")",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 25.sp,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.ParenthesisClose) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = "÷",
                                backgroundColor = colors.operatorKeyBg,
                                contentColor = colors.operatorKeyText,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Medium,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.Operator("÷")) }
                            )
                        }

                        // Row 4: [√(³√)], [7], [8], [9], [×]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(colSpacing)
                        ) {
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = if (isInvMode) "³√" else "√",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 18.sp,
                                shapeCornerRadius = 11.dp,
                                onClick = { if (isInvMode) onAction(CalculatorAction.CubeRoot) else onAction(CalculatorAction.SquareRoot) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = "7",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.Number("7")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = "8",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.Number("8")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = "9",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.Number("9")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = "×",
                                backgroundColor = colors.operatorKeyBg,
                                contentColor = colors.operatorKeyText,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Medium,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.Operator("×")) }
                            )
                        }

                        // Row 5: [^], [4], [5], [6], [−]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(colSpacing)
                        ) {
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = "^",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 22.sp,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.Operator("^")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = "4",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.Number("4")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = "5",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.Number("5")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = "6",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.Number("6")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = "−",
                                backgroundColor = colors.operatorKeyBg,
                                contentColor = colors.operatorKeyText,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Medium,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.Operator("−")) }
                            )
                        }

                        // Row 6: [π], [1], [2], [3], [+]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(colSpacing)
                        ) {
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = "π",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 19.sp,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.ConstantPi) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = "1",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.Number("1")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = "2",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.Number("2")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = "3",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.Number("3")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = "+",
                                backgroundColor = colors.operatorKeyBg,
                                contentColor = colors.operatorKeyText,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Medium,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.Operator("+")) }
                            )
                        }

                        // Row 7: [!], [0], [.], [%], [=]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(colSpacing)
                        ) {
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = "!",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.Factorial) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = "0",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.Number("0")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = ".",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.Decimal) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = "%",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 25.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.Percentage) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(sciColWidth).height(sciButtonHeight),
                                text = "=",
                                backgroundColor = colors.equalsKeyBg,
                                contentColor = colors.equalsKeyText,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                shapeCornerRadius = 11.dp,
                                onClick = { onAction(CalculatorAction.Calculate) }
                            )
                        }
                    }
                } else {
                    // ==========================================
                    // STANDARD PORTRAIT: 4 Columns x 5 Rows
                    // ==========================================
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(stdColSpacing)
                    ) {
                        // Standard Row 1: AC, (, ), ÷
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CalculatorButton(
                                modifier = Modifier.width(stdColWidth).height(stdButtonHeight),
                                text = "AC",
                                backgroundColor = colors.clearKeyBg,
                                contentColor = colors.clearKeyText,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                shapeCornerRadius = 16.dp,
                                onClick = { onAction(CalculatorAction.ClearAll) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(stdColWidth).height(stdButtonHeight),
                                text = "(",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 30.sp,
                                shapeCornerRadius = 16.dp,
                                onClick = { onAction(CalculatorAction.ParenthesisOpen) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(stdColWidth).height(stdButtonHeight),
                                text = ")",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 30.sp,
                                shapeCornerRadius = 16.dp,
                                onClick = { onAction(CalculatorAction.ParenthesisClose) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(stdColWidth).height(stdButtonHeight),
                                text = "÷",
                                backgroundColor = colors.operatorKeyBg,
                                contentColor = colors.operatorKeyText,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Medium,
                                shapeCornerRadius = 16.dp,
                                onClick = { onAction(CalculatorAction.Operator("÷")) }
                            )
                        }

                        // Standard Row 2: 7, 8, 9, ×
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CalculatorButton(
                                modifier = Modifier.width(stdColWidth).height(stdButtonHeight),
                                text = "7",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 16.dp,
                                onClick = { onAction(CalculatorAction.Number("7")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(stdColWidth).height(stdButtonHeight),
                                text = "8",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 16.dp,
                                onClick = { onAction(CalculatorAction.Number("8")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(stdColWidth).height(stdButtonHeight),
                                text = "9",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 16.dp,
                                onClick = { onAction(CalculatorAction.Number("9")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(stdColWidth).height(stdButtonHeight),
                                text = "×",
                                backgroundColor = colors.operatorKeyBg,
                                contentColor = colors.operatorKeyText,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Medium,
                                shapeCornerRadius = 16.dp,
                                onClick = { onAction(CalculatorAction.Operator("×")) }
                            )
                        }

                        // Standard Row 3: 4, 5, 6, −
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CalculatorButton(
                                modifier = Modifier.width(stdColWidth).height(stdButtonHeight),
                                text = "4",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 16.dp,
                                onClick = { onAction(CalculatorAction.Number("4")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(stdColWidth).height(stdButtonHeight),
                                text = "5",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 16.dp,
                                onClick = { onAction(CalculatorAction.Number("5")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(stdColWidth).height(stdButtonHeight),
                                text = "6",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 16.dp,
                                onClick = { onAction(CalculatorAction.Number("6")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(stdColWidth).height(stdButtonHeight),
                                text = "−",
                                backgroundColor = colors.operatorKeyBg,
                                contentColor = colors.operatorKeyText,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Medium,
                                shapeCornerRadius = 16.dp,
                                onClick = { onAction(CalculatorAction.Operator("−")) }
                            )
                        }

                        // Standard Row 4: 1, 2, 3, +
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CalculatorButton(
                                modifier = Modifier.width(stdColWidth).height(stdButtonHeight),
                                text = "1",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 16.dp,
                                onClick = { onAction(CalculatorAction.Number("1")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(stdColWidth).height(stdButtonHeight),
                                text = "2",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 16.dp,
                                onClick = { onAction(CalculatorAction.Number("2")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(stdColWidth).height(stdButtonHeight),
                                text = "3",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 16.dp,
                                onClick = { onAction(CalculatorAction.Number("3")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(stdColWidth).height(stdButtonHeight),
                                text = "+",
                                backgroundColor = colors.operatorKeyBg,
                                contentColor = colors.operatorKeyText,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Medium,
                                shapeCornerRadius = 16.dp,
                                onClick = { onAction(CalculatorAction.Operator("+")) }
                            )
                        }

                        // Standard Row 5: 0, ., %, =
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CalculatorButton(
                                modifier = Modifier.width(stdColWidth).height(stdButtonHeight),
                                text = "0",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 16.dp,
                                onClick = { onAction(CalculatorAction.Number("0")) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(stdColWidth).height(stdButtonHeight),
                                text = ".",
                                backgroundColor = colors.numberKeyBg,
                                contentColor = colors.numberKeyText,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                shapeCornerRadius = 16.dp,
                                onClick = { onAction(CalculatorAction.Decimal) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(stdColWidth).height(stdButtonHeight),
                                text = "%",
                                backgroundColor = colors.functionKeyBg,
                                contentColor = colors.functionKeyText,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.SemiBold,
                                shapeCornerRadius = 16.dp,
                                onClick = { onAction(CalculatorAction.Percentage) }
                            )
                            CalculatorButton(
                                modifier = Modifier.width(stdColWidth).height(stdButtonHeight),
                                text = "=",
                                backgroundColor = colors.equalsKeyBg,
                                contentColor = colors.equalsKeyText,
                                fontSize = 35.sp,
                                fontWeight = FontWeight.Bold,
                                shapeCornerRadius = 16.dp,
                                onClick = { onAction(CalculatorAction.Calculate) }
                            )
                        }
                    }
                }
            }
        }
    }
}
