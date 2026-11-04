package io.github.mauriciogamedev.youvideo.data.provider

import io.github.mauriciogamedev.youvideo.domain.provider.MediaProvider

object DefaultMediaProviders {
    fun create(youtubeApiKey: String): List<MediaProvider> = buildList {
        add(PeerTubeProvider())
        add(InternetArchiveProvider())
        if (youtubeApiKey.isNotBlank()) {
            add(YouTubeProvider(youtubeApiKey))
        }
    }
}
