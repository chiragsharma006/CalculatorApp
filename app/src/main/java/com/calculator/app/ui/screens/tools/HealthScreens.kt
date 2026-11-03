package com.calculator.app.ui.screens.tools

import android.app.DatePickerDialog
import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.calculator.app.ui.components.ClipboardHelper
import com.calculator.app.ui.components.CutCopyPastePopup
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calculator.app.data.preferences.LocalNumberingSystem
import com.calculator.app.domain.format.NumberFormatter
import com.calculator.app.domain.tools.AgeResult
import com.calculator.app.domain.tools.BmiCategory
import com.calculator.app.domain.tools.BmiResult
import com.calculator.app.domain.tools.HealthLifeEngine
import com.calculator.app.ui.theme.LocalCalculatorColors
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// -------------------------------------------------------------
// 1. BMI CALCULATOR
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BmiScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var isMetric by remember { mutableStateOf(true) }
    var heightCmStr by remember { mutableStateOf("175") }
    var weightKgStr by remember { mutableStateOf("70") }

    var heightFeetStr by remember { mutableStateOf("5") }
    var heightInchesStr by remember { mutableStateOf("9") }
    var weightLbsStr by remember { mutableStateOf("154") }

    val bmiResult = remember(
        isMetric, heightCmStr, weightKgStr,
        heightFeetStr, heightInchesStr, weightLbsStr
    ) {
        if (isMetric) {
            val cm = heightCmStr.toDoubleOrNull() ?: 0.0
            val kg = weightKgStr.toDoubleOrNull() ?: 0.0
            HealthLifeEngine.calculateBmiMetric(cm, kg)
        } else {
            val feet = heightFeetStr.toDoubleOrNull() ?: 0.0
            val inches = heightInchesStr.toDoubleOrNull() ?: 0.0
            val totalInches = (feet * 12.0) + inches
            val lbs = weightLbsStr.toDoubleOrNull() ?: 0.0
            HealthLifeEngine.calculateBmiImperial(totalInches, lbs)
        }
    }

    val categoryColor = when (bmiResult?.category) {
        BmiCategory.UNDERWEIGHT -> Color(0xFF38BDF8)
        BmiCategory.NORMAL -> Color(0xFF10B981)
        BmiCategory.OVERWEIGHT -> Color(0xFFF59E0B)
        BmiCategory.OBESE_CLASS_1,
        BmiCategory.OBESE_CLASS_2,
        BmiCategory.OBESE_CLASS_3 -> Color(0xFFEF4444)
        null -> colors.textSecondary
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("BMI Calculator", fontSize = if (isLandscape) 18.sp else 20.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        }
    ) { innerPadding ->
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(colors.background)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column: Inputs
                Column(
                    modifier = Modifier
                        .weight(0.48f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BmiUnitToggle(isMetric = isMetric, onToggle = { isMetric = it })
                    BmiInputCard(
                        isMetric = isMetric,
                        heightCmStr = heightCmStr,
                        onHeightCmChange = { heightCmStr = it },
                        weightKgStr = weightKgStr,
                        onWeightKgChange = { weightKgStr = it },
                        heightFeetStr = heightFeetStr,
                        onHeightFeetChange = { heightFeetStr = it },
                        heightInchesStr = heightInchesStr,
                        onHeightInchesChange = { heightInchesStr = it },
                        weightLbsStr = weightLbsStr,
                        onWeightLbsChange = { weightLbsStr = it }
                    )
                }

                VerticalDivider(color = colors.textSecondary.copy(alpha = 0.15f), thickness = 1.dp, modifier = Modifier.fillMaxHeight())

                // Right Column: Result Card
                Column(
                    modifier = Modifier
                        .weight(0.52f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    BmiResultCard(bmiResult = bmiResult, isMetric = isMetric, categoryColor = categoryColor)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(colors.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    BmiUnitToggle(isMetric = isMetric, onToggle = { isMetric = it })
                }
                item {
                    BmiInputCard(
                        isMetric = isMetric,
                        heightCmStr = heightCmStr,
                        onHeightCmChange = { heightCmStr = it },
                        weightKgStr = weightKgStr,
                        onWeightKgChange = { weightKgStr = it },
                        heightFeetStr = heightFeetStr,
                        onHeightFeetChange = { heightFeetStr = it },
                        heightInchesStr = heightInchesStr,
                        onHeightInchesChange = { heightInchesStr = it },
                        weightLbsStr = weightLbsStr,
                        onWeightLbsChange = { weightLbsStr = it }
                    )
                }
                item {
                    BmiResultCard(bmiResult = bmiResult, isMetric = isMetric, categoryColor = categoryColor)
                }
            }
        }
    }
}

