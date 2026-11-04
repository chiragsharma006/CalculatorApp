package com.calculator.app.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import com.calculator.app.domain.tools.NumberBase

object ClipboardHelper {

    /**
     * Copies the given [text] to the system clipboard.
     * Shows a toast on API < 33 (Android 13+ automatically displays its own clipboard preview).
     */
    fun copy(context: Context, text: String, label: String = "Calculator", showToast: Boolean = true) {
        if (text.isEmpty()) return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)

        if (showToast && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Copies the given [text] to the system clipboard as a cut operation.
     */
    fun cut(context: Context, text: String, label: String = "Calculator", showToast: Boolean = true) {
        if (text.isEmpty()) return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)

        if (showToast && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(context, "Cut to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Retrieves the text currently stored in the system clipboard, or null if none.
     */
    fun getClipboardText(context: Context): String? {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return null
        val clip = clipboard.primaryClip ?: return null
        if (clip.itemCount > 0) {
            val text = clip.getItemAt(0)?.coerceToText(context)?.toString()
            return if (!text.isNullOrBlank()) text else null
        }
        return null
    }

    /**
     * Checks whether the system clipboard currently contains non-empty text.
     */
    fun hasClipboardText(context: Context): Boolean {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return false
        val clip = clipboard.primaryClip ?: return false
        if (clip.itemCount > 0) {
            val text = clip.getItemAt(0)?.coerceToText(context)?.toString()
            return !text.isNullOrBlank()
        }
        return false
    }

    /**
     * Sanitizes raw clipboard text for pasting into the main calculator:
     * - Replaces `*` with `×`, `/` with `÷`
     * - Replaces typographic dashes with standard `-`
     * - Strips thousands separator commas (e.g. `1,000` -> `1000`)
     * - Retains only mathematical characters, operators, and functions.
     */
    fun sanitizeCalculatorPaste(input: String): String {
        var s = input.trim()
        // Replace newlines and extra spaces
        s = s.replace(Regex("[\\r\\n\\t]"), " ")
        // Normalize arithmetic operators
        s = s.replace('*', '×')
        s = s.replace('/', '÷')
        s = s.replace('−', '-') // Unicode minus
        s = s.replace('–', '-') // En-dash
        s = s.replace('—', '-') // Em-dash
        s = s.replace("sqrt", "√", ignoreCase = true)
        s = s.replace("pi", "π", ignoreCase = true)

        // Remove thousands commas between digits (e.g. "1,234,567.89" -> "1234567.89")
        s = s.replace(Regex("(?<=\\d),(?=\\d)"), "")

        // Collapse stray whitespace between adjacent digits (e.g. "5 9" -> "59")
        s = s.replace(Regex("(?<=\\d)\\s+(?=\\d)"), "")

        // Retain only valid characters for expression building
        val allowedChars = "0123456789+-×÷%^!().πe√sincotanloglndegra "
        val filtered = buildString {
            for (ch in s) {
                if (allowedChars.contains(ch, ignoreCase = true)) {
                    append(ch)
                }
            }
        }
        return filtered.trim()
    }

    /**
     * Sanitizes raw clipboard text for decimal numeric fields (e.g. Unit Converter, Finance, Health):
     * - Strips currency symbols, formatting commas, letters
     * - Retains digits and at most one decimal point.
     */
    fun sanitizeDecimalPaste(input: String): String {
        val s = input.trim().replace(",", "")
        val sb = StringBuilder()
        var hasDot = false
        for (ch in s) {
            if (ch.isDigit()) {
                sb.append(ch)
            } else if (ch == '.' && !hasDot) {
                sb.append(ch)
                hasDot = true
            }
        }
        return sb.toString()
    }

    /**
     * Sanitizes raw clipboard text for number system conversions based on [base]:
     * - Binary: only `0` and `1`
     * - Octal: `0` through `7`
     * - Decimal: digits `0` through `9`
     * - Hexadecimal: `0-9`, `A-F` (converted to uppercase)
     */
    fun sanitizeBasePaste(input: String, base: NumberBase): String {
        val s = input.trim().replace(" ", "").replace("_", "")
        return when (base) {
            NumberBase.BINARY -> s.filter { it == '0' || it == '1' }
            NumberBase.OCTAL -> s.filter { it in '0'..'7' }
            NumberBase.DECIMAL -> s.filter { it.isDigit() }
            NumberBase.HEXADECIMAL -> s.filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }.uppercase()
        }
    }
}
