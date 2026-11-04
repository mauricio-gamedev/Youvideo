package io.github.mauriciogamedev.youvideo.domain.provider

import org.junit.Assert.assertEquals
import org.junit.Test

class MediaFormattingTest {
    @Test
    fun durationLabelHandlesEmptyAndInvalidValues() {
        assertEquals("", MediaFormatting.durationLabel(null))
        assertEquals("", MediaFormatting.durationLabel(0L))
        assertEquals("", MediaFormatting.durationLabel(-1L))
    }

    @Test
    fun durationLabelFormatsMinutesAndSeconds() {
        assertEquals("1:05", MediaFormatting.durationLabel(65L))
        assertEquals("59:59", MediaFormatting.durationLabel(3599L))
    }

    @Test
    fun durationLabelFormatsHours() {
        assertEquals("1:01:01", MediaFormatting.durationLabel(3661L))
    }
}
