package com.calculator.app.domain.tools

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

enum class BmiCategory(val label: String, val minBmi: Double, val maxBmi: Double) {
    UNDERWEIGHT("Underweight", 0.0, 18.5),
    NORMAL("Normal weight", 18.5, 24.9),
    OVERWEIGHT("Overweight", 25.0, 29.9),
    OBESE_CLASS_1("Obese (Class 1)", 30.0, 34.9),
    OBESE_CLASS_2("Obese (Class 2)", 35.0, 39.9),
    OBESE_CLASS_3("Severe Obesity (Class 3)", 40.0, 100.0)
}

data class BmiResult(
    val bmi: Double,
    val category: BmiCategory,
    val healthyWeightMinKg: Double,
    val healthyWeightMaxKg: Double,
    val healthyWeightMinLbs: Double,
    val healthyWeightMaxLbs: Double
)

data class AgeResult(
    val years: Int,
    val months: Int,
    val days: Int,
    val nextBirthdayMonths: Int,
    val nextBirthdayDays: Int,
    val totalDays: Long,
    val totalWeeks: Long,
    val totalHours: Long,
    val dayOfWeekBorn: String
)

object HealthLifeEngine {

    fun calculateBmiMetric(heightCm: Double, weightKg: Double): BmiResult? {
        if (heightCm <= 0 || weightKg <= 0) return null
        val heightM = heightCm / 100.0
        val bmi = weightKg / (heightM * heightM)
        return buildBmiResult(bmi, heightM)
    }

    fun calculateBmiImperial(heightInches: Double, weightLbs: Double): BmiResult? {
        if (heightInches <= 0 || weightLbs <= 0) return null
        val bmi = 703.0 * weightLbs / (heightInches * heightInches)
        val heightM = heightInches * 0.0254
        return buildBmiResult(bmi, heightM)
    }

    private fun buildBmiResult(bmi: Double, heightM: Double): BmiResult {
        val category = when {
            bmi < 18.5 -> BmiCategory.UNDERWEIGHT
            bmi < 25.0 -> BmiCategory.NORMAL
            bmi < 30.0 -> BmiCategory.OVERWEIGHT
            bmi < 35.0 -> BmiCategory.OBESE_CLASS_1
            bmi < 40.0 -> BmiCategory.OBESE_CLASS_2
            else -> BmiCategory.OBESE_CLASS_3
        }

        val healthyMinKg = 18.5 * (heightM * heightM)
        val healthyMaxKg = 24.9 * (heightM * heightM)

        return BmiResult(
            bmi = bmi,
            category = category,
            healthyWeightMinKg = healthyMinKg,
            healthyWeightMaxKg = healthyMaxKg,
            healthyWeightMinLbs = healthyMinKg * 2.20462,
            healthyWeightMaxLbs = healthyMaxKg * 2.20462
        )
    }

    fun calculateAge(birthDate: LocalDate, today: LocalDate = LocalDate.now()): AgeResult? {
        if (birthDate.isAfter(today)) return null

        val period = Period.between(birthDate, today)
        val totalDays = ChronoUnit.DAYS.between(birthDate, today)
        val totalWeeks = ChronoUnit.WEEKS.between(birthDate, today)
        val totalHours = totalDays * 24

        // Next Birthday calculation
        var nextBirthday = birthDate.withYear(today.year)
        if (nextBirthday.isBefore(today) || nextBirthday.isEqual(today)) {
            nextBirthday = birthDate.withYear(today.year + 1)
        }
        val nextPeriod = Period.between(today, nextBirthday)

        val dayName = birthDate.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }

        return AgeResult(
            years = period.years,
            months = period.months,
            days = period.days,
            nextBirthdayMonths = nextPeriod.months,
            nextBirthdayDays = nextPeriod.days,
            totalDays = totalDays,
            totalWeeks = totalWeeks,
            totalHours = totalHours,
            dayOfWeekBorn = dayName
        )
    }
}
