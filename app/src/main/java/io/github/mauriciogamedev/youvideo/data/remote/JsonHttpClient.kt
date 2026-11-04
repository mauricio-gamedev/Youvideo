package io.github.mauriciogamedev.youvideo.data.remote

import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

internal object JsonHttpClient {
    fun get(url: String, maxBytes: Int = 1_500_000): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 15_000
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Accept-Language", "pt-BR,pt;q=0.9,en;q=0.8")
            setRequestProperty("User-Agent", "Youvideo/0.1 Android")
        }

        try {
            val statusCode = connection.responseCode
            val stream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.use { input ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(8 * 1024)
                var total = 0
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    total += read
                    if (total > maxBytes) {
                        throw IOException("A resposta da fonte excedeu o limite permitido.")
                    }
                    output.write(buffer, 0, read)
                }
                output.toString(Charsets.UTF_8.name())
            }.orEmpty()

            if (statusCode !in 200..299) {
                throw IOException("A fonte respondeu com HTTP $statusCode.")
            }
            if (body.isBlank()) {
                throw IOException("A fonte retornou uma resposta vazia.")
            }
            return JSONObject(body)
        } finally {
            connection.disconnect()
        }
    }
}
