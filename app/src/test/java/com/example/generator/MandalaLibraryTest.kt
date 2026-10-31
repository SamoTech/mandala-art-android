package com.example.generator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MandalaLibraryTest {

    @Test
    fun unknownDesignIdFailsInsteadOfReturningFirstDesign() {
        val error = assertThrows(NoSuchElementException::class.java) {
            MandalaLibrary.getDesignById("not-a-real-design")
        }
        assertTrue(error.message.orEmpty().contains("not-a-real-design"))
    }

    @Test
    fun nullableLookupReturnsNullForUnknownId() {
        assertEquals(null, MandalaLibrary.findDesignById("not-a-real-design"))
    }

    @Test
    fun validDesignIdReturnsExactMatch() {
        val expected = MandalaLibrary.designs.first { it.id == "starlight_blossom" }
        assertEquals(expected, MandalaLibrary.getDesignById("starlight_blossom"))
    }

    @Test
    fun dailyInspirationIsStableForSameDateAndChangesOnFollowingDate() {
        val first = MandalaLibrary.dailyInspiration(2026, 304)
        val sameDay = MandalaLibrary.dailyInspiration(2026, 304)
        val nextDay = MandalaLibrary.dailyInspiration(2026, 305)

        assertEquals(first.id, sameDay.id)
        assertNotEquals(first.id, nextDay.id)
    }

    @Test
    fun dailyInspirationAdvancesContinuouslyAcrossYearBoundary() {
        val lastDay = MandalaLibrary.dailyInspiration(2025, 365)
        val firstDay = MandalaLibrary.dailyInspiration(2026, 1)
        val nextDay = MandalaLibrary.dailyInspiration(2026, 2)

        assertNotEquals(lastDay.id, firstDay.id)
        assertNotEquals(firstDay.id, nextDay.id)
    }

    @Test
    fun dailyInspirationValidatesLeapDays() {
        assertNotEquals(
            MandalaLibrary.dailyInspiration(2024, 60).id,
            MandalaLibrary.dailyInspiration(2024, 61).id
        )
        assertThrows(IllegalArgumentException::class.java) {
            MandalaLibrary.dailyInspiration(2025, 366)
        }
    }

    @Test
    fun dailyInspirationRejectsEmptyLibrary() {
        assertThrows(IllegalArgumentException::class.java) {
            dailyInspirationForDate(2026, 304, emptyList())
        }
    }
}
