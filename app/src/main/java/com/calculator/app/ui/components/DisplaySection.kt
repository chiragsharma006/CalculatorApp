package com.calculator.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
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
    isLandscape: Boolean = false
) {
    val colors = LocalCalculatorColors.current
    val haptic = LocalHapticFeedback.current
    val viewConfig = LocalViewConfiguration.current
    val scrollState = rememberScrollState()
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var isDraggingCursor by remember { mutableStateOf(false) }

    // Format expression and compute bidirectional cursor mapping
    val formattedExpression = remember(expression, numberingSystem) {
        NumberFormatter.formatExpression(expression, numberingSystem)
    }
    val displayText = if (formattedExpression.formattedText.isEmpty()) "0" else formattedExpression.formattedText
    val isPlaceholder = formattedExpression.formattedText.isEmpty()
    val formattedCursorIndex = remember(expression, cursorIndex, numberingSystem) {
        NumberFormatter.rawToFormattedIndex(cursorIndex, formattedExpression)
    }

    val currentExpression by rememberUpdatedState(expression)
    val currentCursorIndex by rememberUpdatedState(cursorIndex)
    val currentOnCursorChange by rememberUpdatedState(onCursorChange)
    val currentFormattedExpression by rememberUpdatedState(formattedExpression)
    val currentTextLayoutResult by rememberUpdatedState(textLayoutResult)
    val currentHaptic by rememberUpdatedState(haptic)
    val currentIsPlaceholder by rememberUpdatedState(isPlaceholder)

    fun updateCursorFromOffset(offset: Offset) {
        if (currentIsPlaceholder || currentExpression.isEmpty()) {
            currentOnCursorChange(0)
            return
        }
        val layout = currentTextLayoutResult ?: return
        val centerY = (layout.size.height / 2f).coerceAtLeast(1f)
        val charIndex = layout.getOffsetForPosition(Offset(offset.x, centerY))
        val rawIndex = NumberFormatter.formattedToRawIndex(charIndex, currentFormattedExpression).coerceIn(0, currentExpression.length)
        if (rawIndex != currentCursorIndex) {
            currentHaptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            currentOnCursorChange(rawIndex)
        }
    }

    // Blinking animation for the editing cursor
    val infiniteTransition = rememberInfiniteTransition(label = "cursorTransition")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 530, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorBlink"
    )

    // Keep cursor visible when cursorIndex, expression, or numbering system changes
    LaunchedEffect(expression, cursorIndex, numberingSystem) {
        if (!isDraggingCursor) {
            val layout = textLayoutResult
            if (layout != null && displayText.isNotEmpty()) {
                val clamped = formattedCursorIndex.coerceIn(0, displayText.length)
                val rect = layout.getCursorRect(clamped)
                val cursorX = rect.left
                val viewportSize = scrollState.viewportSize
                if (viewportSize > 0) {
                    val currentScroll = scrollState.value
                    if (cursorX < currentScroll + 40) {
                        scrollState.animateScrollTo((cursorX - 60).toInt().coerceAtLeast(0))
                    } else if (cursorX > currentScroll + viewportSize - 40) {
                        scrollState.animateScrollTo((cursorX - viewportSize + 60).toInt().coerceAtMost(scrollState.maxValue))
                    }
                }
            } else {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }
    }

    val horizontalPadding = if (isLandscape) 16.dp else 24.dp
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

    val showCursor = !isResultCalculated
    val formattedFinalResult = remember(finalResult, numberingSystem) {
        NumberFormatter.formatNumber(finalResult, numberingSystem)
    }
    val formattedPreviewResult = remember(previewResult, numberingSystem) {
        NumberFormatter.formatNumber(previewResult, numberingSystem)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        horizontalAlignment = Alignment.End,
        verticalArrangement = if (isLandscape) Arrangement.Center else Arrangement.Bottom
    ) {
        // Main Expression Text with Interactive Draggable Cursor & Tap Placement
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val minWidth = maxWidth
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(min = minWidth)
                        .padding(bottom = 16.dp, top = 4.dp)
                        .pointerInput(showCursor) {
                            if (showCursor) {
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    updateCursorFromOffset(down.position)
                                    down.consume()

                                    var isDragging = false
                                    val pointerId = down.id

                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.find { it.id == pointerId } ?: break
                                        if (change.pressed) {
                                            val dist = (change.position - down.position).getDistance()
                                            if (dist > viewConfig.touchSlop || isDragging) {
                                                if (!isDragging) {
                                                    isDragging = true
                                                    isDraggingCursor = true
                                                }
                                                change.consume()
                                                updateCursorFromOffset(change.position)
                                            }
                                        } else {
                                            break
                                        }
                                    }
                                    isDraggingCursor = false
                                }
                            }
                        }
                        .drawWithContent {
                            drawContent()
                            if (showCursor) {
                                val clamped = if (isPlaceholder) 1 else formattedCursorIndex.coerceIn(0, displayText.length)
                                val rect = textLayoutResult?.getCursorRect(clamped)
                                if (rect != null) {
                                    val effectiveAlpha = if (isDraggingCursor) 1f else cursorAlpha
                                    val cursorWidth = if (isDraggingCursor) 3.dp.toPx() else 2.5.dp.toPx()
                                    val verticalInset = 2.dp.toPx()
                                    val cursorHeight = (rect.bottom - rect.top - verticalInset * 2).coerceAtLeast(12f)
                                    val cursorTop = rect.top + verticalInset

                                    // Vertical cursor line
                                    drawRoundRect(
                                        color = colors.equalsKeyBg.copy(alpha = effectiveAlpha),
                                        topLeft = Offset(rect.left - cursorWidth / 2, cursorTop),
                                        size = Size(cursorWidth, cursorHeight),
                                        cornerRadius = CornerRadius(cursorWidth / 2, cursorWidth / 2)
                                    )

                                    // Draggable teardrop/droplet handle at bottom of cursor line
                                    val handleRadius = if (isDraggingCursor) 8.dp.toPx() else 6.dp.toPx()
                                    val handleCenter = Offset(rect.left, rect.bottom + handleRadius - 1.dp.toPx())

                                    if (isDraggingCursor) {
                                        // Subtle glowing halo when actively dragging
                                        drawCircle(
                                            color = colors.equalsKeyBg.copy(alpha = 0.25f),
                                            radius = handleRadius + 6.dp.toPx(),
                                            center = handleCenter
                                        )
                                    }

                                    // Solid handle circle
                                    drawCircle(
                                        color = colors.equalsKeyBg.copy(alpha = effectiveAlpha),
                                        radius = handleRadius,
                                        center = handleCenter
                                    )
                                }
                            }
                        }
                ) {
                    Text(
                        text = displayText,
                        color = when {
                            isResultCalculated -> colors.textSecondary
                            isPlaceholder -> colors.textSecondary.copy(alpha = 0.5f)
                            else -> colors.textPrimary
                        },
                        fontSize = exprFontSize,
                        fontWeight = if (isResultCalculated) FontWeight.Normal else FontWeight.Medium,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        modifier = Modifier.widthIn(min = minWidth),
                        onTextLayout = { textLayoutResult = it }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(if (isLandscape) 4.dp else 8.dp))

        // Result or Live Preview or Error
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
                        overflow = TextOverflow.Ellipsis
                    )
                }
                formattedPreviewResult.isNotEmpty() -> {
                    PreviewResultText(formattedPreviewResult, colors, previewFontSize)
                }
            }
        }
    }
}

@Composable
private fun PreviewResultText(
    previewResult: String,
    colors: com.calculator.app.ui.theme.CalculatorColors,
    fontSize: androidx.compose.ui.unit.TextUnit = 28.sp
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
            overflow = TextOverflow.Ellipsis
        )
    }
}
