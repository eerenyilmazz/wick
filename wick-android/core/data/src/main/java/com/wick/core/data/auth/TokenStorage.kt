package com.wick.core.data.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.wick.core.model.AuthTokens
import com.wick.core.network.auth.TokenProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStorage @Inject constructor(
    @ApplicationContext context: Context,
) : TokenProvider {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "wick_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    @Volatile
    private var inMemoryTokens: AuthTokens? = null

    private val _sessionExpiredFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpiredFlow: Flow<Unit> = _sessionExpiredFlow.asSharedFlow()

    fun save(tokens: AuthTokens, persist: Boolean = true) {
        if (persist) {
            prefs.edit()
                .putString(KEY_ACCESS_TOKEN, tokens.accessToken)
                .putString(KEY_REFRESH_TOKEN, tokens.refreshToken)
                .apply()
            inMemoryTokens = null
        } else {
            prefs.edit().clear().apply()
            inMemoryTokens = tokens
        }
    }

    fun read(): AuthTokens? {
        inMemoryTokens?.let { return it }
        val accessToken = prefs.getString(KEY_ACCESS_TOKEN, null) ?: return null
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null) ?: return null
        return AuthTokens(accessToken, refreshToken)
    }

    override fun clear() {
        inMemoryTokens = null
        prefs.edit().clear().apply()
    }

    override fun signalSessionExpired() {
        clear()
        _sessionExpiredFlow.tryEmit(Unit)
    }

    override fun getAccessToken(): String? = inMemoryTokens?.accessToken ?: prefs.getString(KEY_ACCESS_TOKEN, null)

    override fun getRefreshToken(): String? = inMemoryTokens?.refreshToken ?: prefs.getString(KEY_REFRESH_TOKEN, null)

    override fun updateAccessToken(accessToken: String, refreshToken: String) {
        save(AuthTokens(accessToken, refreshToken), persist = inMemoryTokens == null)
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
