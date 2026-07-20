package com.wick.core.data.user

import com.wick.core.model.AppResult
import com.wick.core.model.DiscoveryPreference
import com.wick.core.model.Gender
import com.wick.core.model.UiError
import com.wick.core.network.user.DiscoveryPreferenceResponseDto
import com.wick.core.network.user.UpdateDiscoveryPreferenceRequestDto
import com.wick.core.network.user.UserApi
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class DiscoveryPreferenceRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
) : DiscoveryPreferenceRepository {

    override suspend fun getPreference(): AppResult<DiscoveryPreference> {
        return try {
            AppResult.Success(userApi.getDiscoveryPreferences().toDomain())
        } catch (httpError: HttpException) {
            AppResult.Failure(httpError.toUiError())
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    override suspend fun updatePreference(
        minAge: Int,
        maxAge: Int,
        radiusKm: Int?,
        genders: Set<Gender>,
    ): AppResult<DiscoveryPreference> {
        return try {
            val response = userApi.updateDiscoveryPreferences(
                UpdateDiscoveryPreferenceRequestDto(
                    minAge = minAge,
                    maxAge = maxAge,
                    radiusKm = radiusKm,
                    genders = genders.map { it.name },
                ),
            )
            AppResult.Success(response.toDomain())
        } catch (httpError: HttpException) {
            AppResult.Failure(httpError.toUiError())
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    private fun DiscoveryPreferenceResponseDto.toDomain(): DiscoveryPreference = DiscoveryPreference(
        minAge = minAge,
        maxAge = maxAge,
        radiusKm = radiusKm,
        genders = genders.mapNotNull { name -> runCatching { Gender.valueOf(name) }.getOrNull() }.toSet(),
    )

    private fun HttpException.toUiError(): UiError = when (code()) {
        401 -> UiError.Unauthorized
        403 -> UiError.Forbidden
        else -> UiError.Unknown
    }
}
