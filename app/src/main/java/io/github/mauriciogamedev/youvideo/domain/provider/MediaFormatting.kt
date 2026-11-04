package io.github.mauriciogamedev.youvideo.domain.provider

object MediaFormatting {
    fun durationLabel(durationSeconds: Long?): String {
        if (durationSeconds == null || durationSeconds <= 0L) return ""

        val hours = durationSeconds / 3600
        val minutes = (durationSeconds % 3600) / 60
        val seconds = durationSeconds % 60

        return if (hours > 0) {
            hours.toString() + ":" + minutes.toString().padStart(2, '0') +
                ":" + seconds.toString().padStart(2, '0')
        } else {
            minutes.toString() + ":" + seconds.toString().padStart(2, '0')
        }
    }
}
