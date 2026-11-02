package com.calculator.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

data class CurrencyRateState(
    val rates: Map<String, Double>,
    val isLive: Boolean = false,
    val isLoading: Boolean = false,
    val lastUpdatedText: String = "",
    val errorMessage: String? = null
)

class CurrencyRateRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("currency_rates_cache", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_DATE = "last_fetched_date"
        private const val KEY_LAST_TIME_TEXT = "last_updated_time_text"
        private const val KEY_RATES_PREFIX = "rate_"

        val SUPPORTED_CURRENCIES = listOf(
            "USD", "EUR", "GBP", "INR", "JPY", "CAD", "AUD", "CHF", "CNY", "AED", "SGD", "SAR", "NZD", "ZAR", "BRL"
        )

        // Baseline reference rates if completely offline on first launch
        val DEFAULT_RATES = mapOf(
            "USD" to 1.0,
            "EUR" to 0.894,
            "GBP" to 0.749,
            "INR" to 95.27,
            "JPY" to 155.20,
            "CAD" to 1.37,
            "AUD" to 1.52,
            "CHF" to 0.91,
            "CNY" to 7.24,
            "AED" to 3.67,
            "SGD" to 1.35,
            "SAR" to 3.75,
            "NZD" to 1.68,
            "ZAR" to 18.25,
            "BRL" to 5.45
        )

        private const val PRIMARY_API_URL = "https://open.er-api.com/v6/latest/USD"
        private const val FALLBACK_API_URL = "https://api.exchangerate-api.com/v4/latest/USD"
    }

    fun getCachedState(): CurrencyRateState {
        val cachedRates = mutableMapOf<String, Double>()
        for (code in SUPPORTED_CURRENCIES) {
            val key = KEY_RATES_PREFIX + code
            if (prefs.contains(key)) {
                cachedRates[code] = prefs.getFloat(key, 1.0f).toDouble()
            } else {
                cachedRates[code] = DEFAULT_RATES[code] ?: 1.0
            }
        }

        val lastDate = prefs.getString(KEY_LAST_DATE, null)
        val lastTime = prefs.getString(KEY_LAST_TIME_TEXT, "Offline rates") ?: "Offline rates"
        val isToday = lastDate == LocalDate.now().toString()

        return CurrencyRateState(
            rates = cachedRates,
            isLive = isToday,
            isLoading = false,
            lastUpdatedText = lastTime
        )
    }

    suspend fun getRates(forceRefresh: Boolean = false): CurrencyRateState = withContext(Dispatchers.IO) {
        val today = LocalDate.now().toString()
        val lastDate = prefs.getString(KEY_LAST_DATE, null)

        // If not force refresh and already fetched today, return cache instantly
        if (!forceRefresh && lastDate == today) {
            val cached = getCachedState()
            return@withContext cached.copy(isLive = true)
        }

        // Fetch live rates from the internet
        try {
            val rates = fetchFromNetwork(PRIMARY_API_URL) ?: fetchFromNetwork(FALLBACK_API_URL)

            if (rates != null && rates.isNotEmpty()) {
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                val timeText = "Updated today, ${timeFormat.format(Date())}"

                // Save to SharedPreferences
                val editor = prefs.edit()
                editor.putString(KEY_LAST_DATE, today)
                editor.putString(KEY_LAST_TIME_TEXT, timeText)
                rates.forEach { (code, rate) ->
                    editor.putFloat(KEY_RATES_PREFIX + code, rate.toFloat())
                }
                editor.apply()

                return@withContext CurrencyRateState(
                    rates = rates,
                    isLive = true,
                    isLoading = false,
                    lastUpdatedText = timeText
                )
            }
        } catch (e: Exception) {
            // Network failed, fallback to cache
        }

        val cached = getCachedState()
        return@withContext cached.copy(
            isLive = false,
            isLoading = false,
            errorMessage = "Could not reach live exchange server. Using saved rates."
        )
    }

    private fun fetchFromNetwork(urlString: String): Map<String, Double>? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 6000
            connection.readTimeout = 6000
            connection.setRequestProperty("Accept", "application/json")
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val json = JSONObject(response)
                val ratesObj = json.getJSONObject("rates")

                val result = mutableMapOf<String, Double>()
                for (code in SUPPORTED_CURRENCIES) {
                    if (ratesObj.has(code)) {
                        result[code] = ratesObj.getDouble(code)
                    } else {
                        result[code] = DEFAULT_RATES[code] ?: 1.0
                    }
                }
                result
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }
}
