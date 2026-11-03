package com.calculator.app.ui.screens.tools

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calculator.app.data.preferences.LocalNumberingSystem
import com.calculator.app.domain.format.NumberFormatter
import com.calculator.app.domain.tools.DiscountResult
import com.calculator.app.ui.components.ClipboardHelper
import com.calculator.app.ui.components.CutCopyPastePopup
import com.calculator.app.domain.tools.FinanceEngine
import com.calculator.app.domain.tools.GstResult
import com.calculator.app.domain.tools.LoanEmiResult
import com.calculator.app.domain.tools.SipResult
import com.calculator.app.ui.theme.LocalCalculatorColors

@Composable
private fun formatFinance(value: Double): String {
    return NumberFormatter.formatCurrency(value, LocalNumberingSystem.current)
}

// -------------------------------------------------------------
// 1. LOAN & EMI CALCULATOR
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanEmiScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var principalStr by remember { mutableStateOf("100000") }
    var rateStr by remember { mutableStateOf("10.5") }
    var yearsStr by remember { mutableStateOf("3") }
    var monthsStr by remember { mutableStateOf("0") }

    val principal = principalStr.toDoubleOrNull() ?: 0.0
    val rate = rateStr.toDoubleOrNull() ?: 0.0
    val yearsNum = yearsStr.toIntOrNull() ?: 0
    val monthsNum = monthsStr.toIntOrNull() ?: 0
    val tenureMonths = (yearsNum * 12) + monthsNum

    val result = remember(principal, rate, tenureMonths) {
        FinanceEngine.calculateLoanEmi(principal, rate, tenureMonths)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Loan & EMI Calculator", fontSize = if (isLandscape) 18.sp else 20.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary) },
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
                    LoanEmiInputCard(
                        principalStr = principalStr,
                        onPrincipalChange = { principalStr = it },
                        rateStr = rateStr,
                        onRateChange = { rateStr = it },
                        yearsStr = yearsStr,
                        onYearsChange = { yearsStr = it },
                        monthsStr = monthsStr,
                        onMonthsChange = { monthsStr = it },
                        tenureMonths = tenureMonths
                    )
                }

                VerticalDivider(color = colors.textSecondary.copy(alpha = 0.15f), thickness = 1.dp, modifier = Modifier.fillMaxHeight())

                // Right Column: Results
                Column(
                    modifier = Modifier
                        .weight(0.52f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    LoanEmiResultCard(result = result, principal = principal)
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
                    LoanEmiInputCard(
                        principalStr = principalStr,
                        onPrincipalChange = { principalStr = it },
                        rateStr = rateStr,
                        onRateChange = { rateStr = it },
                        yearsStr = yearsStr,
                        onYearsChange = { yearsStr = it },
                        monthsStr = monthsStr,
                        onMonthsChange = { monthsStr = it },
                        tenureMonths = tenureMonths
                    )
                }
                item {
                    LoanEmiResultCard(result = result, principal = principal)
                }
            }
        }
    }
}

