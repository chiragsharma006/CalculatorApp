package com.calculator.app.ui.screens.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
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
import com.calculator.app.data.preferences.LocalNumberingSystem
import com.calculator.app.data.repository.CurrencyRateRepository
import com.calculator.app.data.repository.CurrencyRateState
import com.calculator.app.domain.format.NumberFormatter
import com.calculator.app.domain.tools.UnitCategory
import com.calculator.app.domain.tools.UnitConverterEngine
import com.calculator.app.domain.tools.UnitItem
import com.calculator.app.ui.components.ClipboardHelper
import com.calculator.app.ui.components.CutCopyPastePopup
import com.calculator.app.ui.components.cursorMagnifier
import com.calculator.app.ui.components.getCursorRectSafe
import com.calculator.app.ui.theme.LocalCalculatorColors
import kotlinx.coroutines.launch

enum class ActiveUnitField {
    FROM,
    TO
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    initialCategory: UnitCategory = UnitCategory.CURRENCY,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val coroutineScope = rememberCoroutineScope()

    val currencyRepo = remember { CurrencyRateRepository(context) }
    var currencyState by remember { mutableStateOf(currencyRepo.getCachedState()) }

    var selectedCategory by remember { mutableStateOf(initialCategory) }

    // Fetch current conversion rates from internet whenever Currency category is opened or clicked
    LaunchedEffect(selectedCategory) {
        if (selectedCategory == UnitCategory.CURRENCY) {
            currencyState = currencyState.copy(isLoading = true)
            currencyState = currencyRepo.getRates(forceRefresh = false)
        }
    }

    val units = remember(selectedCategory, currencyState.rates) {
        UnitConverterEngine.getUnitsForCategory(
            category = selectedCategory,
            customCurrencyRates = if (selectedCategory == UnitCategory.CURRENCY) currencyState.rates else null
        )
    }

    var fromUnit by remember(selectedCategory) { mutableStateOf(units.firstOrNull() ?: UnitItem("", "", "")) }
    var toUnit by remember(selectedCategory) { mutableStateOf(if (units.size > 1) units[1] else units.firstOrNull() ?: UnitItem("", "", "")) }

    // Sync fromUnit and toUnit ratios when live currency rates arrive
    LaunchedEffect(units) {
        fromUnit = units.find { it.id == fromUnit.id } ?: units.firstOrNull() ?: UnitItem("", "", "")
        toUnit = units.find { it.id == toUnit.id } ?: (if (units.size > 1) units[1] else units.firstOrNull() ?: UnitItem("", "", ""))
    }

    // Default active field with cursor is the FROM section as requested
    var activeTarget by remember(selectedCategory) { mutableStateOf(ActiveUnitField.FROM) }

    // Swap animation trigger counter
    var swapAnimationCount by remember { mutableIntStateOf(0) }

    var inputValue by remember { mutableStateOf("1") }
    var inputCursorIndex by remember(activeTarget) { mutableIntStateOf(1) }
    val numericInput = inputValue.toDoubleOrNull() ?: 0.0

    fun handleDigit(digit: String) {
        if (inputValue == "0") {
            inputValue = digit
            inputCursorIndex = 1
        } else if (inputValue.length < 15) {
            val clamped = inputCursorIndex.coerceIn(0, inputValue.length)
            inputValue = inputValue.substring(0, clamped) + digit + inputValue.substring(clamped)
            inputCursorIndex = clamped + 1
        }
    }

    fun handleDecimal() {
        if (!inputValue.contains('.')) {
            val clamped = inputCursorIndex.coerceIn(0, inputValue.length)
            inputValue = inputValue.substring(0, clamped) + "." + inputValue.substring(clamped)
            inputCursorIndex = clamped + 1
        }
    }

    fun handleDoubleZero() {
        if (inputValue != "0" && inputValue.length < 14) {
            val clamped = inputCursorIndex.coerceIn(0, inputValue.length)
            inputValue = inputValue.substring(0, clamped) + "00" + inputValue.substring(clamped)
            inputCursorIndex = clamped + 2
        }
    }

