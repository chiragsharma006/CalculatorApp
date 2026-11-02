package com.calculator.app.domain.tools

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

object UnitConverterEngine {

    fun getUnitsForCategory(
        category: UnitCategory,
        customCurrencyRates: Map<String, Double>? = null
    ): List<UnitItem> {
        return when (category) {
            UnitCategory.CURRENCY -> {
                val list = listOf(
                    UnitItem("USD", "US Dollar", "$", customCurrencyRates?.get("USD") ?: 1.0),
                    UnitItem("EUR", "Euro", "€", customCurrencyRates?.get("EUR") ?: 0.894),
                    UnitItem("GBP", "British Pound", "£", customCurrencyRates?.get("GBP") ?: 0.749),
                    UnitItem("INR", "Indian Rupee", "₹", customCurrencyRates?.get("INR") ?: 95.27),
                    UnitItem("JPY", "Japanese Yen", "¥", customCurrencyRates?.get("JPY") ?: 155.20),
                    UnitItem("CAD", "Canadian Dollar", "CA$", customCurrencyRates?.get("CAD") ?: 1.37),
                    UnitItem("AUD", "Australian Dollar", "A$", customCurrencyRates?.get("AUD") ?: 1.52),
                    UnitItem("CHF", "Swiss Franc", "CHF", customCurrencyRates?.get("CHF") ?: 0.91),
                    UnitItem("CNY", "Chinese Yuan", "¥", customCurrencyRates?.get("CNY") ?: 7.24),
                    UnitItem("AED", "UAE Dirham", "AED", customCurrencyRates?.get("AED") ?: 3.67),
                    UnitItem("SGD", "Singapore Dollar", "S$", customCurrencyRates?.get("SGD") ?: 1.35),
                    UnitItem("SAR", "Saudi Riyal", "SAR", customCurrencyRates?.get("SAR") ?: 3.75),
                    UnitItem("NZD", "New Zealand Dollar", "NZ$", customCurrencyRates?.get("NZD") ?: 1.68),
                    UnitItem("ZAR", "South African Rand", "R", customCurrencyRates?.get("ZAR") ?: 18.25),
                    UnitItem("BRL", "Brazilian Real", "R$", customCurrencyRates?.get("BRL") ?: 5.45)
                )
                list
            }
            UnitCategory.LENGTH -> listOf(
                UnitItem("m", "Meter", "m", 1.0),
                UnitItem("km", "Kilometer", "km", 1000.0),
                UnitItem("cm", "Centimeter", "cm", 0.01),
                UnitItem("mm", "Millimeter", "mm", 0.001),
                UnitItem("mi", "Mile", "mi", 1609.344),
                UnitItem("yd", "Yard", "yd", 0.9144),
                UnitItem("ft", "Foot", "ft", 0.3048),
                UnitItem("in", "Inch", "in", 0.0254),
                UnitItem("nmi", "Nautical Mile", "NM", 1852.0)
            )
            UnitCategory.AREA -> listOf(
                UnitItem("sq_m", "Square Meter", "m²", 1.0),
                UnitItem("sq_km", "Square Kilometer", "km²", 1000000.0),
                UnitItem("sq_cm", "Square Centimeter", "cm²", 0.0001),
                UnitItem("sq_mm", "Square Millimeter", "mm²", 0.000001),
                UnitItem("sq_mi", "Square Mile", "mi²", 2589988.11),
                UnitItem("sq_yd", "Square Yard", "yd²", 0.836127),
                UnitItem("sq_ft", "Square Foot", "ft²", 0.092903),
                UnitItem("sq_in", "Square Inch", "in²", 0.00064516),
                UnitItem("hectare", "Hectare", "ha", 10000.0),
                UnitItem("acre", "Acre", "ac", 4046.856)
            )
            UnitCategory.VOLUME -> listOf(
                UnitItem("l", "Liter", "L", 1.0),
                UnitItem("ml", "Milliliter", "mL", 0.001),
                UnitItem("cu_m", "Cubic Meter", "m³", 1000.0),
                UnitItem("cu_cm", "Cubic Centimeter", "cm³", 0.001),
                UnitItem("gal_us", "US Gallon", "gal", 3.78541178),
                UnitItem("qt_us", "US Quart", "qt", 0.946353),
                UnitItem("pt_us", "US Pint", "pt", 0.473176),
                UnitItem("cup_us", "US Cup", "cup", 0.236588),
                UnitItem("fl_oz_us", "US Fluid Ounce", "fl oz", 0.0295735),
                UnitItem("gal_uk", "Imperial Gallon", "imp gal", 4.54609)
            )
            UnitCategory.WEIGHT -> listOf(
                UnitItem("kg", "Kilogram", "kg", 1.0),
                UnitItem("g", "Gram", "g", 0.001),
                UnitItem("mg", "Milligram", "mg", 0.000001),
                UnitItem("ton", "Metric Ton", "t", 1000.0),
                UnitItem("lb", "Pound", "lb", 0.45359237),
                UnitItem("oz", "Ounce", "oz", 0.02834952),
                UnitItem("stone", "Stone", "st", 6.35029318),
                UnitItem("carat", "Carat", "ct", 0.0002)
            )
            UnitCategory.TEMPERATURE -> listOf(
                UnitItem("c", "Celsius", "°C", 1.0),
                UnitItem("f", "Fahrenheit", "°F", 1.0),
                UnitItem("k", "Kelvin", "K", 1.0)
            )
            UnitCategory.SPEED -> listOf(
                UnitItem("m_s", "Meter per second", "m/s", 1.0),
                UnitItem("km_h", "Kilometer per hour", "km/h", 1.0 / 3.6),
                UnitItem("mph", "Miles per hour", "mph", 0.44704),
                UnitItem("knot", "Knot", "kn", 0.514444),
                UnitItem("ft_s", "Feet per second", "ft/s", 0.3048)
            )
            UnitCategory.PRESSURE -> listOf(
                UnitItem("pa", "Pascal", "Pa", 1.0),
                UnitItem("kpa", "Kilopascal", "kPa", 1000.0),
                UnitItem("bar", "Bar", "bar", 100000.0),
                UnitItem("psi", "Pounds per sq inch", "psi", 6894.757),
                UnitItem("atm", "Standard Atmosphere", "atm", 101325.0),
                UnitItem("torr", "Torr / mmHg", "mmHg", 133.322)
            )
            UnitCategory.POWER -> listOf(
                UnitItem("w", "Watt", "W", 1.0),
                UnitItem("kw", "Kilowatt", "kW", 1000.0),
                UnitItem("mw", "Megawatt", "MW", 1000000.0),
                UnitItem("hp", "Horsepower (hp)", "hp", 745.699872),
                UnitItem("hp_metric", "Horsepower (metric)", "PS", 735.49875),
                UnitItem("btu_h", "BTU per hour", "BTU/h", 0.29307107),
                UnitItem("ft_lb_s", "Foot-pound/sec", "ft·lb/s", 1.355818)
            )
            UnitCategory.DATA_STORAGE -> listOf(
                UnitItem("b", "Bit", "b", 0.125),
                UnitItem("B", "Byte", "B", 1.0),
                UnitItem("kb", "Kilobit (kb)", "kb", 125.0),
                UnitItem("KB", "Kilobyte (KB)", "KB", 1000.0),
                UnitItem("KiB", "Kibibyte (binary)", "KiB", 1024.0),
                UnitItem("mb", "Megabit (mb / Mb)", "Mb", 125000.0),
                UnitItem("MB", "Megabyte (MB)", "MB", 1000000.0),
                UnitItem("MiB", "Mebibyte (binary)", "MiB", 1048576.0),
                UnitItem("gb", "Gigabit (gb / Gb)", "Gb", 125000000.0),
                UnitItem("GB", "Gigabyte (GB)", "GB", 1000000000.0),
                UnitItem("GiB", "Gibibyte (binary)", "GiB", 1073741824.0),
                UnitItem("tb", "Terabit (tb / Tb)", "Tb", 125000000000.0),
                UnitItem("TB", "Terabyte (TB)", "TB", 1000000000000.0),
                UnitItem("TiB", "Tebibyte (binary)", "TiB", 1099511627776.0),
                UnitItem("pb", "Petabit (pb / Pb)", "Pb", 125000000000000.0),
                UnitItem("PB", "Petabyte (PB)", "PB", 1000000000000000.0),
                UnitItem("PiB", "Pebibyte (binary)", "PiB", 1125899906842624.0)
            )
            UnitCategory.DATA_SPEED -> listOf(
                UnitItem("bps", "Bit per second", "bps", 1.0),
                UnitItem("b_s", "Byte per second", "B/s", 8.0),
                UnitItem("kbps", "Kilobit per sec (kbps)", "Kbps", 1000.0),
                UnitItem("kb_s", "Kilobyte per sec (KB/s)", "KB/s", 8000.0),
                UnitItem("kibps", "Kibibit per sec", "Kibps", 1024.0),
                UnitItem("kib_s", "Kibibyte per sec", "KiB/s", 8192.0),
                UnitItem("mbps", "Megabit per sec (mbps)", "Mbps", 1000000.0),
                UnitItem("mb_s", "Megabyte per sec (MB/s)", "MB/s", 8000000.0),
                UnitItem("mibps", "Mebibit per sec", "Mibps", 1048576.0),
                UnitItem("mib_s", "Mebibyte per sec", "MiB/s", 8388608.0),
                UnitItem("gbps", "Gigabit per sec (Gbps)", "Gbps", 1000000000.0),
                UnitItem("gb_s", "Gigabyte per sec (GB/s)", "GB/s", 8000000000.0),
                UnitItem("tbps", "Terabit per sec (Tbps)", "Tbps", 1000000000000.0),
                UnitItem("tb_s", "Terabyte per sec (TB/s)", "TB/s", 8000000000000.0)
            )
        }
    }

