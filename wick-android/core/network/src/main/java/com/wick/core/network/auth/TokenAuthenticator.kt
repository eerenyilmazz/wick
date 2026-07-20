package com.wick.core.network.auth

import com.wick.core.network.di.BaseUrl
import com.wick.core.network.dto.RefreshTokenRequestDto
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Route
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock

@Singleton
class TokenAuthenticator @Inject constructor(
    @BaseUrl private val baseUrl: String,
    private val json: Json,
    private val tokenProvider: TokenProvider,
) : Authenticator {

    private val refreshLock = ReentrantLock()

    private val refreshApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AuthApi::class.java)
    }

    override fun authenticate(route: Route?, response: okhttp3.Response): Request? {
        if (responseCount(response) >= 2) return null

        val refreshToken = tokenProvider.getRefreshToken() ?: return null

        return refreshLock.withLock {
            // Another thread may have already refreshed — check if token changed
            val currentAccessToken = tokenProvider.getAccessToken()
            val requestAccessToken = response.request.header("Authorization")?.removePrefix("Bearer ")
            if (currentAccessToken != null && currentAccessToken != requestAccessToken) {
                return@withLock response.request.newBuilder()
                    .header("Authorization", "Bearer $currentAccessToken")
                    .build()
            }

            val newTokens = runBlocking {
                runCatching { refreshApi.refresh(RefreshTokenRequestDto(refreshToken)) }.getOrNull()
            } ?: run {
                tokenProvider.signalSessionExpired()
                return@withLock null
            }

            tokenProvider.updateAccessToken(newTokens.accessToken, newTokens.refreshToken)

            response.request.newBuilder()
                .header("Authorization", "Bearer ${newTokens.accessToken}")
                .build()
        }
    }

    private fun responseCount(response: okhttp3.Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
