package com.example.generator

import com.example.model.MandalaDesign

/**
 * Selects one stable design per local calendar day.
 *
 * Uses the proleptic Gregorian calendar arithmetic so this remains compatible
 * with the application's minSdk without relying on java.time desugaring.
 */
internal fun dailyInspirationForDate(
    year: Int,
    dayOfYear: Int,
    designs: List<MandalaDesign>
): MandalaDesign {
    require(year >= 1) { "Year must be positive" }
    require(designs.isNotEmpty()) { "At least one design is required" }
    val leapYear = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
    val daysInYear = if (leapYear) 366 else 365
    require(dayOfYear in 1..daysInYear) { "Invalid day of year: $dayOfYear" }

    val previousYear = year.toLong() - 1L
    val daysBeforeYear = 365L * previousYear +
        previousYear / 4L -
        previousYear / 100L +
        previousYear / 400L
    val absoluteDay = daysBeforeYear + dayOfYear - 1L
    val index = Math.floorMod(absoluteDay, designs.size.toLong()).toInt()
    return designs[index]
}
