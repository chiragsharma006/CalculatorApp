package com.calculator.app.domain.tools

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.Period
import java.time.temporal.ChronoUnit

data class DateDifferenceResult(
    val years: Int,
    val months: Int,
    val days: Int,
    val totalDays: Long,
    val totalWeeks: Long,
    val remainingDaysInWeek: Long,
    val workDays: Long,
    val weekendDays: Long,
    val isPast: Boolean
)

data class DateAddSubtractResult(
    val resultDate: LocalDate,
    val dayOfWeek: DayOfWeek,
    val dayOfYear: Int,
    val isLeapYear: Boolean
)

data class TimeDifferenceResult(
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val totalSeconds: Long,
    val totalMinutes: Double,
    val totalHours: Double,
    val percentageOfDay: Double,
    val isNextDay: Boolean
)

data class TimeAddSubtractResult(
    val resultTime: LocalTime,
    val daysOffset: Int // 0 = same day, +1 = tomorrow, -1 = yesterday, etc.
)

data class TimeDuration(
    val hours: Long = 0,
    val minutes: Long = 0,
    val seconds: Long = 0
)

data class TimeSumResult(
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val totalSeconds: Long,
    val totalHoursDecimal: Double
)

object DateTimeEngine {

    /**
     * Calculates chronological and breakdown difference between two dates.
     */
    fun calculateDateDifference(start: LocalDate, end: LocalDate): DateDifferenceResult {
        val isPast = end.isBefore(start)
        val from = if (isPast) end else start
        val to = if (isPast) start else end

        val period = Period.between(from, to)
        val totalDays = ChronoUnit.DAYS.between(from, to)
        val totalWeeks = totalDays / 7
        val remainingDays = totalDays % 7

        var workDays = 0L
        var weekendDays = 0L
        var curr = from
        while (curr.isBefore(to)) {
            val dow = curr.dayOfWeek
            if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                weekendDays++
            } else {
                workDays++
            }
            curr = curr.plusDays(1)
        }

        return DateDifferenceResult(
            years = period.years,
            months = period.months,
            days = period.days,
            totalDays = totalDays,
            totalWeeks = totalWeeks,
            remainingDaysInWeek = remainingDays,
            workDays = workDays,
            weekendDays = weekendDays,
            isPast = isPast
        )
    }

    /**
     * Adds or subtracts years, months, weeks, and days to/from a starting date.
     */
    fun addSubtractDate(
        baseDate: LocalDate,
        years: Int = 0,
        months: Int = 0,
        weeks: Int = 0,
        days: Int = 0,
        isAdd: Boolean = true
    ): DateAddSubtractResult {
        val mult = if (isAdd) 1L else -1L
        val res = baseDate
            .plusYears(mult * years)
            .plusMonths(mult * months)
            .plusWeeks(mult * weeks)
            .plusDays(mult * days)

        return DateAddSubtractResult(
            resultDate = res,
            dayOfWeek = res.dayOfWeek,
            dayOfYear = res.dayOfYear,
            isLeapYear = res.isLeapYear
        )
    }

    /**
     * Calculates the duration between two times.
     * If end time is before start time or forceNextDay is true, it accounts for crossing midnight (+24h).
     */
    fun calculateTimeDifference(
        start: LocalTime,
        end: LocalTime,
        forceNextDay: Boolean = false
    ): TimeDifferenceResult {
        var startSec = start.toSecondOfDay().toLong()
        var endSec = end.toSecondOfDay().toLong()

        val isNextDay = forceNextDay || (endSec < startSec)
        if (isNextDay) {
            endSec += 86400L // 24 hours in seconds
        }

        val diffSec = endSec - startSec
        val hours = diffSec / 3600
        val minutes = (diffSec % 3600) / 60
        val seconds = diffSec % 60
        val totalMinutes = diffSec / 60.0
        val totalHours = diffSec / 3600.0
        val percentageOfDay = (diffSec.toDouble() / 86400.0) * 100.0

        return TimeDifferenceResult(
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            totalSeconds = diffSec,
            totalMinutes = totalMinutes,
            totalHours = totalHours,
            percentageOfDay = percentageOfDay,
            isNextDay = isNextDay
        )
    }

    /**
     * Adds or subtracts hours, minutes, and seconds from a base time.
     * Accurately determines day roll-overs (+1 day, -1 day, etc.).
     */
    fun addSubtractTime(
        baseTime: LocalTime,
        hours: Long = 0,
        minutes: Long = 0,
        seconds: Long = 0,
        isAdd: Boolean = true
    ): TimeAddSubtractResult {
        val totalDeltaSec = (hours * 3600 + minutes * 60 + seconds) * (if (isAdd) 1 else -1)
        val baseSec = baseTime.toSecondOfDay().toLong()
        val rawSec = baseSec + totalDeltaSec

        val daysOffset = Math.floorDiv(rawSec, 86400L).toInt()
        val daySec = Math.floorMod(rawSec, 86400L).toInt()

        return TimeAddSubtractResult(
            resultTime = LocalTime.ofSecondOfDay(daySec.toLong()),
            daysOffset = daysOffset
        )
    }

    /**
     * Sums a list of durations (hours, minutes, seconds).
     */
    fun sumTimeDurations(durations: List<TimeDuration>): TimeSumResult {
        var totalSec = 0L
        for (d in durations) {
            totalSec += (d.hours * 3600 + d.minutes * 60 + d.seconds)
        }
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        val decimalHours = totalSec / 3600.0
        return TimeSumResult(
            hours = h,
            minutes = m,
            seconds = s,
            totalSeconds = totalSec,
            totalHoursDecimal = decimalHours
        )
    }
}
