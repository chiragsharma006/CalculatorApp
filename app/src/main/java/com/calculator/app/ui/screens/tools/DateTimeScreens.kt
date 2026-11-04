package com.calculator.app.ui.screens.tools

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calculator.app.data.preferences.LocalNumberingSystem
import com.calculator.app.domain.format.NumberFormatter
import com.calculator.app.domain.tools.DateTimeEngine
import com.calculator.app.domain.tools.DateDifferenceResult
import com.calculator.app.domain.tools.DateAddSubtractResult
import com.calculator.app.domain.tools.TimeDifferenceResult
import com.calculator.app.domain.tools.TimeAddSubtractResult
import com.calculator.app.ui.theme.LocalCalculatorColors
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// -------------------------------------------------------------
// 1. DATE CALCULATOR SCREEN
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateCalculatorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Difference, 1: Add/Subtract

    // Difference State
    val today = remember { LocalDate.now() }
    var startDate by remember { mutableStateOf(today) }
    var endDate by remember { mutableStateOf(today.plusDays(30)) }

    // Add / Subtract State
    var baseDate by remember { mutableStateOf(today) }
    var isAddOperation by remember { mutableStateOf(true) }
    var inputYears by remember { mutableStateOf("0") }
    var inputMonths by remember { mutableStateOf("0") }
    var inputWeeks by remember { mutableStateOf("0") }
    var inputDays by remember { mutableStateOf("7") }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, dd MMM yyyy") }

    val diffResult = remember(startDate, endDate) {
        DateTimeEngine.calculateDateDifference(startDate, endDate)
    }

    val addSubtractResult = remember(baseDate, isAddOperation, inputYears, inputMonths, inputWeeks, inputDays) {
        val y = inputYears.toIntOrNull() ?: 0
        val m = inputMonths.toIntOrNull() ?: 0
        val w = inputWeeks.toIntOrNull() ?: 0
        val d = inputDays.toIntOrNull() ?: 0
        DateTimeEngine.addSubtractDate(
            baseDate = baseDate,
            years = y,
            months = m,
            weeks = w,
            days = d,
            isAdd = isAddOperation
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Date Calculator",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.background)
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = colors.surface,
                contentColor = colors.operatorKeyText,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp).clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Date Difference",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Add / Subtract",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (selectedTab == 0) {
                // DIFFERENCE MODE
                if (isLandscape) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(0.45f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DatePickersCard(
                                startDate = startDate,
                                endDate = endDate,
                                dateFormatter = dateFormatter,
                                onSelectStart = { startDate = it },
                                onSelectEnd = { endDate = it },
                                onSwap = {
                                    val temp = startDate
                                    startDate = endDate
                                    endDate = temp
                                }
                            )
                        }

                        VerticalDivider(
                            color = colors.textSecondary.copy(alpha = 0.15f),
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxHeight()
                        )

                        Column(
                            modifier = Modifier
                                .weight(0.55f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DateDifferenceResultSection(diffResult)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item {
                            DatePickersCard(
                                startDate = startDate,
                                endDate = endDate,
                                dateFormatter = dateFormatter,
                                onSelectStart = { startDate = it },
                                onSelectEnd = { endDate = it },
                                onSwap = {
                                    val temp = startDate
                                    startDate = endDate
                                    endDate = temp
                                }
                            )
                        }

                        item {
                            DateDifferenceResultSection(diffResult)
                        }
                    }
                }
            } else {
                // ADD / SUBTRACT MODE
                if (isLandscape) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(0.48f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DateAddSubtractInputCard(
                                baseDate = baseDate,
                                isAdd = isAddOperation,
                                years = inputYears,
                                months = inputMonths,
                                weeks = inputWeeks,
                                days = inputDays,
                                dateFormatter = dateFormatter,
                                onSelectBaseDate = { baseDate = it },
                                onToggleAdd = { isAddOperation = it },
                                onYearsChange = { inputYears = it },
                                onMonthsChange = { inputMonths = it },
                                onWeeksChange = { inputWeeks = it },
                                onDaysChange = { inputDays = it }
                            )
                        }

                        VerticalDivider(
                            color = colors.textSecondary.copy(alpha = 0.15f),
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxHeight()
                        )

                        Column(
                            modifier = Modifier
                                .weight(0.52f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DateAddSubtractResultSection(addSubtractResult, dateFormatter)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item {
                            DateAddSubtractInputCard(
                                baseDate = baseDate,
                                isAdd = isAddOperation,
                                years = inputYears,
                                months = inputMonths,
                                weeks = inputWeeks,
                                days = inputDays,
                                dateFormatter = dateFormatter,
                                onSelectBaseDate = { baseDate = it },
                                onToggleAdd = { isAddOperation = it },
                                onYearsChange = { inputYears = it },
                                onMonthsChange = { inputMonths = it },
                                onWeeksChange = { inputWeeks = it },
                                onDaysChange = { inputDays = it }
                            )
                        }

                        item {
                            DateAddSubtractResultSection(addSubtractResult, dateFormatter)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// DATE CALCULATOR COMPONENTS
// -------------------------------------------------------------
@Composable
private fun DatePickersCard(
    startDate: LocalDate,
    endDate: LocalDate,
    dateFormatter: DateTimeFormatter,
    onSelectStart: (LocalDate) -> Unit,
    onSelectEnd: (LocalDate) -> Unit,
    onSwap: () -> Unit
) {
    val colors = LocalCalculatorColors.current
    val context = LocalContext.current

    val startPickerDialog = remember(startDate) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onSelectStart(LocalDate.of(year, month + 1, dayOfMonth))
            },
            startDate.year,
            startDate.monthValue - 1,
            startDate.dayOfMonth
        )
    }

    val endPickerDialog = remember(endDate) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onSelectEnd(LocalDate.of(year, month + 1, dayOfMonth))
            },
            endDate.year,
            endDate.monthValue - 1,
            endDate.dayOfMonth
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Select Dates",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )

            // Start Date Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.background)
                    .clickable { startPickerDialog.show() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Start Date", fontSize = 12.sp, color = colors.textSecondary)
                    Text(
                        text = startDate.format(dateFormatter),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                }
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Pick Start Date",
                    tint = colors.operatorKeyText
                )
            }

            // Swap Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = onSwap,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.background)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap Dates",
                        tint = colors.textSecondary
                    )
                }
            }

            // End Date Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.background)
                    .clickable { endPickerDialog.show() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "End Date", fontSize = 12.sp, color = colors.textSecondary)
                    Text(
                        text = endDate.format(dateFormatter),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                }
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Pick End Date",
                    tint = colors.operatorKeyText
                )
            }

            // Quick preset chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val today = LocalDate.now()
                SuggestionChip(
                    onClick = {
                        onSelectStart(today)
                        onSelectEnd(today.plusDays(7))
                    },
                    label = { Text("+7 Days", fontSize = 11.sp) }
                )
                SuggestionChip(
                    onClick = {
                        onSelectStart(today)
                        onSelectEnd(today.plusMonths(1))
                    },
                    label = { Text("+1 Month", fontSize = 11.sp) }
                )
                SuggestionChip(
                    onClick = {
                        onSelectStart(today)
                        onSelectEnd(today.plusYears(1))
                    },
                    label = { Text("+1 Year", fontSize = 11.sp) }
                )
            }
        }
    }
}

