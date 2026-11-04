package io.github.mauriciogamedev.youvideo.data.provider

import io.github.mauriciogamedev.youvideo.data.remote.JsonHttpClient
import io.github.mauriciogamedev.youvideo.domain.provider.MediaProvider
import io.github.mauriciogamedev.youvideo.domain.provider.ProviderCapability
import io.github.mauriciogamedev.youvideo.domain.provider.ProviderId
import io.github.mauriciogamedev.youvideo.domain.provider.ProviderMedia
import io.github.mauriciogamedev.youvideo.domain.provider.ProviderUrlPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class PeerTubeProvider(
    instanceUrl: String = DEFAULT_INSTANCE
) : MediaProvider {
    private val baseUrl = ProviderUrlPolicy.normalizeHttpsBaseUrl(instanceUrl)

    override val id = ProviderId.PEERTUBE
    override val displayName = "PeerTube"
    override val capabilities = setOf(
        ProviderCapability.SEARCH,
        ProviderCapability.EMBEDDED_PLAYBACK
    )

    override suspend fun search(query: String, limit: Int): List<ProviderMedia> =
        withContext(Dispatchers.IO) {
            val cleanedQuery = query.trim()
            if (cleanedQuery.length < 2) return@withContext emptyList()

            val encodedQuery = URLEncoder.encode(cleanedQuery, StandardCharsets.UTF_8.name())
            val requestUrl = baseUrl + "/api/v1/search/videos" +
                "?search=" + encodedQuery +
                "&count=" + limit.coerceIn(1, 20) +
                "&sort=-publishedAt&nsfw=false"

            val response = JsonHttpClient.get(requestUrl)
            val entries = response.optJSONArray("data") ?: return@withContext emptyList()

            buildList {
                for (index in 0 until entries.length()) {
                    val video = entries.optJSONObject(index) ?: continue
                    val uuid = video.optString("uuid").ifBlank {
                        video.optString("shortUUID")
                    }
                    val title = video.optString("name").trim()
                    if (uuid.isBlank() || title.isBlank()) continue

                    val channel = video.optJSONObject("channel")
                    val account = video.optJSONObject("account")
                    val creator = channel?.optString("displayName")
                        ?.takeIf { !it.isNullOrBlank() }
                        ?: account?.optString("displayName")?.takeIf { !it.isNullOrBlank() }

                    val embedPath = video.optString("embedPath")
                    val embedUrl = resolveHttpsUrl(
                        if (embedPath.isBlank()) "/videos/embed/$uuid" else embedPath
                    ) ?: continue

                    val thumbnailPath = video.optString("thumbnailPath")
                        .ifBlank { firstThumbnailUrl(video) }
                    val thumbnailUrl = thumbnailPath.takeIf { it.isNotBlank() }
                        ?.let { resolveHttpsUrl(it) }

                    val sourcePath = video.optString("url")
                        .ifBlank { "/videos/watch/$uuid" }
                    val sourceUrl = resolveHttpsUrl(sourcePath) ?: embedUrl

                    add(
                        ProviderMedia(
                            id = uuid,
                            providerId = id,
                            providerName = displayName,
                            title = title,
                            creator = creator,
                            description = video.optString("truncatedDescription")
                                .takeIf { it.isNotBlank() },
                            thumbnailUrl = thumbnailUrl,
                            durationSeconds = video.optLong("duration", 0L)
                                .takeIf { it > 0L },
                            sourceUrl = sourceUrl,
                            playbackUrl = embedUrl,
                            rightsLabel = video.optJSONObject("licence")
                                ?.optString("label")
                                ?.takeIf { !it.isNullOrBlank() }
                        )
                    )
                }
            }
        }

    private fun firstThumbnailUrl(video: JSONObject): String {
        val thumbnails = video.optJSONArray("thumbnails") ?: return ""
        return thumbnails.optJSONObject(0)?.optString("fileUrl").orEmpty()
    }

    private fun resolveHttpsUrl(pathOrUrl: String): String? {
        return runCatching {
            val baseUri = URI(baseUrl)
            val resolved = baseUri.resolve(pathOrUrl)
            val basePort = if (baseUri.port == -1) 443 else baseUri.port
            val resolvedPort = if (resolved.port == -1) 443 else resolved.port
            val sameInstance = resolved.host.equals(baseUri.host, ignoreCase = true) &&
                resolvedPort == basePort &&
                resolved.userInfo == null

            if (
                resolved.scheme.equals("https", ignoreCase = true) &&
                !resolved.host.isNullOrBlank() &&
                sameInstance
            ) {
                resolved.toString()
            } else {
                null
            }
        }.getOrNull()
    }

    companion object {
        const val DEFAULT_INSTANCE = "https://tilvids.com"
    }
}
