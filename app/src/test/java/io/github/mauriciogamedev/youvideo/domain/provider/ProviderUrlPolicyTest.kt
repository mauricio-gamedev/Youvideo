package io.github.mauriciogamedev.youvideo.domain.provider

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ProviderUrlPolicyTest {
    @Test
    fun normalizesHttpsHostAndDefaultPort() {
        assertEquals(
            "https://example.com",
            ProviderUrlPolicy.normalizeHttpsBaseUrl(" HTTPS://Example.COM:443/ ")
        )
    }

    @Test
    fun preservesNonDefaultPort() {
        assertEquals(
            "https://example.com:8443",
            ProviderUrlPolicy.normalizeHttpsBaseUrl("https://example.com:8443")
        )
    }

    @Test
    fun rejectsHttp() {
        assertThrows(IllegalArgumentException::class.java) {
            ProviderUrlPolicy.normalizeHttpsBaseUrl("http://example.com")
        }
    }

    @Test
    fun rejectsPathAndCredentials() {
        assertThrows(IllegalArgumentException::class.java) {
            ProviderUrlPolicy.normalizeHttpsBaseUrl("https://example.com/instance")
        }
        assertThrows(IllegalArgumentException::class.java) {
            ProviderUrlPolicy.normalizeHttpsBaseUrl("https://user@example.com")
        }
    }
}