@Composable
private fun DateDifferenceResultSection(diff: DateDifferenceResult) {
    val colors = LocalCalculatorColors.current
    val numberingSystem = LocalNumberingSystem.current

    // Primary Highlight Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "DIFFERENCE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.operatorKeyText,
                letterSpacing = 1.sp
            )

            val headlineParts = mutableListOf<String>()
            if (diff.years > 0) headlineParts.add("${diff.years} ${if (diff.years == 1) "year" else "years"}")
            if (diff.months > 0) headlineParts.add("${diff.months} ${if (diff.months == 1) "month" else "months"}")
            if (diff.days > 0 || headlineParts.isEmpty()) headlineParts.add("${NumberFormatter.formatInteger(diff.days.toLong(), numberingSystem)} ${if (diff.days == 1) "day" else "days"}")

            Text(
                text = headlineParts.joinToString(", "),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            if (diff.isPast) {
                Text(
                    text = "End date is earlier than start date",
                    fontSize = 12.sp,
                    color = Color(0xFFF59E0B)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Breakdown Stats Grid
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DateStatCard(
                title = "Total Days",
                value = "${NumberFormatter.formatInteger(diff.totalDays.toLong(), numberingSystem)} days",
                modifier = Modifier.weight(1f)
            )
            val weeksStr = if (diff.remainingDaysInWeek > 0) {
                "${NumberFormatter.formatInteger(diff.totalWeeks.toLong(), numberingSystem)} wks ${diff.remainingDaysInWeek} d"
            } else {
                "${NumberFormatter.formatInteger(diff.totalWeeks.toLong(), numberingSystem)} weeks"
            }
            DateStatCard(
                title = "Total Weeks",
                value = weeksStr,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DateStatCard(
                title = "Working Days (Mon-Fri)",
                value = "${NumberFormatter.formatInteger(diff.workDays.toLong(), numberingSystem)} days",
                iconTint = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
            DateStatCard(
                title = "Weekend Days (Sat-Sun)",
                value = "${NumberFormatter.formatInteger(diff.weekendDays.toLong(), numberingSystem)} days",
                iconTint = Color(0xFF6366F1),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DateStatCard(
    title: String,
    value: String,
    iconTint: Color? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontSize = 11.sp, color = colors.textSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = iconTint ?: colors.textPrimary
            )
        }
    }
}

@Composable
private fun DateAddSubtractInputCard(
    baseDate: LocalDate,
    isAdd: Boolean,
    years: String,
    months: String,
    weeks: String,
    days: String,
    dateFormatter: DateTimeFormatter,
    onSelectBaseDate: (LocalDate) -> Unit,
    onToggleAdd: (Boolean) -> Unit,
    onYearsChange: (String) -> Unit,
    onMonthsChange: (String) -> Unit,
    onWeeksChange: (String) -> Unit,
    onDaysChange: (String) -> Unit
) {
    val colors = LocalCalculatorColors.current
    val context = LocalContext.current

    val baseDatePickerDialog = remember(baseDate) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onSelectBaseDate(LocalDate.of(year, month + 1, dayOfMonth))
            },
            baseDate.year,
            baseDate.monthValue - 1,
            baseDate.dayOfMonth
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Starting Date",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.background)
                    .clickable { baseDatePickerDialog.show() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Date", fontSize = 12.sp, color = colors.textSecondary)
                    Text(
                        text = baseDate.format(dateFormatter),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                }
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Pick Date",
                    tint = colors.operatorKeyText
                )
            }

            // Operation selector (Add vs Subtract)
            OperationSelector(isAdd = isAdd, onToggleAdd = onToggleAdd)

            // Duration Inputs: Years, Months, Weeks, Days
            Text(text = "Duration", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NumericUnitInputField(label = "Years", value = years, onValueChange = onYearsChange, modifier = Modifier.weight(1f))
                NumericUnitInputField(label = "Months", value = months, onValueChange = onMonthsChange, modifier = Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NumericUnitInputField(label = "Weeks", value = weeks, onValueChange = onWeeksChange, modifier = Modifier.weight(1f))
                NumericUnitInputField(label = "Days", value = days, onValueChange = onDaysChange, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun OperationSelector(
    isAdd: Boolean,
    onToggleAdd: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.background)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Add Toggle
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(9.dp))
                .background(if (isAdd) colors.operatorKeyBg else Color.Transparent)
                .clickable { onToggleAdd(true) },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    tint = if (isAdd) colors.operatorKeyText else colors.textSecondary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add",
                    fontSize = 14.sp,
                    fontWeight = if (isAdd) FontWeight.Bold else FontWeight.Medium,
                    color = if (isAdd) colors.operatorKeyText else colors.textSecondary
                )
            }
        }

        // Subtract Toggle
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(9.dp))
                .background(if (!isAdd) colors.operatorKeyBg else Color.Transparent)
                .clickable { onToggleAdd(false) },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    tint = if (!isAdd) colors.operatorKeyText else colors.textSecondary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Subtract",
                    fontSize = 14.sp,
                    fontWeight = if (!isAdd) FontWeight.Bold else FontWeight.Medium,
                    color = if (!isAdd) colors.operatorKeyText else colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun NumericUnitInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    var isExampleValue by remember { mutableStateOf(true) }

    OutlinedTextField(
        value = value,
        onValueChange = { str ->
            val filtered = str.filter { it.isDigit() }
            if (filtered.length <= 5) {
                if (isExampleValue && filtered.isNotEmpty() && filtered != value) {
                    isExampleValue = false
                    val newText = if (filtered.length > value.length) {
                        if (filtered.startsWith(value)) filtered.substring(value.length)
                        else if (filtered.endsWith(value)) filtered.substring(0, filtered.length - value.length)
                        else filtered.filter { !value.contains(it) }.ifEmpty { filtered.last().toString() }
                    } else {
                        filtered
                    }
                    onValueChange(newText.ifEmpty { filtered })
                } else {
                    isExampleValue = false
                    onValueChange(filtered)
                }
            }
        },
        label = { Text(label, fontSize = 11.sp, maxLines = 1) },
        textStyle = androidx.compose.ui.text.TextStyle(
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.operatorKeyText,
            unfocusedBorderColor = colors.textSecondary.copy(alpha = 0.25f),
            focusedContainerColor = colors.background,
            unfocusedContainerColor = colors.background,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary
        ),
        modifier = modifier
    )
}