    fun handleToggleSign() {
        if (inputValue.startsWith("-")) {
            inputValue = inputValue.removePrefix("-")
            inputCursorIndex = (inputCursorIndex - 1).coerceAtLeast(0)
        } else if (inputValue != "0" && inputValue.isNotEmpty()) {
            inputValue = "-$inputValue"
            inputCursorIndex = inputCursorIndex + 1
        }
    }

    fun handleClear() {
        inputValue = "0"
        inputCursorIndex = 1
    }

    fun handleCut() {
        if (inputValue.isNotEmpty()) {
            ClipboardHelper.cut(context, inputValue)
            inputValue = "0"
            inputCursorIndex = 1
        }
    }

    fun handlePaste(rawText: String) {
        val sanitized = ClipboardHelper.sanitizeDecimalPaste(rawText)
        if (sanitized.isNotEmpty()) {
            val clamped = inputCursorIndex.coerceIn(0, inputValue.length)
            if (inputValue == "0") {
                inputValue = sanitized
                inputCursorIndex = sanitized.length
            } else {
                inputValue = inputValue.substring(0, clamped) + sanitized + inputValue.substring(clamped)
                inputCursorIndex = clamped + sanitized.length
            }
            Toast.makeText(context, "Pasted", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleBackspace() {
        val clamped = inputCursorIndex.coerceIn(0, inputValue.length)
        if (clamped > 0) {
            inputValue = inputValue.removeRange(clamped - 1, clamped)
            inputCursorIndex = clamped - 1
            if (inputValue.isEmpty() || inputValue == "-") {
                inputValue = "0"
                inputCursorIndex = 1
            }
        }
    }

    // Bidirectional conversion: whichever field is active receives the input, the other is calculated
    val fromDisplayValue: String
    val toDisplayValue: String

    if (activeTarget == ActiveUnitField.TO) {
        toDisplayValue = inputValue
        fromDisplayValue = remember(numericInput, fromUnit, toUnit, selectedCategory) {
            val res = UnitConverterEngine.convert(numericInput, from = toUnit, to = fromUnit, category = selectedCategory)
            UnitConverterEngine.formatResult(res)
        }
    } else {
        fromDisplayValue = inputValue
        toDisplayValue = remember(numericInput, fromUnit, toUnit, selectedCategory) {
            val res = UnitConverterEngine.convert(numericInput, from = fromUnit, to = toUnit, category = selectedCategory)
            UnitConverterEngine.formatResult(res)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = selectedCategory.displayName + " Converter",
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
            // LANDSCAPE: Side-by-Side (Cards on Left, On-Screen Numpad on Right)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(colors.background)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column: Category chips + Merged From/To card + Backgroundless Live Banner below
                Column(
                    modifier = Modifier
                        .weight(0.48f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CategoryChipsRow(
                        selectedCategory = selectedCategory,
                        onSelectCategory = { selectedCategory = it }
                    )

                    // Unified sleek merged From & To card with swap animation
                    MergedConversionCard(
                        units = units,
                        fromUnit = fromUnit,
                        onSelectFrom = { fromUnit = it },
                        toUnit = toUnit,
                        onSelectTo = { toUnit = it },
                        activeTarget = activeTarget,
                        onSelectActive = { activeTarget = it },
                        fromDisplayValue = fromDisplayValue,
                        toDisplayValue = toDisplayValue,
                        context = context,
                        swapTrigger = swapAnimationCount,
                        cursorIndex = inputCursorIndex,
                        onCursorChange = { inputCursorIndex = it },
                        onCut = ::handleCut,
                        onPaste = ::handlePaste
                    )

                    // Sleek single-line live currency banner directly below "To" section, with NO background
                    if (selectedCategory == UnitCategory.CURRENCY) {
                        CurrencyLiveStatusBanner(
                            currencyState = currencyState,
                            onRefresh = {
                                coroutineScope.launch {
                                    currencyState = currencyState.copy(isLoading = true)
                                    currencyState = currencyRepo.getRates(forceRefresh = true)
                                }
                            }
                        )
                    }
                }

                // Vertical Divider
                VerticalDivider(
                    color = colors.textSecondary.copy(alpha = 0.15f),
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxHeight()
                )

                // Right Column: Dedicated On-Screen Numpad with Swap Toggle in Row 4
                ConverterNumpad(
                    onDigit = ::handleDigit,
                    onDecimal = ::handleDecimal,
                    onDoubleZero = ::handleDoubleZero,
                    onToggleSign = ::handleToggleSign,
                    onClear = ::handleClear,
                    onBackspace = ::handleBackspace,
                    onSwap = {
                        swapAnimationCount++
                        val temp = fromUnit
                        fromUnit = toUnit
                        toUnit = temp
                    },
                    swapTrigger = swapAnimationCount,
                    isLandscape = true,
                    modifier = Modifier
                        .weight(0.52f)
                        .fillMaxHeight()
                )
            }
        } else {
            // PORTRAIT: Top Section (Category chips + Merged Card + Backgroundless Live Banner) + Bottom Numpad
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(colors.background)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                // Top Scrollable Section
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CategoryChipsRow(
                        selectedCategory = selectedCategory,
                        onSelectCategory = { selectedCategory = it }
                    )

                    // Unified sleek merged From & To card with swap animation
                    MergedConversionCard(
                        units = units,
                        fromUnit = fromUnit,
                        onSelectFrom = { fromUnit = it },
                        toUnit = toUnit,
                        onSelectTo = { toUnit = it },
                        activeTarget = activeTarget,
                        onSelectActive = { activeTarget = it },
                        fromDisplayValue = fromDisplayValue,
                        toDisplayValue = toDisplayValue,
                        context = context,
                        swapTrigger = swapAnimationCount,
                        cursorIndex = inputCursorIndex,
                        onCursorChange = { inputCursorIndex = it },
                        onCut = ::handleCut,
                        onPaste = ::handlePaste
                    )

                    // Sleek single-line live currency banner directly below "To" section, with NO background
                    if (selectedCategory == UnitCategory.CURRENCY) {
                        CurrencyLiveStatusBanner(
                            currencyState = currencyState,
                            onRefresh = {
                                coroutineScope.launch {
                                    currencyState = currencyState.copy(isLoading = true)
                                    currencyState = currencyRepo.getRates(forceRefresh = true)
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Bottom: Dedicated On-Screen Numpad with Swap Toggle in Row 4
                ConverterNumpad(
                    onDigit = ::handleDigit,
                    onDecimal = ::handleDecimal,
                    onDoubleZero = ::handleDoubleZero,
                    onToggleSign = ::handleToggleSign,
                    onClear = ::handleClear,
                    onBackspace = ::handleBackspace,
                    onSwap = {
                        swapAnimationCount++
                        val temp = fromUnit
                        fromUnit = toUnit
                        toUnit = temp
                    },
                    swapTrigger = swapAnimationCount,
                    isLandscape = false,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryChipsRow(
    selectedCategory: UnitCategory,
    onSelectCategory: (UnitCategory) -> Unit
) {
    val colors = LocalCalculatorColors.current

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(UnitCategory.entries) { category ->
            val isSelected = category == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onSelectCategory(category) },
                label = {
                    Text(
                        text = category.displayName,
                        fontSize = 13.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.operatorKeyBg,
                    selectedLabelColor = colors.operatorKeyText,
                    containerColor = colors.surface,
                    labelColor = colors.textSecondary
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
private fun MergedConversionCard(
    units: List<UnitItem>,
    fromUnit: UnitItem,
    onSelectFrom: (UnitItem) -> Unit,
    toUnit: UnitItem,
    onSelectTo: (UnitItem) -> Unit,
    activeTarget: ActiveUnitField,
    onSelectActive: (ActiveUnitField) -> Unit,
    fromDisplayValue: String,
    toDisplayValue: String,
    context: Context,
    swapTrigger: Int = 0,
    cursorIndex: Int = 0,
    onCursorChange: ((Int) -> Unit)? = null,
    onCut: (() -> Unit)? = null,
    onPaste: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    val density = LocalDensity.current
    val system = LocalNumberingSystem.current

    val swapAnim = remember { Animatable(0f) }
    LaunchedEffect(swapTrigger) {
        if (swapTrigger > 0) {
            swapAnim.snapTo(1f)
            swapAnim.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.72f,
                    stiffness = 320f
                )
            )
        }
    }

    val travelDistancePx = with(density) { 36.dp.toPx() }
    val fromTranslationY = swapAnim.value * travelDistancePx
    val toTranslationY = -swapAnim.value * travelDistancePx
    val animAlpha = 1f - (swapAnim.value * 0.35f)
    val animScale = 1f - (swapAnim.value * 0.04f)

    Surface(
        color = colors.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.12f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            // FROM ROW with Swap Motion
            Box(
                modifier = Modifier.graphicsLayer {
                    translationY = fromTranslationY
                    alpha = animAlpha
                    scaleX = animScale
                    scaleY = animScale
                }
            ) {
                ConversionUnitRow(
                    label = "From",
                    unit = fromUnit,
                    units = units,
                    onSelectUnit = onSelectFrom,
                    displayValue = fromDisplayValue,
                    isActive = activeTarget == ActiveUnitField.FROM,
                    onClick = { onSelectActive(ActiveUnitField.FROM) },
                    onCopy = {
                        val formatted = NumberFormatter.formatNumber(fromDisplayValue, system)
                        copyText(context, formatted, "${fromUnit.name} (${fromUnit.symbol})")
                    },
                    onCut = if (activeTarget == ActiveUnitField.FROM) onCut else null,
                    onPaste = if (activeTarget == ActiveUnitField.FROM) onPaste else null,
                    cursorIndex = if (activeTarget == ActiveUnitField.FROM) cursorIndex else 0,
                    onCursorChange = if (activeTarget == ActiveUnitField.FROM) onCursorChange else null
                )
            }

            // SLEEK INNER DIVIDER
            HorizontalDivider(
                color = colors.textSecondary.copy(alpha = 0.12f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // TO ROW with Swap Motion
            Box(
                modifier = Modifier.graphicsLayer {
                    translationY = toTranslationY
                    alpha = animAlpha
                    scaleX = animScale
                    scaleY = animScale
                }
            ) {
                ConversionUnitRow(
                    label = "To",
                    unit = toUnit,
                    units = units,
                    onSelectUnit = onSelectTo,
                    displayValue = toDisplayValue,
                    isActive = activeTarget == ActiveUnitField.TO,
                    onClick = { onSelectActive(ActiveUnitField.TO) },
                    onCopy = {
                        val formatted = NumberFormatter.formatNumber(toDisplayValue, system)
                        copyText(context, formatted, "${toUnit.name} (${toUnit.symbol})")
                    },
                    onCut = if (activeTarget == ActiveUnitField.TO) onCut else null,
                    onPaste = if (activeTarget == ActiveUnitField.TO) onPaste else null,
                    cursorIndex = if (activeTarget == ActiveUnitField.TO) cursorIndex else 0,
                    onCursorChange = if (activeTarget == ActiveUnitField.TO) onCursorChange else null
                )
            }
        }
    }
}

@Composable
private fun ConversionUnitRow(
    label: String,
    unit: UnitItem,
    units: List<UnitItem>,
    onSelectUnit: (UnitItem) -> Unit,
    displayValue: String,
    isActive: Boolean,
    onClick: () -> Unit,
    onCopy: () -> Unit,
    onCut: (() -> Unit)? = null,
    onPaste: ((String) -> Unit)? = null,
    cursorIndex: Int = 0,
    onCursorChange: ((Int) -> Unit)? = null
) {
    val context = LocalContext.current
    val colors = LocalCalculatorColors.current
    val system = LocalNumberingSystem.current
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val viewConfig = LocalViewConfiguration.current

    val horizontalInset = 10.dp
    val horizontalInsetPx = with(density) { horizontalInset.toPx() }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var isDraggingCursor by remember { mutableStateOf(false) }
    var showActionPopup by remember { mutableStateOf(false) }
    var actionPopupOffset by remember { mutableStateOf(Offset.Zero) }

    val formattedRes = remember(displayValue, system) {
        NumberFormatter.formatExpression(displayValue, system)
    }
    val formattedDisplay = if (displayValue.isEmpty()) "0" else formattedRes.formattedText
    val formattedCursorIndex = remember(displayValue, cursorIndex, system) {
        NumberFormatter.rawToFormattedIndex(cursorIndex, formattedRes)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "converterCursorTransition")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 530, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "converterCursorAlpha"
    )

    val currentDisplayValue by rememberUpdatedState(displayValue)
    val currentCursorIndex by rememberUpdatedState(cursorIndex)
    val currentOnCursorChange by rememberUpdatedState(onCursorChange)
    val currentFormattedRes by rememberUpdatedState(formattedRes)
    val currentFormattedCursorIndex by rememberUpdatedState(formattedCursorIndex)
    val currentFormattedDisplay by rememberUpdatedState(formattedDisplay)
    val currentTextLayoutResult by rememberUpdatedState(textLayoutResult)
    val currentHaptic by rememberUpdatedState(haptic)

    fun updateCursor(offset: Offset) {
        if (currentDisplayValue.isEmpty()) {
            currentOnCursorChange?.invoke(0)
            return
        }
        val layout = currentTextLayoutResult ?: return
        val centerY = (layout.size.height / 2f).coerceAtLeast(1f)
        val charIndex = layout.getOffsetForPosition(Offset(offset.x, centerY))
        val rawIndex = NumberFormatter.formattedToRawIndex(charIndex, currentFormattedRes).coerceIn(0, currentDisplayValue.length)
        if (rawIndex != currentCursorIndex) {
            currentHaptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            currentOnCursorChange?.invoke(rawIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isActive) colors.operatorKeyText else colors.textSecondary
            )
            UnitDropdownPicker(units = units, selectedUnit = unit, onSelect = onSelectUnit)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isActive) colors.operatorKeyBg.copy(alpha = 0.25f)
                    else colors.background.copy(alpha = 0.6f)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalInset)
                        .padding(bottom = 14.dp, top = 2.dp)
                        .cursorMagnifier(
                            enabled = isDraggingCursor && isActive,
                            sourceCenter = {
                                val currentClamped = formattedCursorIndex.coerceIn(0, formattedDisplay.length)
                                val currentRect = textLayoutResult.getCursorRectSafe(currentClamped)
                                if (currentRect != null) {
                                    Offset(currentRect.left, (currentRect.top + currentRect.bottom) / 2f)
                                } else {
                                    Offset.Unspecified
                                }
                            }
                        )
                        .pointerInput(isActive) {
                            if (isActive) {
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    val layout = currentTextLayoutResult
                                    val currentClamped = currentFormattedCursorIndex.coerceIn(0, currentFormattedDisplay.length)
                                    val currentRect = layout.getCursorRectSafe(currentClamped)

                                    val handleRadius = 6.dp.toPx()
                                    val handleCenter = if (currentRect != null) {
                                        Offset(currentRect.left, currentRect.bottom + handleRadius - 1.dp.toPx())
                                    } else null

                                    val hitRadius = 26.dp.toPx()
                                    val isHandleHit = handleCenter != null && (down.position - handleCenter).getDistance() <= hitRadius

                                    if (isHandleHit) {
                                        // User specifically grabbed the droplet handle
                                        down.consume()
                                        var hasDragged = false
                                        val pointerId = down.id
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
                                                    updateCursor(change.position)
                                                }
                                            } else {
                                                break
                                            }
                                        }
                                        isDraggingCursor = false

                                        if (!hasDragged) {
                                            val hasClip = ClipboardHelper.hasClipboardText(context)
                                            val canCutOrCopy = currentDisplayValue.isNotEmpty()
                                            if (canCutOrCopy || hasClip) {
                                                currentHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                actionPopupOffset = handleCenter ?: down.position
                                                showActionPopup = true
                                            }
                                        }
                                    } else {
                                        // Touched text: detect tap or long press
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
                                                    updateCursor(down.position)
                                                    val hasClip = ClipboardHelper.hasClipboardText(context)
                                                    val canCutOrCopy = currentDisplayValue.isNotEmpty()
                                                    if (canCutOrCopy || hasClip) {
                                                        actionPopupOffset = down.position
                                                        showActionPopup = true
                                                    }
                                                }
                                            } else {
                                                if (!moved && !isLongPress && !change.isConsumed) {
                                                    updateCursor(down.position)
                                                    showActionPopup = false
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
                            if (isActive) {
                                val currentClamped = formattedCursorIndex.coerceIn(0, formattedDisplay.length)
                                val cursorRect = textLayoutResult.getCursorRectSafe(currentClamped)
                                if (cursorRect != null) {
                                    val effectiveAlpha = if (isDraggingCursor) 1f else cursorAlpha
                                    val cursorWidth = 2.dp.toPx()
                                    val verticalInset = 1.dp.toPx()
                                    val cursorHeight = (cursorRect.bottom - cursorRect.top - verticalInset * 2).coerceAtLeast(10f)
                                    val cursorTop = cursorRect.top + verticalInset

                                    // Vertical cursor bar (clean Finance tool style)
                                    drawRoundRect(
                                        color = colors.operatorKeyText.copy(alpha = effectiveAlpha),
                                        topLeft = Offset(cursorRect.left - cursorWidth / 2, cursorTop),
                                        size = Size(cursorWidth, cursorHeight),
                                        cornerRadius = CornerRadius(cursorWidth / 2, cursorWidth / 2)
                                    )

                                    // Teardrop droplet handle directly at bottom of cursor line
                                    val handleRadius = 6.dp.toPx()
                                    val handleCenter = Offset(cursorRect.left, cursorRect.bottom + handleRadius - 1.dp.toPx())

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
                        text = formattedDisplay,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) colors.textPrimary else colors.operatorKeyText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                        onTextLayout = { textLayoutResult = it }
                    )
                }
                Text(
                    text = "${unit.name} (${unit.symbol})",
                    fontSize = 12.5.sp,
                    color = colors.textSecondary
                )
            }

            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    val hasClipboard = remember(showActionPopup) {
        if (showActionPopup) ClipboardHelper.hasClipboardText(context) else false
    }
    val canCopyOrCut = displayValue.isNotEmpty() && displayValue != "0"

    CutCopyPastePopup(
        visible = showActionPopup && isActive,
        onDismissRequest = { showActionPopup = false },
        anchorOffset = actionPopupOffset,
        onCut = if (canCopyOrCut && onCut != null) {
            {
                onCut.invoke()
                showActionPopup = false
            }
        } else null,
        onCopy = if (displayValue.isNotEmpty()) {
            {
                onCopy.invoke()
                showActionPopup = false
            }
        } else null,
        onPaste = if (hasClipboard && onPaste != null) {
            {
                val text = ClipboardHelper.getClipboardText(context)
                if (text != null) {
                    onPaste.invoke(text)
                }
                showActionPopup = false
            }
        } else null
    )
}

@Composable
private fun CurrencyLiveStatusBanner(
    currencyState: CurrencyRateState,
    onRefresh: () -> Unit
) {
    val colors = LocalCalculatorColors.current

    // Sleek single-line status banner with NO background container, placed below "To" section
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Status dot indicator
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (currencyState.isLive) Color(0xFF10B981) else Color(0xFFF59E0B))
            )

            // Single line of text without "(Internet)"
            Text(
                text = when {
                    currencyState.isLoading -> "Updating exchange rates..."
                    currencyState.isLive -> "Live Exchange Rates"
                    else -> "Saved Offline Rates"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (currencyState.lastUpdatedText.isNotEmpty() && !currencyState.isLoading) {
                Text(
                    text = "• ${currencyState.lastUpdatedText}",
                    fontSize = 11.sp,
                    color = colors.textSecondary.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (currencyState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 1.5.dp,
                color = colors.operatorKeyText
            )
        } else {
            IconButton(
                onClick = onRefresh,
                modifier = Modifier.size(22.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh rates",
                    tint = colors.operatorKeyText,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

private fun copyText(context: Context, text: String, label: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Converted Value", text))
    Toast.makeText(context, "Copied $text $label", Toast.LENGTH_SHORT).show()
}

@Composable
fun UnitDropdownPicker(
    units: List<UnitItem>,
    selectedUnit: UnitItem,
    onSelect: (UnitItem) -> Unit
) {
    val colors = LocalCalculatorColors.current
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colors.operatorKeyBg.copy(alpha = 0.35f),
            border = BorderStroke(1.dp, colors.operatorKeyText.copy(alpha = 0.35f)),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = selectedUnit.name,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = colors.operatorKeyBg,
                    modifier = Modifier.padding(vertical = 1.dp)
                ) {
                    Text(
                        text = selectedUnit.symbol,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.operatorKeyText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select unit",
                    tint = colors.operatorKeyText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(18.dp),
            containerColor = colors.surface,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
            border = BorderStroke(1.dp, colors.operatorKeyBg.copy(alpha = 0.5f)),
            modifier = Modifier
                .widthIn(min = 230.dp, max = 290.dp)
                .heightIn(max = 380.dp)
        ) {
            units.forEach { unit ->
                val isSelected = unit.id == selectedUnit.id
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) colors.operatorKeyBg else colors.background,
                                    modifier = Modifier.widthIn(min = 36.dp)
                                ) {
                                    Text(
                                        text = unit.symbol,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) colors.operatorKeyText else colors.textSecondary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                                Text(
                                    text = unit.name,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) colors.operatorKeyText else colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = colors.operatorKeyText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    onClick = {
                        onSelect(unit)
                        expanded = false
                    },
                    modifier = Modifier
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) colors.operatorKeyBg.copy(alpha = 0.25f) else Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun ConverterNumpad(
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onDoubleZero: () -> Unit,
    onToggleSign: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onSwap: () -> Unit,
    swapTrigger: Int = 0,
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false
) {
    val colors = LocalCalculatorColors.current
    val rowSpacing = if (isLandscape) 4.dp else 6.dp
    val colSpacing = if (isLandscape) 6.dp else 8.dp

    // Smooth 180-degree rotation animation on swap button press
    val swapRotation by animateFloatAsState(
        targetValue = swapTrigger * 180f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "swapNumpadRotation"
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(rowSpacing)
    ) {
        // Row 1: 7, 8, 9, AC
        Row(
            modifier = Modifier.fillMaxWidth().then(if (isLandscape) Modifier.weight(1f) else Modifier),
            horizontalArrangement = Arrangement.spacedBy(colSpacing)
        ) {
            NumpadButton(text = "7", onClick = { onDigit("7") }, modifier = Modifier.weight(1f), isLandscape = isLandscape)
            NumpadButton(text = "8", onClick = { onDigit("8") }, modifier = Modifier.weight(1f), isLandscape = isLandscape)
            NumpadButton(text = "9", onClick = { onDigit("9") }, modifier = Modifier.weight(1f), isLandscape = isLandscape)
            NumpadButton(
                text = "AC",
                onClick = onClear,
                bgColor = colors.clearKeyBg,
                textColor = colors.clearKeyText,
                modifier = Modifier.weight(1f),
                isLandscape = isLandscape
            )
        }

        // Row 2: 4, 5, 6, ⌫
        Row(
            modifier = Modifier.fillMaxWidth().then(if (isLandscape) Modifier.weight(1f) else Modifier),
            horizontalArrangement = Arrangement.spacedBy(colSpacing)
        ) {
            NumpadButton(text = "4", onClick = { onDigit("4") }, modifier = Modifier.weight(1f), isLandscape = isLandscape)
            NumpadButton(text = "5", onClick = { onDigit("5") }, modifier = Modifier.weight(1f), isLandscape = isLandscape)
            NumpadButton(text = "6", onClick = { onDigit("6") }, modifier = Modifier.weight(1f), isLandscape = isLandscape)
            NumpadButton(
                icon = Icons.AutoMirrored.Filled.Backspace,
                onClick = onBackspace,
                bgColor = colors.functionKeyBg,
                textColor = colors.functionKeyText,
                modifier = Modifier.weight(1f),
                isLandscape = isLandscape
            )
        }

        // Row 3: 1, 2, 3, ±
        Row(
            modifier = Modifier.fillMaxWidth().then(if (isLandscape) Modifier.weight(1f) else Modifier),
            horizontalArrangement = Arrangement.spacedBy(colSpacing)
        ) {
            NumpadButton(text = "1", onClick = { onDigit("1") }, modifier = Modifier.weight(1f), isLandscape = isLandscape)
            NumpadButton(text = "2", onClick = { onDigit("2") }, modifier = Modifier.weight(1f), isLandscape = isLandscape)
            NumpadButton(text = "3", onClick = { onDigit("3") }, modifier = Modifier.weight(1f), isLandscape = isLandscape)
            NumpadButton(
                text = "±",
                onClick = onToggleSign,
                bgColor = colors.functionKeyBg,
                textColor = colors.functionKeyText,
                modifier = Modifier.weight(1f),
                isLandscape = isLandscape
            )
        }

        // Row 4: 00, 0, ., ⇌ (Restored single Swap button in the numpad with animated rotation)
        Row(
            modifier = Modifier.fillMaxWidth().then(if (isLandscape) Modifier.weight(1f) else Modifier),
            horizontalArrangement = Arrangement.spacedBy(colSpacing)
        ) {
            NumpadButton(text = "00", onClick = onDoubleZero, modifier = Modifier.weight(1f), isLandscape = isLandscape)
            NumpadButton(text = "0", onClick = { onDigit("0") }, modifier = Modifier.weight(1f), isLandscape = isLandscape)
            NumpadButton(text = ".", onClick = onDecimal, modifier = Modifier.weight(1f), isLandscape = isLandscape)
            NumpadButton(
                icon = Icons.Default.SwapVert,
                iconModifier = Modifier.graphicsLayer { rotationZ = swapRotation },
                onClick = onSwap,
                bgColor = colors.operatorKeyBg,
                textColor = colors.operatorKeyText,
                modifier = Modifier.weight(1f),
                isLandscape = isLandscape
            )
        }
    }
}

@Composable
private fun NumpadButton(
    text: String = "",
    icon: ImageVector? = null,
    iconModifier: Modifier = Modifier,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    bgColor: Color? = null,
    textColor: Color? = null,
    isLandscape: Boolean = false
) {
    val colors = LocalCalculatorColors.current
    val background = bgColor ?: colors.numberKeyBg
    val contentColor = textColor ?: colors.numberKeyText
    val height = if (isLandscape) Modifier.fillMaxHeight() else Modifier.height(56.dp)

    Surface(
        onClick = onClick,
        color = background,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.then(height)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp).then(iconModifier)
                )
            } else {
                Text(
                    text = text,
                    fontSize = if (text.length > 2) 18.sp else 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            }
        }
    }
}