    fun convert(
        value: Double,
        from: UnitItem,
        to: UnitItem,
        category: UnitCategory
    ): Double {
        if (from.id == to.id) return value

        if (category == UnitCategory.TEMPERATURE) {
            return convertTemperature(value, from.id, to.id)
        }

        if (category == UnitCategory.CURRENCY) {
            // For currency: ratioToBase is relative to USD (1 USD = X Currency)
            // Value in USD = value / from.ratioToBase
            // Target value = (value / from.ratioToBase) * to.ratioToBase
            val usd = value / from.ratioToBase
            return usd * to.ratioToBase
        }

        // Standard units: ratioToBase converts unit to standard SI base unit
        val baseValue = value * from.ratioToBase
        return baseValue / to.ratioToBase
    }

    private fun convertTemperature(value: Double, fromId: String, toId: String): Double {
        // Convert to Celsius first
        val celsius = when (fromId) {
            "c" -> value
            "f" -> (value - 32.0) * (5.0 / 9.0)
            "k" -> value - 273.15
            else -> value
        }

        // Convert Celsius to target
        return when (toId) {
            "c" -> celsius
            "f" -> (celsius * 9.0 / 5.0) + 32.0
            "k" -> celsius + 273.15
            else -> celsius
        }
    }

    fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "0"
        if (value == 0.0) return "0"

        val absVal = Math.abs(value)
        return when {
            absVal >= 1e9 || (absVal < 1e-4 && absVal > 0) -> {
                val df = DecimalFormat("0.######E0")
                df.format(value)
            }
            absVal >= 1000 -> {
                val bd = BigDecimal(value).setScale(4, RoundingMode.HALF_UP).stripTrailingZeros()
                bd.toPlainString()
            }
            else -> {
                val bd = BigDecimal(value).setScale(6, RoundingMode.HALF_UP).stripTrailingZeros()
                bd.toPlainString()
            }
        }
    }
}
