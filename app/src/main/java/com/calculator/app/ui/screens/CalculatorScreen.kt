package com.calculator.app.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calculator.app.data.preferences.NumberingSystem
import com.calculator.app.ui.CalculatorAction
import com.calculator.app.ui.CalculatorState
import com.calculator.app.ui.components.AppInfoDialog
import com.calculator.app.ui.components.CalculatorButton
import com.calculator.app.ui.components.ClipboardHelper
import com.calculator.app.ui.components.DisplaySection
import com.calculator.app.ui.components.HistoryBottomSheet
import com.calculator.app.ui.components.KeypadSection
import com.calculator.app.ui.components.NumberFormatDialog
import com.calculator.app.ui.components.ThemeSelectionDialog
import com.calculator.app.ui.theme.CalculatorColors
import com.calculator.app.ui.theme.LocalCalculatorColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    state: CalculatorState,
    onAction: (CalculatorAction) -> Unit,
    onOpenToolsHub: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    val context = LocalContext.current
    var isMenuExpanded by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    ModeToggleSwitch(
                        isScientific = state.isScientificMode,
                        onToggle = { onAction(CalculatorAction.ToggleScientificMode) },
                        colors = colors
                    )
                },
                actions = {
                    // Feature Hub icon: Opens Converters, Financial & Health tools
                    IconButton(
                        onClick = onOpenToolsHub
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = "Converters & Tools",
                            tint = colors.textPrimary
                        )
                    }

                    // 3-dot overflow menu (History, Theme, App Info)
                    Box {
                        IconButton(onClick = { isMenuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = colors.textPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false },
                            shape = RoundedCornerShape(20.dp),
                            containerColor = colors.surface,
                            tonalElevation = 8.dp,
                            shadowElevation = 16.dp,
                            border = BorderStroke(1.dp, colors.operatorKeyBg.copy(alpha = 0.6f)),
                            modifier = Modifier.width(235.dp)
                        ) {
                            // 1. History
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "History",
                                            color = colors.textPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "Past calculations",
                                            color = colors.textSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(colors.operatorKeyBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            tint = colors.operatorKeyText,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                onClick = {
                                    isMenuExpanded = false
                                    onAction(CalculatorAction.ToggleHistorySheet(true))
                                }
                            )

                            // 2. Theme
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "Theme",
                                            color = colors.textPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "Light & Dark Mode",
                                            color = colors.textSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Palette,
                                            contentDescription = null,
                                            tint = Color(0xFF8B5CF6),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                onClick = {
                                    isMenuExpanded = false
                                    onAction(CalculatorAction.ToggleThemeDialog(true))
                                }
                            )

                            // 3. Numbering System
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "Number Format",
                                            color = colors.textPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = state.numberingSystem.title,
                                            color = colors.textSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF3B82F6).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Language,
                                            contentDescription = null,
                                            tint = Color(0xFF3B82F6),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                onClick = {
                                    isMenuExpanded = false
                                    onAction(CalculatorAction.ToggleNumberFormatDialog(true))
                                }
                            )

                            // 4. Copy & Paste Quick Actions
                            val hasClip = ClipboardHelper.hasClipboardText(context)
                            val canCopy = state.expression.isNotEmpty() || (state.isResultCalculated && state.finalResult.isNotEmpty())
                            if (canCopy) {
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = "Copy",
                                                color = colors.textPrimary,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = "Copy equation or result",
                                                color = colors.textSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF06B6D4).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = null,
                                                tint = Color(0xFF06B6D4),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    onClick = {
                                        isMenuExpanded = false
                                        val text = if (state.isResultCalculated && state.finalResult.isNotEmpty()) state.finalResult else state.expression
                                        ClipboardHelper.copy(context, text)
                                    }
                                )
                            }

                            if (hasClip) {
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = "Paste",
                                                color = colors.textPrimary,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = "Paste from clipboard",
                                                color = colors.textSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFFEC4899).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentPaste,
                                                contentDescription = null,
                                                tint = Color(0xFFEC4899),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    onClick = {
                                        isMenuExpanded = false
                                        val clipText = ClipboardHelper.getClipboardText(context)
                                        if (clipText != null) {
                                            onAction(CalculatorAction.Paste(clipText))
                                        }
                                    }
                                )
                            }

                            HorizontalDivider(
                                color = colors.textSecondary.copy(alpha = 0.12f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                            )

                            // 3. App Info
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "App Info",
                                            color = colors.textPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "Version 1.2.0 & tools suite",
                                            color = colors.textSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                onClick = {
                                    isMenuExpanded = false
                                    onAction(CalculatorAction.ToggleAppInfoDialog(true))
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background
                )
            )
        }
    ) { innerPadding ->
        if (isLandscape) {
            // LANDSCAPE LAYOUT: Two-pane side-by-side design
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(colors.background),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Pane: Display Area + Bottom Utility Bar (spacious)
                Column(
                    modifier = Modifier
                        .weight(0.40f)
                        .fillMaxHeight()
                        .padding(start = 12.dp, end = 6.dp, bottom = 6.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        DisplaySection(
                            expression = state.expression,
                            previewResult = state.previewResult,
                            finalResult = state.finalResult,
                            errorMessage = state.errorMessage,
                            isResultCalculated = state.isResultCalculated,
                            cursorIndex = state.cursorIndex,
                            onCursorChange = { onAction(CalculatorAction.MoveCursor(it)) },
                            numberingSystem = state.numberingSystem,
                            isLandscape = true,
                            onCut = { onAction(CalculatorAction.Cut) },
                            onCopy = {
                                val text = if (state.isResultCalculated && state.finalResult.isNotEmpty()) state.finalResult else state.expression
                                ClipboardHelper.copy(context, text)
                            },
                            onPaste = { onAction(CalculatorAction.Paste(it)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Landscape Utility Row (DEG/RAD toggle switch, 2nd, Backspace)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (state.isScientificMode) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AngleModeToggleSwitch(
                                    isDegreeMode = state.isDegreeMode,
                                    onToggle = { onAction(CalculatorAction.ToggleAngleMode) },
                                    colors = colors,
                                    modifier = Modifier.width(115.dp).height(32.dp)
                                )

                                CalculatorButton(
                                    text = "2nd",
                                    modifier = Modifier.width(52.dp).height(32.dp),
                                    backgroundColor = if (state.isInvMode) colors.operatorKeyBg else colors.functionKeyBg,
                                    contentColor = if (state.isInvMode) colors.operatorKeyText else colors.functionKeyText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    shapeCornerRadius = 8.dp,
                                    onClick = { onAction(CalculatorAction.ToggleInvMode) }
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        CalculatorButton(
                            icon = Icons.AutoMirrored.Filled.Backspace,
                            modifier = Modifier.width(44.dp).height(32.dp),
                            backgroundColor = colors.background,
                            contentColor = colors.textPrimary,
                            iconSize = 18.dp,
                            shapeCornerRadius = 8.dp,
                            onClick = { onAction(CalculatorAction.Backspace) }
                        )
                    }
                }

                // Vertical Divider between Display and Keypad
                VerticalDivider(
                    color = colors.textSecondary.copy(alpha = 0.35f),
                    thickness = 2.dp,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 8.dp)
                )

                // Right Pane: Keypad Area (Proportional 5 rows, zero clipping)
                Box(
                    modifier = Modifier
                        .weight(0.60f)
                        .fillMaxHeight()
                        .padding(start = 6.dp, end = 12.dp, bottom = 4.dp)
                ) {
                    KeypadSection(
                        onAction = onAction,
                        isScientificMode = state.isScientificMode,
                        isDegreeMode = state.isDegreeMode,
                        isInvMode = state.isInvMode,
                        isLandscape = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        } else {
            // PORTRAIT LAYOUT: Standard vertical column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(colors.background),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Display Area (Expression, Live Preview, Result)
                DisplaySection(
                    expression = state.expression,
                    previewResult = state.previewResult,
                    finalResult = state.finalResult,
                    errorMessage = state.errorMessage,
                    isResultCalculated = state.isResultCalculated,
                    cursorIndex = state.cursorIndex,
                    onCursorChange = { onAction(CalculatorAction.MoveCursor(it)) },
                    numberingSystem = state.numberingSystem,
                    isLandscape = false,
                    onCut = { onAction(CalculatorAction.Cut) },
                    onCopy = {
                        val text = if (state.isResultCalculated && state.finalResult.isNotEmpty()) state.finalResult else state.expression
                        ClipboardHelper.copy(context, text)
                    },
                    onPaste = { onAction(CalculatorAction.Paste(it)) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )

                // Utility row right above the divider (DEG/RAD toggle switch, 2nd, Backspace)
                // Grid-aligned 1:1 with keypad columns underneath
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    val totalWidth = maxWidth
                    val sciColSpacing = 4.dp
                    val sciColWidth = (totalWidth - sciColSpacing * 4) / 5
                    val sciToggleWidth = sciColWidth * 2 + sciColSpacing

                    val stdColSpacing = 10.dp
                    val stdColWidth = (totalWidth - stdColSpacing * 3) / 4

                    if (state.isScientificMode) {
                        // Scientific Utility Row:
                        // Col 1 & 2: [ DEG | RAD ] Toggle Switch (spans width of Col 1 + gap + Col 2)
                        // Col 3: [ 2nd ] Button (width: sciColWidth)
                        // Col 4: Spacer (width: sciColWidth)
                        // Col 5: [ ⌫ ] Backspace Button (width: sciColWidth, black background)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            horizontalArrangement = Arrangement.spacedBy(sciColSpacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AngleModeToggleSwitch(
                                isDegreeMode = state.isDegreeMode,
                                onToggle = { onAction(CalculatorAction.ToggleAngleMode) },
                                colors = colors,
                                modifier = Modifier
                                    .width(sciToggleWidth)
                                    .fillMaxHeight()
                            )

                            CalculatorButton(
                                text = "2nd",
                                modifier = Modifier
                                    .width(sciColWidth)
                                    .fillMaxHeight(),
                                backgroundColor = if (state.isInvMode) colors.operatorKeyBg else colors.functionKeyBg,
                                contentColor = if (state.isInvMode) colors.operatorKeyText else colors.functionKeyText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                shapeCornerRadius = 11.dp,
                                onClick = {
                                    onAction(CalculatorAction.ToggleInvMode)
                                }
                            )

                            Spacer(modifier = Modifier.width(sciColWidth))

                            CalculatorButton(
                                icon = Icons.AutoMirrored.Filled.Backspace,
                                modifier = Modifier
                                    .width(sciColWidth)
                                    .fillMaxHeight(),
                                backgroundColor = colors.background,
                                contentColor = colors.textPrimary,
                                iconSize = 22.dp,
                                shapeCornerRadius = 11.dp,
                                onClick = {
                                    onAction(CalculatorAction.Backspace)
                                }
                            )
                        }
                    } else {
                        // Standard Utility Row:
                        // Col 1, 2, 3: empty
                        // Col 4: [ ⌫ ] Backspace Button (aligned directly over Col 4: ÷, ×, −, +, =, black background)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CalculatorButton(
                                icon = Icons.AutoMirrored.Filled.Backspace,
                                modifier = Modifier
                                    .width(stdColWidth)
                                    .fillMaxHeight(),
                                backgroundColor = colors.background,
                                contentColor = colors.textPrimary,
                                iconSize = 24.dp,
                                shapeCornerRadius = 14.dp,
                                onClick = {
                                    onAction(CalculatorAction.Backspace)
                                }
                            )
                        }
                    }
                }

                // Divider separating Display & Keypad (the bar that keeps numpad separated)
                // Clearly visible, thicker (2.5dp) with elevated contrast
                HorizontalDivider(
                    color = colors.textSecondary.copy(alpha = 0.35f),
                    thickness = 2.5.dp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                )

                // Keypad Area
                KeypadSection(
                    onAction = onAction,
                    isScientificMode = state.isScientificMode,
                    isDegreeMode = state.isDegreeMode,
                    isInvMode = state.isInvMode,
                    isLandscape = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Calculation History Bottom Sheet
    if (state.isHistoryVisible) {
        HistoryBottomSheet(
            historyList = state.historyList,
            onAction = onAction,
            onDismiss = { onAction(CalculatorAction.ToggleHistorySheet(false)) },
            numberingSystem = state.numberingSystem
        )
    }

    // Theme Selection Dialog (System Default, Light Mode, Dark Mode)
    if (state.isThemeDialogVisible) {
        ThemeSelectionDialog(
            selectedTheme = state.themeMode,
            onSelectTheme = { onAction(CalculatorAction.SelectTheme(it)) },
            onDismiss = { onAction(CalculatorAction.ToggleThemeDialog(false)) }
        )
    }

    // Numbering System Dialog (International, Indian / Lakhs)
    if (state.isNumberFormatDialogVisible) {
        NumberFormatDialog(
            selectedSystem = state.numberingSystem,
            onSelectSystem = { onAction(CalculatorAction.SelectNumberingSystem(it)) },
            onDismiss = { onAction(CalculatorAction.ToggleNumberFormatDialog(false)) }
        )
    }

    // App Info Dialog
    if (state.isAppInfoDialogVisible) {
        AppInfoDialog(
            onDismiss = { onAction(CalculatorAction.ToggleAppInfoDialog(false)) }
        )
    }
}

/**
 * Sleek, segmented pill switch for toggling between Standard and Scientific modes.
 * Placed prominently in the TopAppBar replacing the static text header.
 * Both options have equal width, equal height, and perfect center alignment.
 */
@Composable
private fun ModeToggleSwitch(
    isScientific: Boolean,
    onToggle: () -> Unit,
    colors: CalculatorColors,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier
            .width(225.dp)
            .height(38.dp)
            .clip(RoundedCornerShape(19.dp)),
        color = colors.surface,
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(19.dp),
        border = BorderStroke(1.dp, colors.operatorKeyBg.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .padding(3.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Standard Option
            val stdBg by animateColorAsState(
                targetValue = if (!isScientific) colors.operatorKeyBg else Color.Transparent,
                animationSpec = tween(220, easing = FastOutSlowInEasing),
                label = "stdBg"
            )
            val stdTextColor by animateColorAsState(
                targetValue = if (!isScientific) colors.operatorKeyText else colors.textSecondary,
                animationSpec = tween(220, easing = FastOutSlowInEasing),
                label = "stdText"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(stdBg)
                    .clickable {
                        if (isScientific) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggle()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "Standard Mode",
                        tint = stdTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Standard",
                        color = stdTextColor,
                        fontSize = 12.5.sp,
                        fontWeight = if (!isScientific) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }

            // Scientific Option
            val sciBg by animateColorAsState(
                targetValue = if (isScientific) colors.operatorKeyBg else Color.Transparent,
                animationSpec = tween(220, easing = FastOutSlowInEasing),
                label = "sciBg"
            )
            val sciTextColor by animateColorAsState(
                targetValue = if (isScientific) colors.operatorKeyText else colors.textSecondary,
                animationSpec = tween(220, easing = FastOutSlowInEasing),
                label = "sciText"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(sciBg)
                    .clickable {
                        if (!isScientific) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggle()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = "Scientific Mode",
                        tint = sciTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Scientific",
                        color = sciTextColor,
                        fontSize = 12.5.sp,
                        fontWeight = if (isScientific) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Segmented toggle switch for switching between Degrees (DEG) and Radians (RAD).
 * Styled symmetrically with smooth animated pill indicator and tactile haptic feedback.
 */
@Composable
private fun AngleModeToggleSwitch(
    isDegreeMode: Boolean,
    onToggle: () -> Unit,
    colors: CalculatorColors,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp)),
        color = colors.functionKeyBg,
        shape = RoundedCornerShape(11.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(2.5.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // DEG Option
            val degBg by animateColorAsState(
                targetValue = if (isDegreeMode) colors.operatorKeyBg else Color.Transparent,
                animationSpec = tween(200, easing = FastOutSlowInEasing),
                label = "degBg"
            )
            val degTextColor by animateColorAsState(
                targetValue = if (isDegreeMode) colors.operatorKeyText else colors.textSecondary,
                animationSpec = tween(200, easing = FastOutSlowInEasing),
                label = "degText"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(9.dp))
                    .background(degBg)
                    .clickable {
                        if (!isDegreeMode) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggle()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "DEG",
                    color = degTextColor,
                    fontSize = 12.sp,
                    fontWeight = if (isDegreeMode) FontWeight.Bold else FontWeight.Medium
                )
            }

            // RAD Option
            val radBg by animateColorAsState(
                targetValue = if (!isDegreeMode) colors.operatorKeyBg else Color.Transparent,
                animationSpec = tween(200, easing = FastOutSlowInEasing),
                label = "radBg"
            )
            val radTextColor by animateColorAsState(
                targetValue = if (!isDegreeMode) colors.operatorKeyText else colors.textSecondary,
                animationSpec = tween(200, easing = FastOutSlowInEasing),
                label = "radText"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(9.dp))
                    .background(radBg)
                    .clickable {
                        if (isDegreeMode) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggle()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "RAD",
                    color = radTextColor,
                    fontSize = 12.sp,
                    fontWeight = if (!isDegreeMode) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}