@Composable
private fun BmiUnitToggle(isMetric: Boolean, onToggle: (Boolean) -> Unit) {
    val colors = LocalCalculatorColors.current
    Surface(
        color = colors.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BmiSegmentTab(
                title = "Metric System",
                subtitle = "Centimeters / Kilograms",
                isSelected = isMetric,
                icon = Icons.Default.Straighten,
                onClick = { onToggle(true) },
                modifier = Modifier.weight(1f)
            )
            BmiSegmentTab(
                title = "Imperial System",
                subtitle = "Feet + Inches / Pounds",
                isSelected = !isMetric,
                icon = Icons.Default.Scale,
                onClick = { onToggle(false) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BmiSegmentTab(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) colors.operatorKeyBg else Color.Transparent,
        animationSpec = tween(220),
        label = "bmiTabBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) colors.operatorKeyText else colors.textSecondary,
        animationSpec = tween(220),
        label = "bmiTabText"
    )
    val subColor by animateColorAsState(
        targetValue = if (isSelected) colors.operatorKeyText.copy(alpha = 0.75f) else colors.textSecondary.copy(alpha = 0.55f),
        animationSpec = tween(220),
        label = "bmiTabSubText"
    )

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(1.dp, colors.operatorKeyText.copy(alpha = 0.4f)) else null,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) colors.operatorKeyText.copy(alpha = 0.15f) else colors.background.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = contentColor,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    color = subColor,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun BmiInputCard(
    isMetric: Boolean,
    heightCmStr: String,
    onHeightCmChange: (String) -> Unit,
    weightKgStr: String,
    onWeightKgChange: (String) -> Unit,
    heightFeetStr: String,
    onHeightFeetChange: (String) -> Unit,
    heightInchesStr: String,
    onHeightInchesChange: (String) -> Unit,
    weightLbsStr: String,
    onWeightLbsChange: (String) -> Unit
) {
    val colors = LocalCalculatorColors.current
    Surface(
        color = colors.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (isMetric) {
                SimpleInputField(label = "Height", value = heightCmStr, onValueChange = onHeightCmChange, suffix = "cm")
                SimpleInputField(label = "Weight", value = weightKgStr, onValueChange = onWeightKgChange, suffix = "kg")
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        SimpleInputField(
                            label = "Height (Feet)",
                            value = heightFeetStr,
                            onValueChange = onHeightFeetChange,
                            keyboardType = KeyboardType.Number,
                            suffix = "ft"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        SimpleInputField(
                            label = "Inches",
                            value = heightInchesStr,
                            onValueChange = onHeightInchesChange,
                            keyboardType = KeyboardType.Number,
                            suffix = "in"
                        )
                    }
                }

                SimpleInputField(label = "Weight", value = weightLbsStr, onValueChange = onWeightLbsChange, suffix = "lbs")
            }
        }
    }
}

@Composable
private fun BmiResultCard(bmiResult: BmiResult?, isMetric: Boolean, categoryColor: Color) {
    val colors = LocalCalculatorColors.current
    Surface(
        color = colors.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Body Mass Index (BMI)", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textSecondary)

            if (bmiResult != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format("%.1f", bmiResult.bmi),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )

                    Surface(
                        color = categoryColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = bmiResult.category.label,
                            color = categoryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }

                HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.15f))

                val minWeightStr = if (isMetric) "${String.format("%.1f", bmiResult.healthyWeightMinKg)} kg" else "${String.format("%.1f", bmiResult.healthyWeightMinLbs)} lbs"
                val maxWeightStr = if (isMetric) "${String.format("%.1f", bmiResult.healthyWeightMaxKg)} kg" else "${String.format("%.1f", bmiResult.healthyWeightMaxLbs)} lbs"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Healthy Weight Range", fontSize = 14.sp, color = colors.textSecondary)
                    Text(text = "$minWeightStr - $maxWeightStr", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                }
            } else {
                Text(text = "Enter valid height and weight to calculate BMI", fontSize = 14.sp, color = colors.textSecondary)
            }
        }
    }
}

// -------------------------------------------------------------
// 2. AGE CALCULATOR
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgeScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val today = remember { LocalDate.now() }
    var birthDate by remember { mutableStateOf(LocalDate.of(2000, 1, 1)) }

    val ageResult = remember(birthDate, today) {
        HealthLifeEngine.calculateAge(birthDate, today)
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMMM yyyy") }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                birthDate = LocalDate.of(year, month + 1, dayOfMonth)
            },
            birthDate.year,
            birthDate.monthValue - 1,
            birthDate.dayOfMonth
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Age Calculator", fontSize = if (isLandscape) 18.sp else 20.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        }
    ) { innerPadding ->
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(colors.background)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column: Birth Date Picker
                Column(
                    modifier = Modifier
                        .weight(0.44f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    BirthDatePickerCard(
                        birthDate = birthDate,
                        today = today,
                        dateFormatter = dateFormatter,
                        onShowPicker = { datePickerDialog.show() }
                    )
                }

                VerticalDivider(color = colors.textSecondary.copy(alpha = 0.15f), thickness = 1.dp, modifier = Modifier.fillMaxHeight())

                // Right Column: Age Details & Stats
                Column(
                    modifier = Modifier
                        .weight(0.56f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (ageResult != null) {
                        AgeDisplayCard(ageResult = ageResult)
                        NextBirthdayCard(ageResult = ageResult)
                        LifeSummaryCard(ageResult = ageResult)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(colors.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    BirthDatePickerCard(
                        birthDate = birthDate,
                        today = today,
                        dateFormatter = dateFormatter,
                        onShowPicker = { datePickerDialog.show() }
                    )
                }

                if (ageResult != null) {
                    item {
                        AgeDisplayCard(ageResult = ageResult)
                    }
                    item {
                        NextBirthdayCard(ageResult = ageResult)
                    }
                    item {
                        LifeSummaryCard(ageResult = ageResult)
                    }
                }
            }
        }
    }
}

@Composable
private fun BirthDatePickerCard(
    birthDate: LocalDate,
    today: LocalDate,
    dateFormatter: DateTimeFormatter,
    onShowPicker: () -> Unit
) {
    val colors = LocalCalculatorColors.current
    Surface(
        color = colors.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "Date of Birth", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.textSecondary)

            Surface(
                color = colors.background.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.2f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onShowPicker)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = birthDate.format(dateFormatter), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Pick Date", tint = colors.operatorKeyText)
                }
            }

            Text(text = "Today: ${today.format(dateFormatter)}", fontSize = 12.sp, color = colors.textSecondary)
        }
    }
}

