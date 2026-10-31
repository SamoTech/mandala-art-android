package com.example.generator

import java.time.LocalDate
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
        val day = LocalDate.of(2026, 10, 31)
        val first = MandalaLibrary.dailyInspiration(day)
        val sameDay = MandalaLibrary.dailyInspiration(day)
        val nextDay = MandalaLibrary.dailyInspiration(day.plusDays(1))

        assertEquals(first.id, sameDay.id)
        assertNotEquals(first.id, nextDay.id)
    }

    @Test
    fun dailyInspirationRejectsEmptyLibrary() {
        assertThrows(IllegalArgumentException::class.java) {
            dailyInspirationForDate(LocalDate.of(2026, 10, 31), emptyList())
        }
    }
}
