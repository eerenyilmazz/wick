package com.wick.core.network.auth

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.json.Json
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class TokenAuthenticatorTest {

    private lateinit var tokenProvider: TokenProvider
    private lateinit var authenticator: TokenAuthenticator

    @Before
    fun setUp() {
        tokenProvider = mockk(relaxed = true)
        authenticator = TokenAuthenticator(
            baseUrl = "http://localhost:8080/",
            json = Json { ignoreUnknownKeys = true },
            tokenProvider = tokenProvider,
        )
    }

    private fun buildResponse(requestToken: String, priorResponse: Response? = null): Response {
        val request = Request.Builder()
            .url("http://localhost:8080/api/test")
            .header("Authorization", "Bearer $requestToken")
            .build()
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .priorResponse(priorResponse)
            .build()
    }

    @Test
    fun `returns null when no refresh token available`() {
        every { tokenProvider.getRefreshToken() } returns null

        val result = authenticator.authenticate(null, buildResponse("old-token"))

        assertNull(result)
    }

    @Test
    fun `returns null after two retries to prevent infinite loop`() {
        every { tokenProvider.getRefreshToken() } returns null

        val first = buildResponse("token")
        val second = buildResponse("token", priorResponse = first)

        val result = authenticator.authenticate(null, second)

        assertNull(result)
    }

    @Test
    fun `reuses refreshed token when another thread already refreshed`() {
        val newToken = "already-refreshed-token"
        every { tokenProvider.getRefreshToken() } returns "refresh-token"
        every { tokenProvider.getAccessToken() } returns newToken

        val response = buildResponse("old-token")
        val result = authenticator.authenticate(null, response)

        assertNotNull(result)
        assertEquals("Bearer $newToken", result!!.header("Authorization"))
        // signalSessionExpired must NOT have been called — token was reused
        verify(exactly = 0) { tokenProvider.signalSessionExpired() }
    }
}
