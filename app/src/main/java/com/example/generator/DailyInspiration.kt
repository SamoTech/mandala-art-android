package com.example.generator

import com.example.model.MandalaDesign
import java.time.LocalDate

/**
 * Selects one stable design per calendar day.
 * The same date always maps to the same design, independently of recomposition.
 */
internal fun dailyInspirationForDate(
    date: LocalDate,
    designs: List<MandalaDesign>
): MandalaDesign {
    require(designs.isNotEmpty()) { "At least one design is required" }
    val index = Math.floorMod(date.toEpochDay(), designs.size.toLong()).toInt()
    return designs[index]
}