@Composable
private fun DateAddSubtractResultSection(
    result: DateAddSubtractResult,
    dateFormatter: DateTimeFormatter
) {
    val colors = LocalCalculatorColors.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "CALCULATED TARGET DATE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.operatorKeyText,
                letterSpacing = 1.sp
            )

            Text(
                text = result.resultDate.format(dateFormatter),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.15f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Day of Week", fontSize = 13.sp, color = colors.textSecondary)
                Text(
                    text = result.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Day of Year", fontSize = 13.sp, color = colors.textSecondary)
                Text(
                    text = "Day ${result.dayOfYear}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Leap Year", fontSize = 13.sp, color = colors.textSecondary)
                Text(
                    text = if (result.isLeapYear) "Yes" else "No",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (result.isLeapYear) Color(0xFF10B981) else colors.textPrimary
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 2. TIME CALCULATOR SCREEN
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeCalculatorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Difference, 1: Add/Subtract

    // Difference State
    val now = remember { LocalTime.now() }
    var startTime by remember { mutableStateOf(LocalTime.of(now.hour, now.minute, 0)) }
    var endTime by remember { mutableStateOf(LocalTime.of(now.hour, now.minute, 0).plusHours(2)) }
    var forceNextDay by remember { mutableStateOf(false) }

    // Add / Subtract State
    var baseTime by remember { mutableStateOf(LocalTime.of(now.hour, now.minute, 0)) }
    var isAddOperation by remember { mutableStateOf(true) }
    var addHoursStr by remember { mutableStateOf("1") }
    var addMinutesStr by remember { mutableStateOf("30") }
    var addSecondsStr by remember { mutableStateOf("0") }

    val diffResult = remember(startTime, endTime, forceNextDay) {
        DateTimeEngine.calculateTimeDifference(startTime, endTime, forceNextDay)
    }

    val addSubtractResult = remember(baseTime, isAddOperation, addHoursStr, addMinutesStr, addSecondsStr) {
        val h = addHoursStr.toLongOrNull() ?: 0L
        val m = addMinutesStr.toLongOrNull() ?: 0L
        val s = addSecondsStr.toLongOrNull() ?: 0L
        DateTimeEngine.addSubtractTime(
            baseTime = baseTime,
            hours = h,
            minutes = m,
            seconds = s,
            isAdd = isAddOperation
        )
    }

    val timeFormatter = remember { DateTimeFormatter.ofPattern("hh:mm:ss a") }
    val shortTimeFormatter = remember { DateTimeFormatter.ofPattern("hh:mm a") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Time Calculator",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.background)
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = colors.surface,
                contentColor = colors.operatorKeyText,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp).clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Time Difference",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Add / Subtract",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (selectedTab == 0) {
                // TIME DIFFERENCE MODE
                if (isLandscape) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(0.45f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TimePickersCard(
                                startTime = startTime,
                                endTime = endTime,
                                forceNextDay = forceNextDay,
                                timeFormatter = timeFormatter,
                                onSelectStart = { startTime = it },
                                onSelectEnd = { endTime = it },
                                onToggleForceNextDay = { forceNextDay = it },
                                onSwap = {
                                    val temp = startTime
                                    startTime = endTime
                                    endTime = temp
                                }
                            )
                        }

                        VerticalDivider(
                            color = colors.textSecondary.copy(alpha = 0.15f),
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxHeight()
                        )

                        Column(
                            modifier = Modifier
                                .weight(0.55f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TimeDifferenceResultSection(diffResult)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item {
                            TimePickersCard(
                                startTime = startTime,
                                endTime = endTime,
                                forceNextDay = forceNextDay,
                                timeFormatter = timeFormatter,
                                onSelectStart = { startTime = it },
                                onSelectEnd = { endTime = it },
                                onToggleForceNextDay = { forceNextDay = it },
                                onSwap = {
                                    val temp = startTime
                                    startTime = endTime
                                    endTime = temp
                                }
                            )
                        }

                        item {
                            TimeDifferenceResultSection(diffResult)
                        }
                    }
                }
            } else {
                // TIME ADD / SUBTRACT MODE
                if (isLandscape) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(0.48f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TimeAddSubtractInputCard(
                                baseTime = baseTime,
                                isAdd = isAddOperation,
                                hours = addHoursStr,
                                minutes = addMinutesStr,
                                seconds = addSecondsStr,
                                timeFormatter = timeFormatter,
                                onSelectBaseTime = { baseTime = it },
                                onToggleAdd = { isAddOperation = it },
                                onHoursChange = { addHoursStr = it },
                                onMinutesChange = { addMinutesStr = it },
                                onSecondsChange = { addSecondsStr = it }
                            )
                        }

                        VerticalDivider(
                            color = colors.textSecondary.copy(alpha = 0.15f),
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxHeight()
                        )

                        Column(
                            modifier = Modifier
                                .weight(0.52f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TimeAddSubtractResultSection(addSubtractResult, timeFormatter)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item {
                            TimeAddSubtractInputCard(
                                baseTime = baseTime,
                                isAdd = isAddOperation,
                                hours = addHoursStr,
                                minutes = addMinutesStr,
                                seconds = addSecondsStr,
                                timeFormatter = timeFormatter,
                                onSelectBaseTime = { baseTime = it },
                                onToggleAdd = { isAddOperation = it },
                                onHoursChange = { addHoursStr = it },
                                onMinutesChange = { addMinutesStr = it },
                                onSecondsChange = { addSecondsStr = it }
                            )
                        }

                        item {
                            TimeAddSubtractResultSection(addSubtractResult, timeFormatter)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TIME CALCULATOR COMPONENTS
// -------------------------------------------------------------
@Composable
private fun TimePickersCard(
    startTime: LocalTime,
    endTime: LocalTime,
    forceNextDay: Boolean,
    timeFormatter: DateTimeFormatter,
    onSelectStart: (LocalTime) -> Unit,
    onSelectEnd: (LocalTime) -> Unit,
    onToggleForceNextDay: (Boolean) -> Unit,
    onSwap: () -> Unit
) {
    val colors = LocalCalculatorColors.current
    val context = LocalContext.current

    val startPickerDialog = remember(startTime) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                onSelectStart(LocalTime.of(hourOfDay, minute, 0))
            },
            startTime.hour,
            startTime.minute,
            false // 12-hour AM/PM view
        )
    }

    val endPickerDialog = remember(endTime) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                onSelectEnd(LocalTime.of(hourOfDay, minute, 0))
            },
            endTime.hour,
            endTime.minute,
            false // 12-hour AM/PM view
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Select Times",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )

            // Start Time Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.background)
                    .clickable { startPickerDialog.show() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Start Time", fontSize = 12.sp, color = colors.textSecondary)
                    Text(
                        text = startTime.format(timeFormatter),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                }
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Pick Start Time",
                    tint = colors.operatorKeyText
                )
            }

            // Swap Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = onSwap,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.background)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap Times",
                        tint = colors.textSecondary
                    )
                }
            }

            // End Time Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.background)
                    .clickable { endPickerDialog.show() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "End Time", fontSize = 12.sp, color = colors.textSecondary)
                    Text(
                        text = endTime.format(timeFormatter),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                }
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Pick End Time",
                    tint = colors.operatorKeyText
                )
            }

            // Next Day Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "End time on next day (+24h)",
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
                Switch(
                    checked = forceNextDay,
                    onCheckedChange = onToggleForceNextDay,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.operatorKeyText,
                        checkedTrackColor = colors.operatorKeyBg
                    )
                )
            }
        }
    }
}

