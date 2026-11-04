package io.github.mauriciogamedev.youvideo.domain.provider

import java.net.URI

object ProviderUrlPolicy {
    fun normalizeHttpsBaseUrl(rawValue: String): String {
        val value = rawValue.trim().trimEnd('/')
        require(value.isNotEmpty()) { "Informe o endereço da instância." }

        val uri = URI(value)
        require(uri.scheme.equals("https", ignoreCase = true)) {
            "A instância precisa usar HTTPS."
        }
        require(!uri.host.isNullOrBlank()) { "O endereço da instância é inválido." }
        require(uri.userInfo == null && uri.query == null && uri.fragment == null) {
            "Informe somente o endereço base da instância."
        }
        require(uri.rawPath.isNullOrEmpty() || uri.rawPath == "/") {
            "O endereço precisa apontar para a raiz da instância."
        }

        val host = uri.host.lowercase()
        val port = if (uri.port == -1 || uri.port == 443) "" else ":" + uri.port
        return "https://" + host + port
    }
}
