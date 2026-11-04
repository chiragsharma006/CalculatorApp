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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
    isLandscape: Boolean = false,
    onCut: (() -> Unit)? = null,
    onCopy: (() -> Unit)? = null,
    onPaste: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val colors = LocalCalculatorColors.current
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val viewConfig = LocalViewConfiguration.current

    val scrollState = rememberScrollState()
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var isDraggingCursor by remember { mutableStateOf(false) }
    var showActionPopup by remember { mutableStateOf(false) }
    var actionPopupOffset by remember { mutableStateOf(Offset.Zero) }
    var magnifierAnchorOffset by remember { mutableStateOf(Offset.Zero) }

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

    // Format expression and map cursor
    val formattedExpression = remember(expression, numberingSystem) {
        NumberFormatter.formatExpression(expression, numberingSystem)
    }
    val displayText = if (formattedExpression.formattedText.isEmpty()) "0" else formattedExpression.formattedText
    val isPlaceholder = formattedExpression.formattedText.isEmpty()
    val formattedCursorIndex = remember(expression, cursorIndex, numberingSystem) {
        NumberFormatter.rawToFormattedIndex(cursorIndex, formattedExpression)
    }

    val formattedFinalResult = remember(finalResult, numberingSystem) {
        NumberFormatter.formatNumber(finalResult, numberingSystem)
    }
    val formattedPreviewResult = remember(previewResult, numberingSystem) {
        NumberFormatter.formatNumber(previewResult, numberingSystem)
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

    val currentExpression by rememberUpdatedState(expression)
    val currentCursorIndex by rememberUpdatedState(cursorIndex)
    val currentOnCursorChange by rememberUpdatedState(onCursorChange)
    val currentFormattedExpression by rememberUpdatedState(formattedExpression)
    val currentFormattedCursorIndex by rememberUpdatedState(formattedCursorIndex)
    val currentDisplayText by rememberUpdatedState(displayText)
    val currentTextLayoutResult by rememberUpdatedState(textLayoutResult)
    val currentHaptic by rememberUpdatedState(haptic)
    val currentIsPlaceholder by rememberUpdatedState(isPlaceholder)

    val showCursor = !isResultCalculated

    // Insets to ensure the cursor dot handle is NEVER clipped in any direction
    val startInset = 20.dp
    val endInset = 20.dp
    val topInset = 6.dp
    val bottomInset = 26.dp
    val startInsetPx = with(density) { startInset.toPx() }
    val endInsetPx = with(density) { endInset.toPx() }
    val topInsetPx = with(density) { topInset.toPx() }

    fun updateCursorFromOffset(offset: Offset) {
        if (currentIsPlaceholder || currentExpression.isEmpty()) {
            currentOnCursorChange(0)
            return
        }
        val layout = currentTextLayoutResult ?: return
        val centerY = (layout.size.height / 2f).coerceAtLeast(1f)
        val localX = offset.x - startInsetPx
        val charIndex = layout.getOffsetForPosition(Offset(localX, centerY))
        val rawIndex = NumberFormatter.formattedToRawIndex(charIndex, currentFormattedExpression)
            .coerceIn(0, currentExpression.length)
        if (rawIndex != currentCursorIndex) {
            currentHaptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            currentOnCursorChange(rawIndex)
        }
    }

    // Auto-scroll when cursor position moves beyond viewport
    LaunchedEffect(expression, cursorIndex, numberingSystem) {
        val layout = textLayoutResult ?: return@LaunchedEffect
        if (displayText.isEmpty()) return@LaunchedEffect
        val clamped = if (isPlaceholder) 0 else formattedCursorIndex.coerceIn(0, displayText.length)
        val rect = layout.getCursorRectSafe(clamped) ?: return@LaunchedEffect

        if (scrollState.maxValue > 0) {
            val cursorXInRow = rect.left + startInsetPx
            val viewport = scrollState.viewportSize
            val currentScroll = scrollState.value
            val leftMargin = 50
            val rightMargin = 50

            if (cursorXInRow < currentScroll + leftMargin) {
                scrollState.animateScrollTo((cursorXInRow - leftMargin).toInt().coerceAtLeast(0))
            } else if (cursorXInRow > currentScroll + viewport - rightMargin) {
                scrollState.animateScrollTo((cursorXInRow - viewport + rightMargin).toInt().coerceAtMost(scrollState.maxValue))
            }
        } else {
            if (scrollState.value != 0) {
                scrollState.scrollTo(0)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        horizontalAlignment = Alignment.End,
        verticalArrangement = if (isLandscape) Arrangement.Center else Arrangement.Bottom
    ) {
        // Main Expression Display Area
        if (!isResultCalculated) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth()
            ) {
                val minWidth = maxWidth
                val clampedIndex = if (isPlaceholder) 0 else formattedCursorIndex.coerceIn(0, displayText.length)
                val curRect = textLayoutResult.getCursorRectSafe(clampedIndex)
                val cursorScreenX = (curRect?.left ?: 0f) + startInsetPx - scrollState.value
                val cursorScreenY = (curRect?.top ?: 0f) + topInsetPx

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(min = minWidth)
                            .pointerInput(showCursor, displayText) {
                                if (!showCursor) return@pointerInput
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    val layout = currentTextLayoutResult
                                    val clamped = if (currentIsPlaceholder) 0 else currentFormattedCursorIndex.coerceIn(0, currentDisplayText.length)
                                    val currentRect = layout.getCursorRectSafe(clamped)

                                    val handleRadius = 6.5.dp.toPx()
                                    val cursorHeight = if (currentRect != null) {
                                        (currentRect.bottom - currentRect.top - 2.dp.toPx()).coerceAtLeast(14f)
                                    } else 14f
                                    val cursorTop = (currentRect?.top ?: 0f) + 1.dp.toPx() + topInsetPx
                                    val cursorBottom = cursorTop + cursorHeight
                                    val cursorX = (currentRect?.left ?: 0f) + startInsetPx

                                    // The dot is centered EXACTLY on the cursor line: (cursorX, cursorBottom + handleRadius)
                                    val handleCenter = if (currentRect != null) {
                                        Offset(cursorX, cursorBottom + handleRadius)
                                    } else null

                                    // Generous touch target around the dot handle (28dp radius)
                                    val hitRadius = 28.dp.toPx()
                                    val isHandleHit = handleCenter != null && (down.position - handleCenter).getDistance() <= hitRadius

                                    if (isHandleHit) {
                                        // User touched the dot handle: Consume touch and drag cursor
                                        down.consume()
                                        var hasDragged = false
                                        val pointerId = down.id
                                        magnifierAnchorOffset = Offset(
                                            (currentRect?.left ?: 0f) + startInsetPx - scrollState.value,
                                            cursorScreenY
                                        )

                                        while (true) {
                                            val event = awaitPointerEvent()
                                            val change = event.changes.find { it.id == pointerId } ?: break
                                            if (change.pressed) {
                                                val dist = (change.position - down.position).getDistance()
                                                if (dist > viewConfig.touchSlop) {
                                                    if (!hasDragged) {
                                                        hasDragged = true
                                                        isDraggingCursor = true
                                                        currentHaptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    }
                                                    change.consume()
                                                    magnifierAnchorOffset = Offset(
                                                        change.position.x - scrollState.value,
                                                        cursorScreenY
                                                    )
                                                    updateCursorFromOffset(change.position)
                                                }
                                            } else {
                                                break
                                            }
                                        }
                                        isDraggingCursor = false

                                        // If tapped the handle without dragging, show Cut / Copy / Paste
                                        if (!hasDragged) {
                                            val canCutOrCopy = !currentIsPlaceholder && currentExpression.isNotEmpty()
                                            val hasClip = ClipboardHelper.hasClipboardText(context)
                                            if (canCutOrCopy || hasClip) {
                                                currentHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                actionPopupOffset = handleCenter ?: down.position
                                                showActionPopup = true
                                            }
                                        }
                                    } else {
                                        // User touched outside the dot handle: Allow smooth horizontal scrolling!
                                        val pointerId = down.id
                                        var moved = false
                                        var isLongPress = false
                                        val longPressTimeout = viewConfig.longPressTimeoutMillis
                                        val startTime = System.currentTimeMillis()

                                        while (true) {
                                            val event = awaitPointerEvent()
                                            val change = event.changes.find { it.id == pointerId } ?: break
                                            if (change.pressed) {
                                                val dist = (change.position - down.position).getDistance()
                                                if (dist > viewConfig.touchSlop || change.isConsumed) {
                                                    moved = true
                                                    showActionPopup = false
                                                } else if (!moved && !isLongPress && (System.currentTimeMillis() - startTime >= longPressTimeout)) {
                                                    isLongPress = true
                                                    currentHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    updateCursorFromOffset(down.position)
                                                    val canCutOrCopy = !currentIsPlaceholder && currentExpression.isNotEmpty()
                                                    val hasClip = ClipboardHelper.hasClipboardText(context)
                                                    if (canCutOrCopy || hasClip) {
                                                        actionPopupOffset = down.position
                                                        showActionPopup = true
                                                    }
                                                }
                                            } else {
                                                // Tap without dragging or long press: Place cursor
                                                if (!moved && !isLongPress && !change.isConsumed) {
                                                    updateCursorFromOffset(down.position)
                                                    showActionPopup = false
                                                }
                                                break
                                            }
                                        }
                                    }
                                }
                            }
                            .drawWithContent {
                                drawContent()
                                if (showCursor) {
                                    val currentClamped = if (isPlaceholder) 0 else formattedCursorIndex.coerceIn(0, displayText.length)
                                    val cursorRect = textLayoutResult.getCursorRectSafe(currentClamped)
                                    if (cursorRect != null) {
                                        val effectiveAlpha = if (isDraggingCursor) 1f else cursorAlpha
                                        val cursorWidth = 2.5.dp.toPx()
                                        val verticalInset = 1.dp.toPx()
                                        val cursorHeight = (cursorRect.bottom - cursorRect.top - verticalInset * 2).coerceAtLeast(14f)
                                        val cursorTop = cursorRect.top + verticalInset + topInsetPx
                                        val cursorBottom = cursorTop + cursorHeight
                                        val cursorX = cursorRect.left + startInsetPx

                                        // Vertical cursor bar centered at cursorX
                                        drawRoundRect(
                                            color = colors.equalsKeyBg.copy(alpha = effectiveAlpha),
                                            topLeft = Offset(cursorX - cursorWidth / 2f, cursorTop),
                                            size = Size(cursorWidth, cursorHeight),
                                            cornerRadius = CornerRadius(cursorWidth / 2f, cursorWidth / 2f)
                                        )

                                        // Symmetrical circular dot handle centered EXACTLY on the same cursorX vertical line!
                                        val handleRadius = 6.5.dp.toPx()
                                        val handleCenter = Offset(cursorX, cursorBottom + handleRadius)

                                        // Subtle halo feedback when dragging
                                        if (isDraggingCursor) {
                                            drawCircle(
                                                color = colors.equalsKeyBg.copy(alpha = 0.25f),
                                                radius = handleRadius + 6.dp.toPx(),
                                                center = handleCenter
                                            )
                                        }

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
                                isPlaceholder -> colors.textSecondary.copy(alpha = 0.5f)
                                else -> colors.textPrimary
                            },
                            fontSize = exprFontSize,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            modifier = Modifier
                                .padding(start = startInset, end = endInset, top = topInset, bottom = bottomInset)
                                .widthIn(min = (minWidth - startInset - endInset).coerceAtLeast(0.dp))
                                .align(Alignment.CenterEnd),
                            onTextLayout = { textLayoutResult = it }
                        )
                    }
                }

                // Magnifier Loupe Bubble floating directly above cursor while dragging
                val activeAnchor = if (isDraggingCursor) {
                    magnifierAnchorOffset
                } else {
                    Offset(cursorScreenX, cursorScreenY)
                }

                MagnifierPopup(
                    visible = isDraggingCursor && showCursor,
                    displayText = displayText,
                    cursorIndex = clampedIndex,
                    anchorOffset = activeAnchor,
                    cursorColor = colors.equalsKeyBg
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
                                onLongPress = { offset ->
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    actionPopupOffset = offset
                                    showActionPopup = true
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

        // Floating Cut / Copy / Paste popup above cursor or touch point
        val canCopyOrCut = if (isResultCalculated) {
            finalResult.isNotEmpty()
        } else {
            !isPlaceholder && expression.isNotEmpty()
        }
        val hasClipboard = remember(showActionPopup) {
            if (showActionPopup) ClipboardHelper.hasClipboardText(context) else false
        }

        CutCopyPastePopup(
            visible = showActionPopup,
            onDismissRequest = { showActionPopup = false },
            anchorOffset = actionPopupOffset,
            onCut = if (canCopyOrCut) {
                {
                    val textToCut = if (isResultCalculated) finalResult else expression
                    ClipboardHelper.cut(context, textToCut)
                    onCut?.invoke()
                    showActionPopup = false
                }
            } else null,
            onCopy = if (canCopyOrCut) {
                {
                    val textToCopy = if (isResultCalculated) finalResult else expression
                    ClipboardHelper.copy(context, textToCopy)
                    onCopy?.invoke()
                    showActionPopup = false
                }
            } else null,
            onPaste = if (hasClipboard) {
                {
                    val text = ClipboardHelper.getClipboardText(context)
                    if (text != null) {
                        onPaste?.invoke(text)
                    }
                    showActionPopup = false
                }
            } else null
        )
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
