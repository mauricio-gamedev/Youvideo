package io.github.mauriciogamedev.youvideo.domain.provider

enum class ProviderId(val displayName: String) {
    PEERTUBE("PeerTube"),
    INTERNET_ARCHIVE("Internet Archive"),
    YOUTUBE("YouTube")
}

enum class ProviderCapability {
    SEARCH,
    EMBEDDED_PLAYBACK
}

data class ProviderMedia(
    val id: String,
    val providerId: ProviderId,
    val providerName: String,
    val title: String,
    val creator: String?,
    val description: String?,
    val thumbnailUrl: String?,
    val durationSeconds: Long?,
    val sourceUrl: String,
    val playbackUrl: String,
    val rightsLabel: String? = null,
    val downloadUrl: String? = null
) {
    val stableKey: String
        get() = providerId.name + ":" + id
}

interface MediaProvider {
    val id: ProviderId
    val displayName: String
    val capabilities: Set<ProviderCapability>

    suspend fun search(query: String, limit: Int = 12): List<ProviderMedia>
}
