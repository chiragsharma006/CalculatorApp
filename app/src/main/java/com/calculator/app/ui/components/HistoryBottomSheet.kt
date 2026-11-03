package com.calculator.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calculator.app.R
import com.calculator.app.data.local.CalculationRecord
import com.calculator.app.data.preferences.NumberingSystem
import com.calculator.app.domain.format.NumberFormatter
import com.calculator.app.ui.CalculatorAction
import com.calculator.app.ui.theme.LocalCalculatorColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryBottomSheet(
    historyList: List<CalculationRecord>,
    onAction: (CalculatorAction) -> Unit,
    onDismiss: () -> Unit,
    numberingSystem: NumberingSystem = NumberingSystem.INTERNATIONAL
) {
    val colors = LocalCalculatorColors.current
    var showConfirmClearDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.textSecondary.copy(alpha = 0.5f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = colors.textPrimary
                    )
                    Text(
                        text = stringResource(R.string.history_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                Row {
                    if (historyList.isNotEmpty()) {
                        IconButton(onClick = { showConfirmClearDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.clear_history),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textSecondary
                        )
                    }
                }
            }

            HorizontalDivider(
                color = colors.textSecondary.copy(alpha = 0.15f),
                thickness = 1.dp
            )

            // Content
            if (historyList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = colors.textSecondary.copy(alpha = 0.35f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = stringResource(R.string.no_history),
                            fontSize = 16.sp,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(historyList, key = { it.id }) { record ->
                        HistoryItemRow(
                            record = record,
                            numberingSystem = numberingSystem,
                            onExpressionClick = {
                                onAction(CalculatorAction.HistoryItemClicked(record, reuseResult = false))
                            },
                            onResultClick = {
                                onAction(CalculatorAction.HistoryItemClicked(record, reuseResult = true))
                            },
                            onDeleteClick = {
                                onAction(CalculatorAction.DeleteHistoryItem(record))
                            }
                        )
                    }
                }
            }
        }
    }

    if (showConfirmClearDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmClearDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.clear_history),
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.clear_history_confirmation),
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAction(CalculatorAction.ClearAllHistory)
                        showConfirmClearDialog = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.clear),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearDialog = false }) {
                    Text(
                        text = stringResource(R.string.cancel),
                        color = colors.textSecondary
                    )
                }
            },
            containerColor = colors.surface
        )
    }
}

@Composable
private fun HistoryItemRow(
    record: CalculationRecord,
    numberingSystem: NumberingSystem,
    onExpressionClick: () -> Unit,
    onResultClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val colors = LocalCalculatorColors.current
    val formattedDate = remember(record.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.getDefault())
        sdf.format(Date(record.timestamp))
    }
    val formattedExpression = remember(record.expression, numberingSystem) {
        NumberFormatter.formatExpression(record.expression, numberingSystem).formattedText
    }
    val formattedResult = remember(record.result, numberingSystem) {
        NumberFormatter.formatNumber(record.result, numberingSystem)
    }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = colors.functionKeyBg.copy(alpha = 0.5f)
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    fontSize = 12.sp,
                    color = colors.textSecondary.copy(alpha = 0.8f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            ClipboardHelper.copy(context, "${record.expression} = ${record.result}", "Equation")
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy equation",
                            tint = colors.textSecondary.copy(alpha = 0.7f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete entry",
                            tint = colors.textSecondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Expression (clickable to load, long-click to copy)
            Text(
                text = formattedExpression,
                fontSize = 18.sp,
                color = colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(record.expression) {
                        detectTapGestures(
                            onTap = { onExpressionClick() },
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                ClipboardHelper.copy(context, record.expression, "Expression")
                            }
                        )
                    }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Result (clickable to insert, long-click to copy)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(record.result) {
                        detectTapGestures(
                            onTap = { onResultClick() },
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                ClipboardHelper.copy(context, record.result, "Result")
                            }
                        )
                    },
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "= $formattedResult",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.operatorKeyText,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