@Composable
private fun AgeDisplayCard(ageResult: AgeResult) {
    val colors = LocalCalculatorColors.current
    Surface(
        color = colors.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Your Exact Age", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textSecondary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AgeUnitBadge(value = ageResult.years.toString(), label = "Years")
                AgeUnitBadge(value = ageResult.months.toString(), label = "Months")
                AgeUnitBadge(value = ageResult.days.toString(), label = "Days")
            }
        }
    }
}

@Composable
private fun NextBirthdayCard(ageResult: AgeResult) {
    val colors = LocalCalculatorColors.current
    Surface(
        color = colors.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Next Birthday In", fontSize = 13.sp, color = colors.textSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${ageResult.nextBirthdayMonths} months, ${ageResult.nextBirthdayDays} days",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF06B6D4)
                )
            }
            Text(text = "🎂", fontSize = 32.sp)
        }
    }
}

@Composable
private fun LifeSummaryCard(ageResult: AgeResult) {
    val colors = LocalCalculatorColors.current
    val numberingSystem = LocalNumberingSystem.current
    Surface(
        color = colors.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Life Summary", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
            StatRow(label = "Day of week born", value = ageResult.dayOfWeekBorn)
            StatRow(label = "Total Days", value = NumberFormatter.formatInteger(ageResult.totalDays, numberingSystem))
            StatRow(label = "Total Weeks", value = NumberFormatter.formatInteger(ageResult.totalWeeks, numberingSystem))
            StatRow(label = "Total Hours", value = NumberFormatter.formatInteger(ageResult.totalHours, numberingSystem))
        }
    }
}

@Composable
private fun AgeUnitBadge(value: String, label: String) {
    val colors = LocalCalculatorColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            color = colors.operatorKeyBg.copy(alpha = 0.35f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, colors.operatorKeyText.copy(alpha = 0.4f)),
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.operatorKeyText
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    val colors = LocalCalculatorColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = colors.textSecondary)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
    }
}

@Composable
private fun SimpleInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Decimal,
    suffix: String? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showActionPopup by remember { mutableStateOf(false) }
    var actionPopupOffset by remember { mutableStateOf(Offset.Zero) }

    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Surface(
            color = colors.background.copy(alpha = 0.6f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.2f)),
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { offset ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            actionPopupOffset = offset
                            showActionPopup = true
                        }
                    )
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) {
                            onValueChange(input)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    ),
                    cursorBrush = SolidColor(colors.operatorKeyText),
                    modifier = Modifier.weight(1f)
                )
                if (suffix != null) {
                    Text(
                        text = suffix,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.operatorKeyText,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                if (value.isNotEmpty()) {
                    IconButton(
                        onClick = { onValueChange("") },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                } else if (ClipboardHelper.hasClipboardText(context)) {
                    IconButton(
                        onClick = {
                            val clip = ClipboardHelper.getClipboardText(context)
                            if (clip != null) {
                                val sanitized = ClipboardHelper.sanitizeDecimalPaste(clip)
                                if (sanitized.isNotEmpty()) onValueChange(sanitized)
                            }
                        },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste",
                            tint = colors.operatorKeyText,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        val hasClipboard = remember(showActionPopup) {
            if (showActionPopup) ClipboardHelper.hasClipboardText(context) else false
        }
        CutCopyPastePopup(
            visible = showActionPopup,
            onDismissRequest = { showActionPopup = false },
            anchorOffset = actionPopupOffset,
            onCut = if (value.isNotEmpty()) {
                {
                    ClipboardHelper.cut(context, value)
                    onValueChange("")
                    showActionPopup = false
                }
            } else null,
            onCopy = if (value.isNotEmpty()) {
                {
                    ClipboardHelper.copy(context, value)
                    showActionPopup = false
                }
            } else null,
            onPaste = if (hasClipboard) {
                {
                    val clip = ClipboardHelper.getClipboardText(context)
                    if (clip != null) {
                        val sanitized = ClipboardHelper.sanitizeDecimalPaste(clip)
                        if (sanitized.isNotEmpty()) onValueChange(sanitized)
                    }
                    showActionPopup = false
                }
            } else null
        )
    }
}
