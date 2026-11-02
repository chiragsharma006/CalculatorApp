package com.calculator.app.ui.screens.tools

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calculator.app.data.preferences.LocalNumberingSystem
import com.calculator.app.data.preferences.NumberingSystem
import com.calculator.app.domain.tools.UnitCategory
import com.calculator.app.ui.components.NumberFormatDialog
import com.calculator.app.ui.navigation.AppScreen
import com.calculator.app.ui.theme.LocalCalculatorColors

data class ToolItem(
    val title: String,
    val icon: ImageVector,
    val iconTint: Color,
    val screen: AppScreen? = null,
    val section: String,
    val keywords: String = "",
    val onClick: (() -> Unit)? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsHubScreen(
    onNavigate: (AppScreen) -> Unit,
    onBack: () -> Unit,
    numberingSystem: NumberingSystem = LocalNumberingSystem.current,
    onSelectNumberingSystem: (NumberingSystem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var searchQuery by remember { mutableStateOf("") }

    val allTools = remember {
        listOf(
            // Unit Converters
            ToolItem("Currency", Icons.Default.AttachMoney, Color(0xFF10B981), AppScreen.UnitConverter(UnitCategory.CURRENCY), "Unit Converters", "usd eur inr gbp exchange forex money rates"),
            ToolItem("Length", Icons.Default.Straighten, Color(0xFF3B82F6), AppScreen.UnitConverter(UnitCategory.LENGTH), "Unit Converters", "meters feet miles inches cm km millimeter distance"),
            ToolItem("Area", Icons.Default.SquareFoot, Color(0xFF8B5CF6), AppScreen.UnitConverter(UnitCategory.AREA), "Unit Converters", "sq meters acres hectares sq ft square plot land"),
            ToolItem("Volume", Icons.Default.WaterDrop, Color(0xFF06B6D4), AppScreen.UnitConverter(UnitCategory.VOLUME), "Unit Converters", "liters gallons ml cups cubic liquid capacity"),
            ToolItem("Weight", Icons.Default.Scale, Color(0xFFF59E0B), AppScreen.UnitConverter(UnitCategory.WEIGHT), "Unit Converters", "kilograms pounds grams ounces kg lbs mass"),
            ToolItem("Temperature", Icons.Default.Thermostat, Color(0xFFEF4444), AppScreen.UnitConverter(UnitCategory.TEMPERATURE), "Unit Converters", "celsius fahrenheit kelvin heat weather temp"),
            ToolItem("Speed", Icons.Default.Speed, Color(0xFF6366F1), AppScreen.UnitConverter(UnitCategory.SPEED), "Unit Converters", "km/h mph m/s knots velocity"),
            ToolItem("Pressure", Icons.Default.Compress, Color(0xFFEC4899), AppScreen.UnitConverter(UnitCategory.PRESSURE), "Unit Converters", "pascal bar psi atm mmhg atmosphere"),
            ToolItem("Power", Icons.Default.Bolt, Color(0xFFEAB308), AppScreen.UnitConverter(UnitCategory.POWER), "Unit Converters", "watts kw horsepower btu energy watt"),
            ToolItem("Data Storage", Icons.Default.Storage, Color(0xFF14B8A6), AppScreen.UnitConverter(UnitCategory.DATA_STORAGE), "Unit Converters", "data storage mb MB bytes bits gigabyte megabyte kb gb tb pb memory convert"),
            ToolItem("Data Speed", Icons.Default.NetworkCheck, Color(0xFF0EA5E9), AppScreen.UnitConverter(UnitCategory.DATA_SPEED), "Unit Converters", "data speed transfer rate mbps mb/s kbps gbps bandwidth internet speed download upload"),

            // Finance & Tax
            ToolItem("Loan", Icons.Default.AccountBalance, Color(0xFF3B82F6), AppScreen.LoanEmi, "Finance & Tax", "emi interest mortgage monthly payment finance bank"),
            ToolItem("Investment", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF10B981), AppScreen.InvestmentSip, "Finance & Tax", "sip mutual funds compound interest wealth return stocks"),
            ToolItem("GST", Icons.AutoMirrored.Filled.ReceiptLong, Color(0xFF8B5CF6), AppScreen.Gst, "Finance & Tax", "tax goods services slab cgst sgst vat inclusive exclusive"),
            ToolItem("Discount", Icons.Default.LocalOffer, Color(0xFFF97316), AppScreen.Discount, "Finance & Tax", "sale price money saved offer coupon percentage off"),

            // Date & Time
            ToolItem("Date", Icons.Default.CalendarMonth, Color(0xFF3B82F6), AppScreen.DateCalculator, "Date & Time", "date calculator difference add subtract days weeks months years duration calendar"),
            ToolItem("Time", Icons.Default.Schedule, Color(0xFFF59E0B), AppScreen.TimeCalculator, "Date & Time", "time calculator difference add subtract hours minutes seconds duration clock stopwatch"),
            ToolItem("Age", Icons.Default.Cake, Color(0xFF06B6D4), AppScreen.Age, "Date & Time", "birthday date days lived countdown years months age"),

            // Health & Utilities
            ToolItem("BMI", Icons.Default.Favorite, Color(0xFFEC4899), AppScreen.Bmi, "Health & Utilities", "body mass index ideal weight fitness health calculator"),

            // Programmer
            ToolItem("Number Base", Icons.Default.Code, Color(0xFF8B5CF6), AppScreen.NumberSystem, "Programmer", "bin oct dec hex converter binary hexadecimal decimal octal number base system programmer")
        )
    }

    val filteredTools = remember(searchQuery) {
        if (searchQuery.isBlank()) allTools
        else allTools.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.keywords.contains(searchQuery, ignoreCase = true) ||
            it.section.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Converters & Tools",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
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
            // Search Input Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = {
                    Text(
                        text = "Search converters & calculators...",
                        color = colors.textSecondary.copy(alpha = 0.7f),
                        fontSize = 15.sp
                    )
                },
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = colors.textSecondary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = colors.textSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.operatorKeyText,
                    unfocusedBorderColor = colors.textSecondary.copy(alpha = 0.25f),
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                )
            )

            if (searchQuery.isNotBlank()) {
                // Flat grid of search results
                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (isLandscape) 4 else 2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTools) { tool ->
                        ToolCard(
                            tool = tool,
                            onClick = { tool.onClick?.invoke() ?: tool.screen?.let { onNavigate(it) } }
                        )
                    }
                }
            } else {
                // Grouped by section
                val sections = remember { listOf("Unit Converters", "Finance & Tax", "Date & Time", "Health & Utilities", "Programmer") }

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    sections.forEach { sectionName ->
                        val sectionTools = allTools.filter { it.section == sectionName }
                        if (sectionTools.isNotEmpty()) {
                            item {
                                Text(
                                    text = sectionName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(start = 20.dp, top = 18.dp, bottom = 8.dp)
                                )
                            }
                            item {
                                // Responsive grid inside section
                                val chunkCount = if (isLandscape) 4 else 2
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val rows = sectionTools.chunked(chunkCount)
                                    rows.forEach { rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            rowItems.forEach { tool ->
                                                Box(modifier = Modifier.weight(1f)) {
                                                    ToolCard(
                                                        tool = tool,
                                                        onClick = { tool.onClick?.invoke() ?: tool.screen?.let { onNavigate(it) } }
                                                    )
                                                }
                                            }
                                            val emptySlots = chunkCount - rowItems.size
                                            repeat(emptySlots) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolCard(
    tool: ToolItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    var textFontSize by remember { mutableStateOf(15.sp) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.12f)),
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tool.iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = null,
                    tint = tool.iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = tool.title,
                fontSize = textFontSize,
                lineHeight = (textFontSize.value * 1.25f).sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { result ->
                    if (result.hasVisualOverflow && textFontSize > 13.sp) {
                        textFontSize = (textFontSize.value - 0.5f).sp
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
