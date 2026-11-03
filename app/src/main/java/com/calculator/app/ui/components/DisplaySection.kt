package com.calculator.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calculator.app.data.preferences.NumberingSystem
import com.calculator.app.domain.format.NumberFormatter
import com.calculator.app.ui.theme.LocalCalculatorColors

@Composable
fun DisplaySection(
    expression: String,
    previewResult: String,
    finalResult: String,
    errorMessage: String?,
    isResultCalculated: Boolean,
    cursorIndex: Int,
    onCursorChange: (Int) -> Unit,
    numberingSystem: NumberingSystem = NumberingSystem.INTERNATIONAL,
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false,
    onCut: (() -> Unit)? = null,
    onCopy: (() -> Unit)? = null,
    onPaste: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val colors = LocalCalculatorColors.current
    val haptic = LocalHapticFeedback.current
    val focusRequester = remember { FocusRequester() }

    val horizontalPadding = if (isLandscape) 14.dp else 18.dp
    val verticalPadding = if (isLandscape) 8.dp else 16.dp

    val exprFontSize = when {
        isLandscape && isResultCalculated -> 22.sp
        isLandscape -> 32.sp
        isResultCalculated -> 28.sp
        else -> 44.sp
    }

    val resultFontSize = if (isLandscape) 34.sp else 46.sp
    val previewFontSize = if (isLandscape) 22.sp else 28.sp
    val resultBoxHeight = if (isLandscape) 42.dp else 56.dp

    val formattedFinalResult = remember(finalResult, numberingSystem) {
        NumberFormatter.formatNumber(finalResult, numberingSystem)
    }
    val formattedPreviewResult = remember(previewResult, numberingSystem) {
        NumberFormatter.formatNumber(previewResult, numberingSystem)
    }

    var textFieldValue by remember(expression, cursorIndex) {
        val clamped = cursorIndex.coerceIn(0, expression.length)
        mutableStateOf(
            TextFieldValue(
                text = expression,
                selection = TextRange(clamped)
            )
        )
    }

    // Auto-focus the expression field when editing
    LaunchedEffect(isResultCalculated) {
        if (!isResultCalculated) {
            try {
                focusRequester.requestFocus()
            } catch (_: Throwable) {}
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        horizontalAlignment = Alignment.End,
        verticalArrangement = if (isLandscape) Arrangement.Center else Arrangement.Bottom
    ) {
        // Main Expression Area: Uses the EXACT native BasicTextField from the Finance tool
        if (!isResultCalculated) {
            NoSoftKeyboard {
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        if (newValue.text != expression) {
                            if (newValue.text.isEmpty()) {
                                onCut?.invoke()
                            } else {
                                onPaste?.invoke(newValue.text)
                            }
                        } else if (newValue.selection.start != cursorIndex) {
                            onCursorChange(newValue.selection.start)
                        }
                        textFieldValue = newValue
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = exprFontSize,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary,
                        textAlign = TextAlign.End
                    ),
                    cursorBrush = SolidColor(colors.operatorKeyText),
                    visualTransformation = remember(numberingSystem) {
                        ExpressionVisualTransformation(numberingSystem)
                    },
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            if (expression.isEmpty()) {
                                Text(
                                    text = "0",
                                    fontSize = exprFontSize,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textSecondary.copy(alpha = 0.5f),
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            }
        } else {
            // When result is calculated, expression is shown in secondary style
            val formattedExpr = remember(expression, numberingSystem) {
                NumberFormatter.formatExpression(expression, numberingSystem).formattedText
            }
            Text(
                text = formattedExpr.ifEmpty { "0" },
                color = colors.textSecondary,
                fontSize = exprFontSize,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(expression) {
                        detectTapGestures(
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                ClipboardHelper.copy(context, expression, "Expression")
                            }
                        )
                    }
            )
        }

        Spacer(modifier = Modifier.height(if (isLandscape) 4.dp else 8.dp))

        // Result or Live Preview or Error Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(resultBoxHeight),
            contentAlignment = Alignment.CenterEnd
        ) {
            when {
                errorMessage != null -> {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = if (isLandscape) 16.sp else 20.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                isResultCalculated && formattedFinalResult.isNotEmpty() -> {
                    Text(
                        text = "= $formattedFinalResult",
                        color = colors.textPrimary,
                        fontSize = resultFontSize,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.pointerInput(finalResult) {
                            detectTapGestures(
                                onLongPress = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    ClipboardHelper.copy(context, finalResult, "Final Result")
                                }
                            )
                        }
                    )
                }
                formattedPreviewResult.isNotEmpty() -> {
                    PreviewResultText(
                        previewResult = formattedPreviewResult,
                        colors = colors,
                        fontSize = previewFontSize,
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            ClipboardHelper.copy(context, previewResult, "Preview Result")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewResultText(
    previewResult: String,
    colors: com.calculator.app.ui.theme.CalculatorColors,
    fontSize: androidx.compose.ui.unit.TextUnit = 28.sp,
    onLongClick: (() -> Unit)? = null
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Text(
            text = "= $previewResult",
            color = colors.textSecondary.copy(alpha = 0.85f),
            fontSize = fontSize,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.pointerInput(onLongClick) {
                if (onLongClick != null) {
                    detectTapGestures(
                        onLongPress = { onLongClick() }
                    )
                }
            }
        )
    }
}
