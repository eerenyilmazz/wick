package com.wick.core.data.auth

import com.wick.core.model.AppResult
import com.wick.core.model.AuthTokens
import com.wick.core.model.UiError
import com.wick.core.model.User
import com.wick.core.network.auth.AuthApi
import com.wick.core.network.auth.ForgotPasswordRequestDto
import com.wick.core.network.auth.ResetPasswordRequestDto
import com.wick.core.network.di.BaseUrl
import com.wick.core.network.dto.LoginRequestDto
import com.wick.core.network.dto.RefreshTokenRequestDto
import com.wick.core.network.dto.RegisterRequestDto
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage,
    @BaseUrl private val baseUrl: String,
) : AuthRepository {

    override val sessionExpiredFlow: Flow<Unit> = tokenStorage.sessionExpiredFlow

    override suspend fun login(email: String, password: String, keepSignedIn: Boolean): AppResult<User> {
        return try {
            val response = authApi.login(LoginRequestDto(email = email, password = password))
            tokenStorage.save(AuthTokens(response.accessToken, response.refreshToken), persist = keepSignedIn)
            AppResult.Success(response.user.toDomain(baseUrl))
        } catch (httpError: HttpException) {
            AppResult.Failure(httpError.toLoginUiError())
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    override suspend fun isEmailAvailable(email: String): AppResult<Boolean> {
        return try {
            AppResult.Success(authApi.checkEmail(email).available)
        } catch (httpError: HttpException) {
            AppResult.Failure(httpError.toRegisterUiError())
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String,
        acceptedPrivacyPolicyVersion: Int,
        acceptedTermsVersion: Int,
    ): AppResult<User> {
        return try {
            val response = authApi.register(
                RegisterRequestDto(
                    email = email,
                    password = password,
                    displayName = displayName,
                    acceptedPrivacyPolicyVersion = acceptedPrivacyPolicyVersion,
                    acceptedTermsVersion = acceptedTermsVersion,
                ),
            )
            tokenStorage.save(AuthTokens(response.accessToken, response.refreshToken))
            AppResult.Success(response.user.toDomain(baseUrl))
        } catch (httpError: HttpException) {
            AppResult.Failure(httpError.toRegisterUiError())
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    override suspend fun logout() {
        val refreshToken = tokenStorage.getRefreshToken()
        tokenStorage.clear()
        if (refreshToken != null) {
            runCatching { authApi.logout(RefreshTokenRequestDto(refreshToken)) }
        }
    }

    override suspend fun forgotPassword(email: String): AppResult<Unit> {
        return try {
            authApi.forgotPassword(ForgotPasswordRequestDto(email))
            AppResult.Success(Unit)
        } catch (httpError: HttpException) {
            AppResult.Failure(UiError.Unknown)
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    override suspend fun resetPassword(token: String, newPassword: String): AppResult<Unit> {
        return try {
            authApi.resetPassword(ResetPasswordRequestDto(token, newPassword))
            AppResult.Success(Unit)
        } catch (httpError: HttpException) {
            AppResult.Failure(if (httpError.code() == 400) UiError.InvalidLink else UiError.Unknown)
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    override fun hasSession(): Boolean = tokenStorage.read() != null

    private fun HttpException.toLoginUiError(): UiError = when (code()) {
        401 -> UiError.Unauthorized
        429 -> UiError.RateLimited
        else -> UiError.Unknown
    }

    private fun HttpException.toRegisterUiError(): UiError = when (code()) {
        409 -> UiError.Conflict
        429 -> UiError.RateLimited
        else -> UiError.Unknown
    }
}
