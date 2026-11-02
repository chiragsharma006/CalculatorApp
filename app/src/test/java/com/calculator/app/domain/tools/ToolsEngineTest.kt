package com.calculator.app.domain.tools

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ToolsEngineTest {

    @Test
    fun testUnitConverterLength() {
        val meter = UnitItem("m", "Meter", "m", 1.0)
        val km = UnitItem("km", "Kilometer", "km", 1000.0)

        val result = UnitConverterEngine.convert(1500.0, meter, km, UnitCategory.LENGTH)
        assertEquals(1.5, result, 0.0001)

        val ft = UnitItem("ft", "Foot", "ft", 0.3048)
        val inUnit = UnitItem("in", "Inch", "in", 0.0254)
        val inchesInOneFoot = UnitConverterEngine.convert(1.0, ft, inUnit, UnitCategory.LENGTH)
        assertEquals(12.0, inchesInOneFoot, 0.0001)
    }

    @Test
    fun testUnitConverterTemperature() {
        val c = UnitItem("c", "Celsius", "°C", 1.0)
        val f = UnitItem("f", "Fahrenheit", "°F", 1.0)
        val k = UnitItem("k", "Kelvin", "K", 1.0)

        // 100 C -> 212 F
        val fVal = UnitConverterEngine.convert(100.0, c, f, UnitCategory.TEMPERATURE)
        assertEquals(212.0, fVal, 0.001)

        // 0 C -> 273.15 K
        val kVal = UnitConverterEngine.convert(0.0, c, k, UnitCategory.TEMPERATURE)
        assertEquals(273.15, kVal, 0.001)
    }

    @Test
    fun testNumberSystemConversions() {
        val resultDec = NumberSystemEngine.convertAll("255", NumberBase.DECIMAL)
        assertEquals("255", resultDec.decimal)
        assertEquals("FF", resultDec.hexadecimal)
        assertEquals("377", resultDec.octal)
        assertEquals("1111 1111", resultDec.binary)

        val resultHex = NumberSystemEngine.convertAll("1A", NumberBase.HEXADECIMAL)
        assertEquals("26", resultHex.decimal)
        assertEquals("1A", resultHex.hexadecimal)
        assertEquals("32", resultHex.octal)
    }

    @Test
    fun testLoanEmi() {
        // Loan of 100,000 at 12% annual for 12 months
        val emiResult = FinanceEngine.calculateLoanEmi(100000.0, 12.0, 12)
        // Standard EMI for 100k @ 12% for 12 mo is ~8884.88
        assertTrue(emiResult.monthlyEmi > 8880.0 && emiResult.monthlyEmi < 8890.0)
        assertTrue(emiResult.totalPayment > 100000.0)
    }

    @Test
    fun testGstCalculation() {
        // Add 18% GST to 1000 -> 180 GST, 1180 Total
        val addGst = FinanceEngine.calculateGst(1000.0, 18.0, isAddGst = true)
        assertEquals(1000.0, addGst.netAmount, 0.001)
        assertEquals(180.0, addGst.gstAmount, 0.001)
        assertEquals(1180.0, addGst.grossAmount, 0.001)
        assertEquals(90.0, addGst.cgstAmount, 0.001)

        // Remove 18% GST from 1180 -> 1000 Net, 180 GST
        val removeGst = FinanceEngine.calculateGst(1180.0, 18.0, isAddGst = false)
        assertEquals(1000.0, removeGst.netAmount, 0.001)
        assertEquals(180.0, removeGst.gstAmount, 0.001)
    }

    @Test
    fun testBmiCalculation() {
        // 70 kg, 175 cm (1.75m) -> BMI ~ 22.86 (Normal)
        val bmi = HealthLifeEngine.calculateBmiMetric(175.0, 70.0)
        assertNotNull(bmi)
        assertEquals(22.86, bmi!!.bmi, 0.05)
        assertEquals(BmiCategory.NORMAL, bmi.category)
    }

    @Test
    fun testAgeCalculation() {
        val birth = LocalDate.of(2000, 1, 1)
        val target = LocalDate.of(2025, 1, 1)
        val age = HealthLifeEngine.calculateAge(birth, target)
        assertNotNull(age)
        assertEquals(25, age!!.years)
        assertEquals(0, age.months)
        assertEquals(0, age.days)
    }

    @Test
    fun testDataStorageConversion() {
        val storageUnits = UnitConverterEngine.getUnitsForCategory(UnitCategory.DATA_STORAGE)
        val mb = storageUnits.first { it.id == "mb" }
        val mB = storageUnits.first { it.id == "MB" }
        val gB = storageUnits.first { it.id == "GB" }
        val byteUnit = storageUnits.first { it.id == "B" }
        val bitUnit = storageUnits.first { it.id == "b" }

        // 8 Megabits (mb) should equal 1 Megabyte (MB)
        val mbToMB = UnitConverterEngine.convert(8.0, from = mb, to = mB, category = UnitCategory.DATA_STORAGE)
        assertEquals(1.0, mbToMB, 0.0001)

        // 1 Megabyte (MB) should equal 8 Megabits (mb)
        val mbToBits = UnitConverterEngine.convert(1.0, from = mB, to = mb, category = UnitCategory.DATA_STORAGE)
        assertEquals(8.0, mbToBits, 0.0001)

        // 1000 MB = 1 GB (decimal)
        val mbToGB = UnitConverterEngine.convert(1000.0, from = mB, to = gB, category = UnitCategory.DATA_STORAGE)
        assertEquals(1.0, mbToGB, 0.0001)

        // 1 Byte = 8 bits
        val byteToBit = UnitConverterEngine.convert(1.0, from = byteUnit, to = bitUnit, category = UnitCategory.DATA_STORAGE)
        assertEquals(8.0, byteToBit, 0.0001)
    }

    @Test
    fun testDataSpeedConversion() {
        val speedUnits = UnitConverterEngine.getUnitsForCategory(UnitCategory.DATA_SPEED)
        val mbps = speedUnits.first { it.id == "mbps" }
        val mB_s = speedUnits.first { it.id == "mb_s" }
        val gbps = speedUnits.first { it.id == "gbps" }

        // 100 Mbps internet speed = 12.5 MB/s download speed
        val downloadSpeed = UnitConverterEngine.convert(100.0, from = mbps, to = mB_s, category = UnitCategory.DATA_SPEED)
        assertEquals(12.5, downloadSpeed, 0.0001)

        // 1 Gbps = 1000 Mbps
        val gigabitSpeed = UnitConverterEngine.convert(1.0, from = gbps, to = mbps, category = UnitCategory.DATA_SPEED)
        assertEquals(1000.0, gigabitSpeed, 0.0001)
    }

    @Test
    fun testDateCalculatorDifference() {
        val start = LocalDate.of(2024, 1, 1)
        val end = LocalDate.of(2025, 2, 15)

        val diff = DateTimeEngine.calculateDateDifference(start, end)
        assertEquals(1, diff.years)
        assertEquals(1, diff.months)
        assertEquals(14, diff.days)
        assertTrue(diff.totalDays > 400)
        assertTrue(diff.workDays > 0)
        assertTrue(diff.weekendDays > 0)
        assertEquals(false, diff.isPast)
    }

    @Test
    fun testDateCalculatorAddSubtract() {
        val start = LocalDate.of(2024, 1, 1)
        val added = DateTimeEngine.addSubtractDate(
            baseDate = start,
            years = 1,
            months = 2,
            weeks = 1,
            days = 3,
            isAdd = true
        )
        // 2024-01-01 + 1 yr -> 2025-01-01 + 2 mo -> 2025-03-01 + 10 days -> 2025-03-11
        assertEquals(LocalDate.of(2025, 3, 11), added.resultDate)

        val subtracted = DateTimeEngine.addSubtractDate(
            baseDate = start,
            years = 1,
            days = 1,
            isAdd = false
        )
        // 2024-01-01 - 1 yr -> 2023-01-01 - 1 day -> 2022-12-31
        assertEquals(LocalDate.of(2022, 12, 31), subtracted.resultDate)
    }

    @Test
    fun testTimeCalculatorDifference() {
        val t1 = java.time.LocalTime.of(9, 0, 0)
        val t2 = java.time.LocalTime.of(17, 30, 0)
        val diff = DateTimeEngine.calculateTimeDifference(t1, t2)
        assertEquals(8, diff.hours)
        assertEquals(30, diff.minutes)
        assertEquals(0, diff.seconds)
        assertEquals(8.5, diff.totalHours, 0.001)
        assertEquals(510.0, diff.totalMinutes, 0.001)
        assertEquals(false, diff.isNextDay)

        // Overnight: 23:00 to 02:30 next day
        val tNight1 = java.time.LocalTime.of(23, 0, 0)
        val tNight2 = java.time.LocalTime.of(2, 30, 0)
        val nightDiff = DateTimeEngine.calculateTimeDifference(tNight1, tNight2)
        assertEquals(3, nightDiff.hours)
        assertEquals(30, nightDiff.minutes)
        assertEquals(true, nightDiff.isNextDay)
    }

    @Test
    fun testTimeCalculatorAddSubtract() {
        val base = java.time.LocalTime.of(10, 0, 0)
        // +5h 30m -> 15:30 (Same day, offset 0)
        val resAdd = DateTimeEngine.addSubtractTime(base, hours = 5, minutes = 30, seconds = 0, isAdd = true)
        assertEquals(java.time.LocalTime.of(15, 30, 0), resAdd.resultTime)
        assertEquals(0, resAdd.daysOffset)

        // +16h -> 02:00 next day (+1 day, offset 1)
        val resNextDay = DateTimeEngine.addSubtractTime(base, hours = 16, minutes = 0, seconds = 0, isAdd = true)
        assertEquals(java.time.LocalTime.of(2, 0, 0), resNextDay.resultTime)
        assertEquals(1, resNextDay.daysOffset)

        // -12h -> 22:00 previous day (-1 day, offset -1)
        val resPrevDay = DateTimeEngine.addSubtractTime(base, hours = 12, minutes = 0, seconds = 0, isAdd = false)
        assertEquals(java.time.LocalTime.of(22, 0, 0), resPrevDay.resultTime)
        assertEquals(-1, resPrevDay.daysOffset)
    }
}
