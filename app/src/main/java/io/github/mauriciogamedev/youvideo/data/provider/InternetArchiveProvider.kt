package io.github.mauriciogamedev.youvideo.data.provider

import io.github.mauriciogamedev.youvideo.data.remote.JsonHttpClient
import io.github.mauriciogamedev.youvideo.domain.provider.MediaProvider
import io.github.mauriciogamedev.youvideo.domain.provider.ProviderCapability
import io.github.mauriciogamedev.youvideo.domain.provider.ProviderId
import io.github.mauriciogamedev.youvideo.domain.provider.ProviderMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class InternetArchiveProvider : MediaProvider {
    override val id = ProviderId.INTERNET_ARCHIVE
    override val displayName = "Internet Archive"
    override val capabilities = setOf(
        ProviderCapability.SEARCH,
        ProviderCapability.EMBEDDED_PLAYBACK
    )

    override suspend fun search(query: String, limit: Int): List<ProviderMedia> =
        withContext(Dispatchers.IO) {
            val cleanedQuery = query.trim()
            if (cleanedQuery.length < 2) return@withContext emptyList()

            val safeTitleQuery = cleanedQuery
                .replace("\\", " ")
                .replace("\"", " ")
                .replace(":", " ")
                .replace("(", " ")
                .replace(")", " ")
                .replace("*", " ")
                .trim()
            if (safeTitleQuery.isBlank()) return@withContext emptyList()

            val archiveQuery = "mediatype:movies AND title:\"$safeTitleQuery\""
            val encodedQuery = URLEncoder.encode(archiveQuery, StandardCharsets.UTF_8.name())
            val fields = listOf(
                "identifier",
                "title",
                "creator",
                "description",
                "licenseurl",
                "rights"
            ).joinToString("&") { "fl%5B%5D=" + it }

            val requestUrl = "https://archive.org/advancedsearch.php?q=" + encodedQuery +
                "&" + fields +
                "&rows=" + limit.coerceIn(1, 20) +
                "&page=1&output=json"

            val response = JsonHttpClient.get(requestUrl, maxBytes = 1_500_000)
            val docs = response.optJSONObject("response")
                ?.optJSONArray("docs") ?: return@withContext emptyList()

            buildList {
                for (index in 0 until docs.length()) {
                    val item = docs.optJSONObject(index) ?: continue
                    val identifier = item.optString("identifier").trim()
                    val title = item.readableValue("title").trim()
                    if (identifier.isBlank() || title.isBlank()) continue

                    add(
                        ProviderMedia(
                            id = identifier,
                            providerId = id,
                            providerName = displayName,
                            title = title,
                            creator = item.readableValue("creator").takeIf { it.isNotBlank() },
                            description = item.readableValue("description").takeIf { it.isNotBlank() },
                            thumbnailUrl = "https://archive.org/services/img/$identifier",
                            durationSeconds = null,
                            sourceUrl = "https://archive.org/details/$identifier",
                            playbackUrl = "https://archive.org/embed/$identifier",
                            rightsLabel = item.readableValue("licenseurl")
                                .ifBlank { item.readableValue("rights") }
                                .takeIf { it.isNotBlank() },
                            downloadUrl = null
                        )
                    )
                }
            }
        }

    private fun JSONObject.readableValue(key: String): String {
        return when (val value = opt(key)) {
            null, JSONObject.NULL -> ""
            is JSONArray -> value.optString(0).takeIf { it.isNotBlank() }.orEmpty()
            else -> value.toString()
        }
    }
}
