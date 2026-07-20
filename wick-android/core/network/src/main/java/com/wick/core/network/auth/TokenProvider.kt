package com.wick.core.network.auth

interface TokenProvider {
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun updateAccessToken(accessToken: String, refreshToken: String)
    fun clear()
    fun signalSessionExpired()
}
