package com.calculator.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.InterceptPlatformTextInput
import kotlinx.coroutines.awaitCancellation

/**
 * Wraps [content] and intercepts text input connection requests so that Android's
 * software keyboard (IME) is never launched, while preserving 100% of native
 * BasicTextField cursor, handle dragging, OS loupe magnifier, and text selection.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NoSoftKeyboard(
    content: @Composable () -> Unit
) {
    InterceptPlatformTextInput(
        interceptor = { _, _ -> awaitCancellation() },
        content = content
    )
}
