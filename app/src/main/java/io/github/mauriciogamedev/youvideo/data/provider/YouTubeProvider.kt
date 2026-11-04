package io.github.mauriciogamedev.youvideo.data.provider

import io.github.mauriciogamedev.youvideo.data.remote.JsonHttpClient
import io.github.mauriciogamedev.youvideo.domain.provider.MediaProvider
import io.github.mauriciogamedev.youvideo.domain.provider.ProviderCapability
import io.github.mauriciogamedev.youvideo.domain.provider.ProviderId
import io.github.mauriciogamedev.youvideo.domain.provider.ProviderMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class YouTubeProvider(
    private val apiKey: String
) : MediaProvider {
    init {
        require(apiKey.isNotBlank()) { "A chave da YouTube Data API não foi configurada." }
    }

    override val id = ProviderId.YOUTUBE
    override val displayName = "YouTube"
    override val capabilities = setOf(
        ProviderCapability.SEARCH,
        ProviderCapability.EMBEDDED_PLAYBACK
    )

    override suspend fun search(query: String, limit: Int): List<ProviderMedia> =
        withContext(Dispatchers.IO) {
            val cleanedQuery = query.trim()
            if (cleanedQuery.length < 2) return@withContext emptyList()

            val requestUrl = "https://www.googleapis.com/youtube/v3/search" +
                "?part=snippet&type=video&videoEmbeddable=true&safeSearch=strict" +
                "&regionCode=BR&relevanceLanguage=pt&maxResults=" +
                limit.coerceIn(1, 10) +
                "&q=" + encode(cleanedQuery) +
                "&key=" + encode(apiKey)

            val response = JsonHttpClient.get(requestUrl, maxBytes = 1_000_000)
            val entries = response.optJSONArray("items") ?: return@withContext emptyList()

            val candidates = buildList {
                for (index in 0 until entries.length()) {
                    val item = entries.optJSONObject(index) ?: continue
                    val videoId = item.optJSONObject("id")?.optString("videoId").orEmpty()
                    val snippet = item.optJSONObject("snippet") ?: continue
                    val title = snippet.optString("title").trim()
                    if (videoId.isNotBlank() && title.isNotBlank()) {
                        add(videoId to snippet)
                    }
                }
            }
            if (candidates.isEmpty()) return@withContext emptyList()

            // Re-check current public status before embedding: made-for-kids videos
            // and results whose embed status cannot be verified are excluded.
            val ids = encode(candidates.joinToString(",") { it.first })
            val statusResponse = JsonHttpClient.get(
                "https://www.googleapis.com/youtube/v3/videos" +
                    "?part=status&id=$ids&key=${encode(apiKey)}",
                maxBytes = 500_000
            )
            val statusItems = statusResponse.optJSONArray("items")
            val eligibleIds = buildSet {
                if (statusItems != null) {
                    for (index in 0 until statusItems.length()) {
                        val item = statusItems.optJSONObject(index) ?: continue
                        val videoId = item.optString("id")
                        val status = item.optJSONObject("status") ?: continue
                        val hasKidsStatus = status.has("madeForKids") &&
                            !status.isNull("madeForKids")
                        if (
                            videoId.isNotBlank() &&
                            hasKidsStatus &&
                            !status.optBoolean("madeForKids", true) &&
                            status.optBoolean("embeddable", false)
                        ) {
                            add(videoId)
                        }
                    }
                }
            }

            buildList {
                for ((videoId, snippet) in candidates) {
                    if (videoId !in eligibleIds) continue
                    val thumbnails = snippet.optJSONObject("thumbnails")
                    val thumbnailUrl = thumbnails?.optJSONObject("high")?.optString("url")
                        ?.takeIf { !it.isNullOrBlank() }
                        ?: thumbnails?.optJSONObject("medium")?.optString("url")
                            ?.takeIf { !it.isNullOrBlank() }

                    add(
                        ProviderMedia(
                            id = videoId,
                            providerId = id,
                            providerName = displayName,
                            title = snippet.optString("title").trim(),
                            creator = snippet.optString("channelTitle")
                                .takeIf { it.isNotBlank() },
                            description = snippet.optString("description")
                                .takeIf { it.isNotBlank() },
                            thumbnailUrl = thumbnailUrl,
                            durationSeconds = null,
                            sourceUrl = "https://www.youtube.com/watch?v=$videoId",
                            playbackUrl = "https://www.youtube.com/embed/$videoId",
                            rightsLabel = "Reprodução pelo player oficial do YouTube",
                            downloadUrl = null
                        )
                    )
                }
            }
        }

    private fun encode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.name())
}