@Composable
private fun LoanEmiInputCard(
    principalStr: String,
    onPrincipalChange: (String) -> Unit,
    rateStr: String,
    onRateChange: (String) -> Unit,
    yearsStr: String,
    onYearsChange: (String) -> Unit,
    monthsStr: String,
    onMonthsChange: (String) -> Unit,
    tenureMonths: Int
) {
    val colors = LocalCalculatorColors.current
    val yearsNum = yearsStr.toIntOrNull() ?: 0
    val monthsNum = monthsStr.toIntOrNull() ?: 0

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
            FinanceInputField(
                label = "Loan Amount (Principal)",
                value = principalStr,
                onValueChange = onPrincipalChange,
                prefix = "₹"
            )

            FinanceInputField(
                label = "Annual Interest Rate (%)",
                value = rateStr,
                onValueChange = onRateChange,
                suffix = "%"
            )

            // Sleek Inner Divider
            HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.12f), thickness = 1.dp)

            // Tenure Section with Years and Months Together
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Loan Tenure (Years & Months)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary
                    )
                    val summaryText = buildString {
                        if (yearsNum > 0) append("$yearsNum yr${if (yearsNum > 1) "s" else ""} ")
                        if (monthsNum > 0) append("$monthsNum mo${if (monthsNum > 1) "s" else ""} ")
                        if (yearsNum == 0 && monthsNum == 0) append("0 months")
                        else append("($tenureMonths mos)")
                    }.trim()
                    Text(
                        text = summaryText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.operatorKeyText
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FinanceInputField(
                        label = "Years",
                        value = yearsStr,
                        onValueChange = onYearsChange,
                        keyboardType = KeyboardType.Number,
                        suffix = "Yr",
                        modifier = Modifier.weight(1f)
                    )
                    FinanceInputField(
                        label = "Months",
                        value = monthsStr,
                        onValueChange = onMonthsChange,
                        keyboardType = KeyboardType.Number,
                        suffix = "Mo",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Quick Tenure Preset Pills
                val tenurePresets = listOf(1, 2, 3, 5, 10, 15, 20, 25, 30)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    items(tenurePresets) { yr ->
                        val isSelected = yearsNum == yr && monthsNum == 0
                        Surface(
                            color = if (isSelected) colors.operatorKeyBg else colors.background.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) colors.operatorKeyText else colors.textSecondary.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onYearsChange(yr.toString())
                                    onMonthsChange("0")
                                }
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)) {
                                Text(
                                    text = "$yr Yr",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) colors.operatorKeyText else colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanEmiResultCard(result: LoanEmiResult, principal: Double) {
    val colors = LocalCalculatorColors.current
    Surface(
        color = colors.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(text = "Monthly EMI", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textSecondary)
            Text(text = formatFinance(result.monthlyEmi), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = colors.operatorKeyText)

            HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.15f))

            FinanceResultRow(label = "Principal Amount", value = formatFinance(principal))
            FinanceResultRow(label = "Total Interest", value = formatFinance(result.totalInterest), valueColor = Color(0xFFEF4444))
            FinanceResultRow(label = "Total Payment (Principal + Interest)", value = formatFinance(result.totalPayment), isBold = true)

            if (result.totalPayment > 0) {
                Column(modifier = Modifier.padding(top = 6.dp)) {
                    Text(text = "Payment Breakdown", fontSize = 12.sp, color = colors.textSecondary, modifier = Modifier.padding(bottom = 6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(result.principalRatio.coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(colors.operatorKeyText)
                        )
                        Box(
                            modifier = Modifier
                                .weight(result.interestRatio.coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(Color(0xFFEF4444))
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Principal: ${(result.principalRatio * 100).toInt()}%", fontSize = 11.sp, color = colors.operatorKeyText)
                        Text(text = "Interest: ${(result.interestRatio * 100).toInt()}%", fontSize = 11.sp, color = Color(0xFFEF4444))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. INVESTMENT & SIP CALCULATOR
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentSipScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var isSipMode by remember { mutableStateOf(true) }
    var investmentStr by remember { mutableStateOf("5000") }
    var rateStr by remember { mutableStateOf("12.0") }
    var yearsStr by remember { mutableStateOf("5") }
    var monthsStr by remember { mutableStateOf("0") }

    val amount = investmentStr.toDoubleOrNull() ?: 0.0
    val rate = rateStr.toDoubleOrNull() ?: 0.0
    val yearsNum = yearsStr.toIntOrNull() ?: 0
    val monthsNum = monthsStr.toIntOrNull() ?: 0
    val totalMonths = (yearsNum * 12) + monthsNum
    val totalYears = yearsNum + (monthsNum / 12.0)

    val result = remember(isSipMode, amount, rate, totalYears) {
        if (isSipMode) {
            FinanceEngine.calculateSip(amount, rate, totalYears)
        } else {
            FinanceEngine.calculateLumpsum(amount, rate, totalYears)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Investment & SIP", fontSize = if (isLandscape) 18.sp else 20.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary) },
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
                    SipModeSelector(isSipMode = isSipMode, onToggle = { isSipMode = it })
                    SipInputCard(
                        isSipMode = isSipMode,
                        investmentStr = investmentStr,
                        onInvestmentChange = { investmentStr = it },
                        rateStr = rateStr,
                        onRateChange = { rateStr = it },
                        yearsStr = yearsStr,
                        onYearsChange = { yearsStr = it },
                        monthsStr = monthsStr,
                        onMonthsChange = { monthsStr = it },
                        totalMonths = totalMonths
                    )
                }

                VerticalDivider(color = colors.textSecondary.copy(alpha = 0.15f), thickness = 1.dp, modifier = Modifier.fillMaxHeight())

                // Right Column: Results
                Column(
                    modifier = Modifier
                        .weight(0.52f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    SipResultCard(result = result)
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
                    SipModeSelector(isSipMode = isSipMode, onToggle = { isSipMode = it })
                }
                item {
                    SipInputCard(
                        isSipMode = isSipMode,
                        investmentStr = investmentStr,
                        onInvestmentChange = { investmentStr = it },
                        rateStr = rateStr,
                        onRateChange = { rateStr = it },
                        yearsStr = yearsStr,
                        onYearsChange = { yearsStr = it },
                        monthsStr = monthsStr,
                        onMonthsChange = { monthsStr = it },
                        totalMonths = totalMonths
                    )
                }
                item {
                    SipResultCard(result = result)
                }
            }
        }
    }
}

@Composable
private fun SipModeSelector(isSipMode: Boolean, onToggle: (Boolean) -> Unit) {
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
            SipSegmentTab(
                title = "Monthly SIP",
                subtitle = "Recurring Monthly",
                isSelected = isSipMode,
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                onClick = { onToggle(true) },
                modifier = Modifier.weight(1f)
            )
            SipSegmentTab(
                title = "Lump Sum",
                subtitle = "One-Time Investment",
                isSelected = !isSipMode,
                icon = Icons.Default.Savings,
                onClick = { onToggle(false) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SipSegmentTab(
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
        label = "sipTabBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) colors.operatorKeyText else colors.textSecondary,
        animationSpec = tween(220),
        label = "sipTabText"
    )
    val subColor by animateColorAsState(
        targetValue = if (isSelected) colors.operatorKeyText.copy(alpha = 0.75f) else colors.textSecondary.copy(alpha = 0.55f),
        animationSpec = tween(220),
        label = "sipTabSubText"
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
private fun SipInputCard(
    isSipMode: Boolean,
    investmentStr: String,
    onInvestmentChange: (String) -> Unit,
    rateStr: String,
    onRateChange: (String) -> Unit,
    yearsStr: String,
    onYearsChange: (String) -> Unit,
    monthsStr: String,
    onMonthsChange: (String) -> Unit,
    totalMonths: Int
) {
    val colors = LocalCalculatorColors.current
    val yearsNum = yearsStr.toIntOrNull() ?: 0
    val monthsNum = monthsStr.toIntOrNull() ?: 0

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
            FinanceInputField(
                label = if (isSipMode) "Monthly Investment" else "Total Investment (Lump Sum)",
                value = investmentStr,
                onValueChange = onInvestmentChange,
                prefix = "₹"
            )

            // Return rate with quick pills
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FinanceInputField(
                    label = "Expected Annual Return Rate (%)",
                    value = rateStr,
                    onValueChange = onRateChange,
                    suffix = "%"
                )

                val returnPresets = listOf("8", "10", "12", "15", "18")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    returnPresets.forEach { preset ->
                        val isSelected = rateStr == preset
                        Surface(
                            color = if (isSelected) colors.operatorKeyBg else colors.background.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) colors.operatorKeyText else colors.textSecondary.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onRateChange(preset) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$preset%",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) colors.operatorKeyText else colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Sleek Inner Divider
            HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.12f), thickness = 1.dp)

            // Time Period Section with Years and Months Together
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Time Period (Years & Months)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary
                    )
                    val summaryText = buildString {
                        if (yearsNum > 0) append("$yearsNum yr${if (yearsNum > 1) "s" else ""} ")
                        if (monthsNum > 0) append("$monthsNum mo${if (monthsNum > 1) "s" else ""} ")
                        if (yearsNum == 0 && monthsNum == 0) append("0 months")
                        else append("($totalMonths mos)")
                    }.trim()
                    Text(
                        text = summaryText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.operatorKeyText
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FinanceInputField(
                        label = "Years",
                        value = yearsStr,
                        onValueChange = onYearsChange,
                        keyboardType = KeyboardType.Number,
                        suffix = "Yr",
                        modifier = Modifier.weight(1f)
                    )
                    FinanceInputField(
                        label = "Months",
                        value = monthsStr,
                        onValueChange = onMonthsChange,
                        keyboardType = KeyboardType.Number,
                        suffix = "Mo",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Quick Period Preset Pills
                val periodPresets = listOf(1, 3, 5, 10, 15, 20, 25)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    items(periodPresets) { yr ->
                        val isSelected = yearsNum == yr && monthsNum == 0
                        Surface(
                            color = if (isSelected) colors.operatorKeyBg else colors.background.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) colors.operatorKeyText else colors.textSecondary.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onYearsChange(yr.toString())
                                    onMonthsChange("0")
                                }
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)) {
                                Text(
                                    text = "$yr Yr",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) colors.operatorKeyText else colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SipResultCard(result: SipResult) {
    val colors = LocalCalculatorColors.current
    Surface(
        color = colors.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(text = "Expected Total Future Value", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textSecondary)
            Text(text = formatFinance(result.totalValue), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))

            HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.15f))

            FinanceResultRow(label = "Total Invested", value = formatFinance(result.totalInvested))
            FinanceResultRow(label = "Estimated Returns (Wealth Gain)", value = formatFinance(result.estimatedReturns), valueColor = Color(0xFF10B981))

            if (result.totalValue > 0) {
                Column(modifier = Modifier.padding(top = 6.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(result.investedRatio.coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(colors.operatorKeyText)
                        )
                        Box(
                            modifier = Modifier
                                .weight(result.returnsRatio.coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(Color(0xFF10B981))
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Invested: ${(result.investedRatio * 100).toInt()}%", fontSize = 11.sp, color = colors.operatorKeyText)
                        Text(text = "Gain: ${(result.returnsRatio * 100).toInt()}%", fontSize = 11.sp, color = Color(0xFF10B981))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. GST CALCULATOR
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GstCalculatorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var amountStr by remember { mutableStateOf("1000") }
    var gstRateStr by remember { mutableStateOf("18") }
    var isAddGst by remember { mutableStateOf(true) }

    val amount = amountStr.toDoubleOrNull() ?: 0.0
    val rate = gstRateStr.toDoubleOrNull() ?: 0.0

    val result = remember(amount, rate, isAddGst) {
        FinanceEngine.calculateGst(amount, rate, isAddGst)
    }

    val standardSlabs = listOf("3", "5", "12", "18", "28")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("GST Calculator", fontSize = if (isLandscape) 18.sp else 20.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary) },
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
                    GstModeToggle(isAddGst = isAddGst, onToggle = { isAddGst = it })
                    GstInputCard(
                        amountStr = amountStr,
                        onAmountChange = { amountStr = it },
                        gstRateStr = gstRateStr,
                        onGstRateChange = { gstRateStr = it },
                        isAddGst = isAddGst,
                        standardSlabs = standardSlabs
                    )
                }

                VerticalDivider(color = colors.textSecondary.copy(alpha = 0.15f), thickness = 1.dp, modifier = Modifier.fillMaxHeight())

                // Right Column: Results
                Column(
                    modifier = Modifier
                        .weight(0.52f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    GstResultCard(result = result, isAddGst = isAddGst, gstRateStr = gstRateStr)
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
                    GstModeToggle(isAddGst = isAddGst, onToggle = { isAddGst = it })
                }
                item {
                    GstInputCard(
                        amountStr = amountStr,
                        onAmountChange = { amountStr = it },
                        gstRateStr = gstRateStr,
                        onGstRateChange = { gstRateStr = it },
                        isAddGst = isAddGst,
                        standardSlabs = standardSlabs
                    )
                }
                item {
                    GstResultCard(result = result, isAddGst = isAddGst, gstRateStr = gstRateStr)
                }
            }
        }
    }
}

@Composable
private fun GstModeToggle(isAddGst: Boolean, onToggle: (Boolean) -> Unit) {
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
            GstSegmentTab(
                title = "Add GST",
                subtitle = "Exclusive",
                isSelected = isAddGst,
                icon = Icons.Default.Add,
                onClick = { onToggle(true) },
                modifier = Modifier.weight(1f)
            )
            GstSegmentTab(
                title = "Remove GST",
                subtitle = "Inclusive",
                isSelected = !isAddGst,
                icon = Icons.Default.Remove,
                onClick = { onToggle(false) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun GstSegmentTab(
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
        label = "gstTabBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) colors.operatorKeyText else colors.textSecondary,
        animationSpec = tween(220),
        label = "gstTabText"
    )
    val subColor by animateColorAsState(
        targetValue = if (isSelected) colors.operatorKeyText.copy(alpha = 0.75f) else colors.textSecondary.copy(alpha = 0.55f),
        animationSpec = tween(220),
        label = "gstTabSubText"
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
private fun GstInputCard(
    amountStr: String,
    onAmountChange: (String) -> Unit,
    gstRateStr: String,
    onGstRateChange: (String) -> Unit,
    isAddGst: Boolean,
    standardSlabs: List<String>
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Amount Input
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAddGst) "Initial Net Amount" else "Total Gross Amount",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary
                    )
                    Text(
                        text = if (isAddGst) "Without GST" else "With GST",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = colors.operatorKeyText.copy(alpha = 0.8f)
                    )
                }

                Surface(
                    color = colors.background.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "₹",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.operatorKeyText,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        BasicTextField(
                            value = amountStr,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    onAmountChange(input)
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            ),
                            cursorBrush = SolidColor(colors.operatorKeyText),
                            modifier = Modifier.weight(1f)
                        )
                        val sys = LocalNumberingSystem.current
                        val formattedGstBadge = remember(amountStr, sys) {
                            if (amountStr.length >= 4) NumberFormatter.formatNumber(amountStr, sys) else null
                        }
                        if (formattedGstBadge != null) {
                            Text(
                                text = "($formattedGstBadge)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.operatorKeyText.copy(alpha = 0.85f),
                                modifier = Modifier.padding(end = 6.dp)
                            )
                        }
                        if (amountStr.isNotEmpty()) {
                            IconButton(
                                onClick = { onAmountChange("") },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Divider
            HorizontalDivider(
                color = colors.textSecondary.copy(alpha = 0.12f),
                thickness = 1.dp
            )

            // 2. GST Slab Rate Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select GST Slab Rate",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "$gstRateStr% Applied",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.operatorKeyText
                    )
                }

                // 5 Equal-Width Modern Slab Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    standardSlabs.forEach { slab ->
                        val isSelected = gstRateStr == slab
                        Surface(
                            color = if (isSelected) colors.operatorKeyBg else colors.background.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) colors.operatorKeyText else colors.textSecondary.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onGstRateChange(slab) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$slab%",
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) colors.operatorKeyText else colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // 3. Custom GST Rate Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Custom GST Rate",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "Enter any custom %",
                        fontSize = 10.sp,
                        color = colors.textSecondary.copy(alpha = 0.6f)
                    )
                }

                Surface(
                    color = colors.background.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        if (!standardSlabs.contains(gstRateStr) && gstRateStr.isNotEmpty()) colors.operatorKeyText
                        else colors.textSecondary.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.width(96.dp).height(38.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        BasicTextField(
                            value = gstRateStr,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    onGstRateChange(input)
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                textAlign = TextAlign.End
                            ),
                            cursorBrush = SolidColor(colors.operatorKeyText),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.operatorKeyText,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GstResultCard(result: GstResult, isAddGst: Boolean, gstRateStr: String) {
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
            Text(
                text = if (isAddGst) "Total Gross Amount" else "Net Amount",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary
            )
            Text(
                text = formatFinance(if (isAddGst) result.grossAmount else result.netAmount),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = colors.operatorKeyText
            )

            HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.15f))

            FinanceResultRow(label = "Net Price", value = formatFinance(result.netAmount))
            FinanceResultRow(label = "GST Amount ($gstRateStr%)", value = formatFinance(result.gstAmount), valueColor = Color(0xFF10B981))
            FinanceResultRow(label = "CGST (Central GST 50%)", value = formatFinance(result.cgstAmount))
            FinanceResultRow(label = "SGST (State GST 50%)", value = formatFinance(result.sgstAmount))
            FinanceResultRow(label = "Total Gross Amount", value = formatFinance(result.grossAmount), isBold = true)
        }
    }
}

// -------------------------------------------------------------
// 4. DISCOUNT CALCULATOR
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscountCalculatorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var originalPriceStr by remember { mutableStateOf("1000") }
    var discountPercentStr by remember { mutableStateOf("20") }
    var extraDiscountStr by remember { mutableStateOf("0") }

    val originalPrice = originalPriceStr.toDoubleOrNull() ?: 0.0
    val discountPercent = discountPercentStr.toDoubleOrNull() ?: 0.0
    val extraDiscount = extraDiscountStr.toDoubleOrNull() ?: 0.0

    val result = remember(originalPrice, discountPercent, extraDiscount) {
        FinanceEngine.calculateDiscount(originalPrice, discountPercent, extraDiscount)
    }

    val quickDiscounts = listOf("5", "10", "15", "20", "25", "50")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Discount Calculator", fontSize = if (isLandscape) 18.sp else 20.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary) },
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
                        .verticalScroll(rememberScrollState())
                ) {
                    DiscountInputCard(
                        originalPriceStr = originalPriceStr,
                        onOriginalPriceChange = { originalPriceStr = it },
                        discountPercentStr = discountPercentStr,
                        onDiscountChange = { discountPercentStr = it },
                        extraDiscountStr = extraDiscountStr,
                        onExtraDiscountChange = { extraDiscountStr = it },
                        quickDiscounts = quickDiscounts
                    )
                }

                VerticalDivider(color = colors.textSecondary.copy(alpha = 0.15f), thickness = 1.dp, modifier = Modifier.fillMaxHeight())

                // Right Column: Results
                Column(
                    modifier = Modifier
                        .weight(0.52f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    DiscountResultCard(result = result)
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
                    DiscountInputCard(
                        originalPriceStr = originalPriceStr,
                        onOriginalPriceChange = { originalPriceStr = it },
                        discountPercentStr = discountPercentStr,
                        onDiscountChange = { discountPercentStr = it },
                        extraDiscountStr = extraDiscountStr,
                        onExtraDiscountChange = { extraDiscountStr = it },
                        quickDiscounts = quickDiscounts
                    )
                }
                item {
                    DiscountResultCard(result = result)
                }
            }
        }
    }
}

@Composable
private fun DiscountInputCard(
    originalPriceStr: String,
    onOriginalPriceChange: (String) -> Unit,
    discountPercentStr: String,
    onDiscountChange: (String) -> Unit,
    extraDiscountStr: String,
    onExtraDiscountChange: (String) -> Unit,
    quickDiscounts: List<String>
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
            FinanceInputField(
                label = "Original Price",
                value = originalPriceStr,
                onValueChange = onOriginalPriceChange,
                prefix = "₹"
            )

            // Sleek Quick Discount Pills Row
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick Discount Rate",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "$discountPercentStr% Selected",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.operatorKeyText
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickDiscounts.forEach { discount ->
                        val isSelected = discountPercentStr == discount
                        Surface(
                            color = if (isSelected) colors.operatorKeyBg else colors.background.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) colors.operatorKeyText else colors.textSecondary.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onDiscountChange(discount) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$discount%",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) colors.operatorKeyText else colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Sleek Inner Divider
            HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.12f), thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FinanceInputField(
                    label = "Custom Discount",
                    value = discountPercentStr,
                    onValueChange = onDiscountChange,
                    suffix = "%",
                    modifier = Modifier.weight(1f)
                )
                FinanceInputField(
                    label = "Extra Discount",
                    value = extraDiscountStr,
                    onValueChange = onExtraDiscountChange,
                    suffix = "%",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DiscountResultCard(result: DiscountResult) {
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
            Text(text = "Final Price to Pay", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textSecondary)
            Text(text = formatFinance(result.finalPrice), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))

            HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.15f))

            FinanceResultRow(label = "Original Price", value = formatFinance(result.originalPrice))
            FinanceResultRow(label = "You Save", value = formatFinance(result.totalSavings), valueColor = Color(0xFF10B981), isBold = true)
            FinanceResultRow(label = "Effective Discount", value = "${String.format("%.1f", result.effectiveDiscountPercent)}%")
        }
    }
}

// -------------------------------------------------------------
// Helper Components
// -------------------------------------------------------------
@Composable
private fun FinanceInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Decimal,
    prefix: String? = null,
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
                if (prefix != null) {
                    Text(
                        text = prefix,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.operatorKeyText,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
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
                val sys = LocalNumberingSystem.current
                val formattedBadge = remember(value, sys) {
                    if ((prefix == "₹" || prefix?.contains("$") == true) && value.length >= 4) {
                        NumberFormatter.formatNumber(value, sys)
                    } else null
                }
                if (formattedBadge != null) {
                    Text(
                        text = "($formattedBadge)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.operatorKeyText.copy(alpha = 0.85f),
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
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

@Composable
private fun FinanceResultRow(
    label: String,
    value: String,
    valueColor: Color? = null,
    isBold: Boolean = false
) {
    val colors = LocalCalculatorColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isBold) colors.textPrimary else colors.textSecondary
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = valueColor ?: colors.textPrimary
        )
    }
}