@Composable
private fun TimeDifferenceResultSection(diff: TimeDifferenceResult) {
    val colors = LocalCalculatorColors.current
    val numberingSystem = LocalNumberingSystem.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "DURATION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.operatorKeyText,
                letterSpacing = 1.sp
            )

            val headlineParts = mutableListOf<String>()
            if (diff.hours > 0) headlineParts.add("${diff.hours}h")
            if (diff.minutes > 0) headlineParts.add("${diff.minutes}m")
            if (diff.seconds > 0 || headlineParts.isEmpty()) headlineParts.add("${diff.seconds}s")

            Text(
                text = headlineParts.joinToString(" "),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            if (diff.isNextDay) {
                Text(
                    text = "Spans across midnight / next day (+24h)",
                    fontSize = 12.sp,
                    color = Color(0xFF0EA5E9)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Time Stats Grid
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DateStatCard(
                title = "Total Hours",
                value = "${NumberFormatter.formatDouble(diff.totalHours, numberingSystem, 2)} hrs",
                modifier = Modifier.weight(1f)
            )
            DateStatCard(
                title = "Total Minutes",
                value = "${NumberFormatter.formatDouble(diff.totalMinutes, numberingSystem, 2)} min",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DateStatCard(
                title = "Total Seconds",
                value = "${NumberFormatter.formatInteger(diff.totalSeconds, numberingSystem)} s",
                modifier = Modifier.weight(1f)
            )
            DateStatCard(
                title = "Percent of Day",
                value = "${NumberFormatter.formatDouble(diff.percentageOfDay, numberingSystem, 2)}%",
                iconTint = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TimeAddSubtractInputCard(
    baseTime: LocalTime,
    isAdd: Boolean,
    hours: String,
    minutes: String,
    seconds: String,
    timeFormatter: DateTimeFormatter,
    onSelectBaseTime: (LocalTime) -> Unit,
    onToggleAdd: (Boolean) -> Unit,
    onHoursChange: (String) -> Unit,
    onMinutesChange: (String) -> Unit,
    onSecondsChange: (String) -> Unit
) {
    val colors = LocalCalculatorColors.current
    val context = LocalContext.current

    val baseTimePickerDialog = remember(baseTime) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                onSelectBaseTime(LocalTime.of(hourOfDay, minute, 0))
            },
            baseTime.hour,
            baseTime.minute,
            false
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Starting Time",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.background)
                    .clickable { baseTimePickerDialog.show() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Time", fontSize = 12.sp, color = colors.textSecondary)
                    Text(
                        text = baseTime.format(timeFormatter),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                }
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Pick Time",
                    tint = colors.operatorKeyText
                )
            }

            // Operation selector (Add vs Subtract)
            OperationSelector(isAdd = isAdd, onToggleAdd = onToggleAdd)

            // Duration Inputs: Hours, Minutes, Seconds
            Text(text = "Duration", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NumericUnitInputField(label = "Hours", value = hours, onValueChange = onHoursChange, modifier = Modifier.weight(1f))
                NumericUnitInputField(label = "Minutes", value = minutes, onValueChange = onMinutesChange, modifier = Modifier.weight(1f))
                NumericUnitInputField(label = "Seconds", value = seconds, onValueChange = onSecondsChange, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TimeAddSubtractResultSection(
    result: TimeAddSubtractResult,
    timeFormatter: DateTimeFormatter
) {
    val colors = LocalCalculatorColors.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "CALCULATED TARGET TIME",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.operatorKeyText,
                letterSpacing = 1.sp
            )

            Text(
                text = result.resultTime.format(timeFormatter),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.15f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Day Offset", fontSize = 13.sp, color = colors.textSecondary)
                val (offsetText, offsetColor) = when {
                    result.daysOffset == 0 -> "Same Day" to Color(0xFF10B981)
                    result.daysOffset > 0 -> "+${result.daysOffset} day(s) ahead" to Color(0xFF0EA5E9)
                    else -> "${result.daysOffset} day(s) behind" to Color(0xFFF59E0B)
                }
                Text(
                    text = offsetText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = offsetColor
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "24-Hour Format", fontSize = 13.sp, color = colors.textSecondary)
                Text(
                    text = result.resultTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
            }
        }
    }
}
