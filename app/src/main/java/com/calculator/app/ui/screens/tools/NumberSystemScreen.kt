package com.calculator.app.ui.screens.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calculator.app.data.preferences.LocalNumberingSystem
import com.calculator.app.domain.format.NumberFormatter
import com.calculator.app.domain.tools.NumberBase
import com.calculator.app.domain.tools.NumberSystemEngine
import com.calculator.app.ui.components.MagnifierPopup
import com.calculator.app.ui.components.getCursorRectSafe
import com.calculator.app.ui.theme.LocalCalculatorColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberSystemScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val numberingSystem = LocalNumberingSystem.current

    var selectedBase by remember { mutableStateOf(NumberBase.DECIMAL) }
    var inputString by remember { mutableStateOf("0") }
    var cursorIndex by remember(selectedBase) { mutableIntStateOf(inputString.length) }

    val result = remember(inputString, selectedBase) {
        NumberSystemEngine.convertAll(inputString, selectedBase)
    }

    fun handleKey(key: String) {
        if (inputString == "0") {
            inputString = key
            cursorIndex = 1
        } else if (inputString.length < 24) {
            val clamped = cursorIndex.coerceIn(0, inputString.length)
            inputString = inputString.substring(0, clamped) + key + inputString.substring(clamped)
            cursorIndex = clamped + 1
        }
    }

    fun handleClear() {
        inputString = "0"
        cursorIndex = 1
    }

    fun handleBackspace() {
        val clamped = cursorIndex.coerceIn(0, inputString.length)
        if (clamped > 0 && inputString.isNotEmpty()) {
            val newStr = inputString.removeRange(clamped - 1, clamped)
            inputString = if (newStr.isEmpty()) "0" else newStr
            cursorIndex = (clamped - 1).coerceAtLeast(if (inputString == "0") 1 else 0)
        }
    }

    fun selectBase(newBase: NumberBase) {
        if (selectedBase != newBase) {
            val converted = when (newBase) {
                NumberBase.DECIMAL -> result.decimal
                NumberBase.HEXADECIMAL -> result.hexadecimal
                NumberBase.OCTAL -> result.octal
                NumberBase.BINARY -> result.binary.replace(" ", "")
            }
            if (converted != "Invalid") {
                inputString = converted
                cursorIndex = converted.length
            }
            selectedBase = newBase
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Number System",
                        fontSize = if (isLandscape) 18.sp else 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        }
    ) { innerPadding ->
        if (isLandscape) {
            // LANDSCAPE: Side-by-side (Cards on Left, Keypad on Right)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left Column: 4 Cards
                Column(
                    modifier = Modifier
                        .weight(0.46f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BaseDisplayCard(
                        base = NumberBase.HEXADECIMAL,
                        value = if (selectedBase == NumberBase.HEXADECIMAL) inputString else result.hexadecimal,
                        isSelected = selectedBase == NumberBase.HEXADECIMAL,
                        onSelect = { selectBase(NumberBase.HEXADECIMAL) },
                        onCopy = { copyToClipboard(context, result.hexadecimal, "HEX") },
                        cursorIndex = cursorIndex,
                        onCursorChange = { cursorIndex = it }
                    )
                    BaseDisplayCard(
                        base = NumberBase.DECIMAL,
                        value = if (selectedBase == NumberBase.DECIMAL) inputString else result.decimal,
                        isSelected = selectedBase == NumberBase.DECIMAL,
                        onSelect = { selectBase(NumberBase.DECIMAL) },
                        onCopy = { copyToClipboard(context, result.decimal, "DEC") },
                        cursorIndex = cursorIndex,
                        onCursorChange = { cursorIndex = it }
                    )
                    BaseDisplayCard(
                        base = NumberBase.OCTAL,
                        value = if (selectedBase == NumberBase.OCTAL) inputString else result.octal,
                        isSelected = selectedBase == NumberBase.OCTAL,
                        onSelect = { selectBase(NumberBase.OCTAL) },
                        onCopy = { copyToClipboard(context, result.octal, "OCT") },
                        cursorIndex = cursorIndex,
                        onCursorChange = { cursorIndex = it }
                    )
                    BaseDisplayCard(
                        base = NumberBase.BINARY,
                        value = if (selectedBase == NumberBase.BINARY) inputString else result.binary,
                        isSelected = selectedBase == NumberBase.BINARY,
                        onSelect = { selectBase(NumberBase.BINARY) },
                        onCopy = { copyToClipboard(context, result.binary, "BIN") },
                        cursorIndex = cursorIndex,
                        onCursorChange = { cursorIndex = it }
                    )
                }

                // Vertical Divider
                VerticalDivider(
                    color = colors.textSecondary.copy(alpha = 0.15f),
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxHeight()
                )

                // Right Column: Programmer Keypad
                Box(
                    modifier = Modifier
                        .weight(0.54f)
                        .fillMaxHeight()
                ) {
                    ProgrammerKeypad(
                        selectedBase = selectedBase,
                        onKeyClick = ::handleKey,
                        onClear = ::handleClear,
                        onBackspace = ::handleBackspace,
                        keyHeight = 36.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        } else {
            // PORTRAIT: Cards on top, Keypad on bottom
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Cards displaying DEC, HEX, OCT, BIN simultaneously
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BaseDisplayCard(
                        base = NumberBase.HEXADECIMAL,
                        value = if (selectedBase == NumberBase.HEXADECIMAL) inputString else result.hexadecimal,
                        isSelected = selectedBase == NumberBase.HEXADECIMAL,
                        onSelect = { selectBase(NumberBase.HEXADECIMAL) },
                        onCopy = { copyToClipboard(context, result.hexadecimal, "HEX") },
                        cursorIndex = cursorIndex,
                        onCursorChange = { cursorIndex = it }
                    )
                    BaseDisplayCard(
                        base = NumberBase.DECIMAL,
                        value = if (selectedBase == NumberBase.DECIMAL) inputString else result.decimal,
                        isSelected = selectedBase == NumberBase.DECIMAL,
                        onSelect = { selectBase(NumberBase.DECIMAL) },
                        onCopy = { copyToClipboard(context, result.decimal, "DEC") },
                        cursorIndex = cursorIndex,
                        onCursorChange = { cursorIndex = it }
                    )
                    BaseDisplayCard(
                        base = NumberBase.OCTAL,
                        value = if (selectedBase == NumberBase.OCTAL) inputString else result.octal,
                        isSelected = selectedBase == NumberBase.OCTAL,
                        onSelect = { selectBase(NumberBase.OCTAL) },
                        onCopy = { copyToClipboard(context, result.octal, "OCT") },
                        cursorIndex = cursorIndex,
                        onCursorChange = { cursorIndex = it }
                    )
                    BaseDisplayCard(
                        base = NumberBase.BINARY,
                        value = if (selectedBase == NumberBase.BINARY) inputString else result.binary,
                        isSelected = selectedBase == NumberBase.BINARY,
                        onSelect = { selectBase(NumberBase.BINARY) },
                        onCopy = { copyToClipboard(context, result.binary, "BIN") },
                        cursorIndex = cursorIndex,
                        onCursorChange = { cursorIndex = it }
                    )
                }

                // On-screen programmer keypad
                ProgrammerKeypad(
                    selectedBase = selectedBase,
                    onKeyClick = ::handleKey,
                    onClear = ::handleClear,
                    onBackspace = ::handleBackspace,
                    keyHeight = 48.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String, label: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "Copied $label: $text", Toast.LENGTH_SHORT).show()
}

@Composable
private fun BaseDisplayCard(
    base: NumberBase,
    value: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onCopy: () -> Unit,
    cursorIndex: Int = 0,
    onCursorChange: ((Int) -> Unit)? = null
) {
    val colors = LocalCalculatorColors.current
    val numberingSystem = LocalNumberingSystem.current
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val viewConfig = LocalViewConfiguration.current

    val horizontalInset = 10.dp
    val horizontalInsetPx = with(density) { horizontalInset.toPx() }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var isDraggingCursor by remember { mutableStateOf(false) }

    val formattedRes = remember(value, numberingSystem) {
        if (base == NumberBase.DECIMAL) {
            NumberFormatter.formatExpression(value, numberingSystem)
        } else {
            null
        }
    }
    val displayText = when {
        value.isEmpty() -> "0"
        formattedRes != null -> formattedRes.formattedText
        else -> value
    }
    val formattedCursorIndex = remember(value, cursorIndex, numberingSystem) {
        if (formattedRes != null) {
            NumberFormatter.rawToFormattedIndex(cursorIndex, formattedRes)
        } else {
            cursorIndex.coerceIn(0, displayText.length)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "baseCursorTransition")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 530, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "baseCursorAlpha"
    )

    val currentBaseValue by rememberUpdatedState(value)
    val currentCursorIndex by rememberUpdatedState(cursorIndex)
    val currentOnCursorChange by rememberUpdatedState(onCursorChange)
    val currentFormattedRes by rememberUpdatedState(formattedRes)
    val currentFormattedCursorIndex by rememberUpdatedState(formattedCursorIndex)
    val currentDisplayText by rememberUpdatedState(displayText)
    val currentTextLayoutResult by rememberUpdatedState(textLayoutResult)
    val currentHaptic by rememberUpdatedState(haptic)

    fun updateCursor(offset: Offset) {
        if (currentBaseValue.isEmpty()) {
            currentOnCursorChange?.invoke(0)
            return
        }
        val layout = currentTextLayoutResult ?: return
        val centerY = (layout.size.height / 2f).coerceAtLeast(1f)
        val charIndex = layout.getOffsetForPosition(Offset(offset.x, centerY))
        val res = currentFormattedRes
        val rawIndex = if (res != null) {
            NumberFormatter.formattedToRawIndex(charIndex, res).coerceIn(0, currentBaseValue.length)
        } else {
            charIndex.coerceIn(0, currentBaseValue.length)
        }
        if (rawIndex != currentCursorIndex) {
            currentHaptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            currentOnCursorChange?.invoke(rawIndex)
        }
    }

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) colors.operatorKeyBg else colors.surface,
        animationSpec = tween(200),
        label = "baseCardBg"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val containerWidthPx = with(density) { maxWidth.toPx() }
        val clamped = formattedCursorIndex.coerceIn(0, displayText.length)
        val rect = if (isDraggingCursor) textLayoutResult.getCursorRectSafe(clamped) else null
        val cursorScreenX = (rect?.left ?: 0f) + horizontalInsetPx + with(density) { 12.dp.toPx() }

        Surface(
            color = bgColor,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(
                if (isSelected) 1.5.dp else 1.dp,
                if (isSelected) colors.operatorKeyText else colors.textSecondary.copy(alpha = 0.12f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onSelect)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                if (isSelected && onCursorChange != null) {
                                    onCursorChange(value.length)
                                } else {
                                    onSelect()
                                }
                            }
                        )
                ) {
                    Text(
                        text = base.displayName,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) colors.operatorKeyText else colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalInset)
                            .padding(bottom = if (isSelected) 14.dp else 2.dp, top = 2.dp)
                            .pointerInput(isSelected) {
                                if (isSelected) {
                                    awaitEachGesture {
                                        val down = awaitFirstDown(requireUnconsumed = false)
                                        val layout = currentTextLayoutResult
                                        val currentClamped = currentFormattedCursorIndex.coerceIn(0, currentDisplayText.length)
                                        val currentRect = layout.getCursorRectSafe(currentClamped)

                                        val handleRadius = 7.5.dp.toPx()
                                        val handleCenter = if (currentRect != null) {
                                            Offset(currentRect.left, currentRect.bottom + handleRadius - 1.dp.toPx())
                                        } else null

                                        val hitRadius = 26.dp.toPx()
                                        val isHandleHit = handleCenter != null && (down.position - handleCenter).getDistance() <= hitRadius

                                        if (isHandleHit) {
                                            down.consume()
                                            isDraggingCursor = true
                                            currentHaptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                                            val pointerId = down.id
                                            while (true) {
                                                val event = awaitPointerEvent()
                                                val change = event.changes.find { it.id == pointerId } ?: break
                                                if (change.pressed) {
                                                    change.consume()
                                                    updateCursor(change.position)
                                                } else {
                                                    break
                                                }
                                            }
                                            isDraggingCursor = false
                                        } else {
                                            val pointerId = down.id
                                            var moved = false
                                            while (true) {
                                                val event = awaitPointerEvent()
                                                val change = event.changes.find { it.id == pointerId } ?: break
                                                if (change.pressed) {
                                                    val dist = (change.position - down.position).getDistance()
                                                    if (dist > viewConfig.touchSlop || change.isConsumed) {
                                                        moved = true
                                                    }
                                                } else {
                                                    if (!moved && !change.isConsumed) {
                                                        updateCursor(down.position)
                                                    }
                                                    break
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            .drawWithContent {
                                drawContent()
                                if (isSelected) {
                                    val currentClamped = formattedCursorIndex.coerceIn(0, displayText.length)
                                    val cursorRect = textLayoutResult.getCursorRectSafe(currentClamped)
                                    if (cursorRect != null) {
                                        val effectiveAlpha = if (isDraggingCursor) 1f else cursorAlpha
                                        val cursorWidth = if (isDraggingCursor) 3.dp.toPx() else 2.5.dp.toPx()
                                        val verticalInset = 1.dp.toPx()
                                        val cursorHeight = (cursorRect.bottom - cursorRect.top - verticalInset * 2).coerceAtLeast(10f)
                                        val cursorTop = cursorRect.top + verticalInset

                                        // Vertical cursor bar
                                        drawRoundRect(
                                            color = colors.operatorKeyText.copy(alpha = effectiveAlpha),
                                            topLeft = Offset(cursorRect.left - cursorWidth / 2, cursorTop),
                                            size = Size(cursorWidth, cursorHeight),
                                            cornerRadius = CornerRadius(cursorWidth / 2, cursorWidth / 2)
                                        )

                                        // Draggable teardrop/droplet handle at bottom of cursor line
                                        val handleRadius = if (isDraggingCursor) 7.5.dp.toPx() else 5.dp.toPx()
                                        val handleCenter = Offset(cursorRect.left, cursorRect.bottom + handleRadius - 1.dp.toPx())

                                        if (isDraggingCursor) {
                                            drawCircle(
                                                color = colors.operatorKeyText.copy(alpha = 0.25f),
                                                radius = handleRadius + 6.dp.toPx(),
                                                center = handleCenter
                                            )
                                        }

                                        drawCircle(
                                            color = colors.operatorKeyText.copy(alpha = effectiveAlpha),
                                            radius = handleRadius,
                                            center = handleCenter
                                        )
                                    }
                                }
                            }
                    ) {
                        Text(
                            text = displayText,
                            fontSize = 17.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth(),
                            onTextLayout = { textLayoutResult = it }
                        )
                    }
                }

                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy ${base.name}",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Magnifier loupe bubble floating directly above cursor while dragging
        MagnifierPopup(
            visible = isDraggingCursor && isSelected,
            displayText = displayText,
            cursorIndex = clamped,
            cursorX = cursorScreenX,
            containerWidth = containerWidthPx,
            cursorColor = colors.operatorKeyText,
            yOffset = (-36).dp
        )
    }
}

@Composable
private fun ProgrammerKeypad(
    selectedBase: NumberBase,
    onKeyClick: (String) -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    keyHeight: androidx.compose.ui.unit.Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Row 1: A, B, C, D
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("A", "B", "C", "D").forEach { key ->
                val enabled = NumberSystemEngine.isValidChar(key[0], selectedBase)
                KeypadKey(
                    text = key,
                    enabled = enabled,
                    onClick = { onKeyClick(key) },
                    keyHeight = keyHeight,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 2: E, F, AC, DEL
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val enabledE = NumberSystemEngine.isValidChar('E', selectedBase)
            KeypadKey(text = "E", enabled = enabledE, onClick = { onKeyClick("E") }, keyHeight = keyHeight, modifier = Modifier.weight(1f))

            val enabledF = NumberSystemEngine.isValidChar('F', selectedBase)
            KeypadKey(text = "F", enabled = enabledF, onClick = { onKeyClick("F") }, keyHeight = keyHeight, modifier = Modifier.weight(1f))

            KeypadKey(
                text = "AC",
                enabled = true,
                onClick = onClear,
                isClear = true,
                keyHeight = keyHeight,
                modifier = Modifier.weight(1f)
            )

            KeypadKey(
                text = "",
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                enabled = true,
                onClick = onBackspace,
                keyHeight = keyHeight,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: 7, 8, 9
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("7", "8", "9").forEach { key ->
                val enabled = NumberSystemEngine.isValidChar(key[0], selectedBase)
                KeypadKey(
                    text = key,
                    enabled = enabled,
                    onClick = { onKeyClick(key) },
                    keyHeight = keyHeight,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 4: 4, 5, 6
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("4", "5", "6").forEach { key ->
                val enabled = NumberSystemEngine.isValidChar(key[0], selectedBase)
                KeypadKey(
                    text = key,
                    enabled = enabled,
                    onClick = { onKeyClick(key) },
                    keyHeight = keyHeight,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 5: 1, 2, 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("1", "2", "3").forEach { key ->
                val enabled = NumberSystemEngine.isValidChar(key[0], selectedBase)
                KeypadKey(
                    text = key,
                    enabled = enabled,
                    onClick = { onKeyClick(key) },
                    keyHeight = keyHeight,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 6: 0
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KeypadKey(
                text = "0",
                enabled = true,
                onClick = { onKeyClick("0") },
                keyHeight = keyHeight,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun KeypadKey(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isClear: Boolean = false,
    keyHeight: androidx.compose.ui.unit.Dp = 48.dp,
    icon: (@Composable () -> Unit)? = null
) {
    val colors = LocalCalculatorColors.current

    val bgColor = when {
        !enabled -> colors.functionKeyBg.copy(alpha = 0.35f)
        isClear -> colors.clearKeyBg
        else -> colors.numberKeyBg
    }

    val contentColor = when {
        !enabled -> colors.textSecondary.copy(alpha = 0.25f)
        isClear -> colors.clearKeyText
        else -> colors.numberKeyText
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .height(keyHeight)
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null && enabled) {
                icon()
            } else {
                Text(
                    text = text,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            }
        }
    }
}
