package com.wick.core.data.config

import com.wick.core.model.AppConfig
import com.wick.core.model.AppResult
import com.wick.core.model.UiError
import com.wick.core.network.config.AppConfigApi
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ConfigRepositoryImpl @Inject constructor(
    private val appConfigApi: AppConfigApi,
) : ConfigRepository {

    override suspend fun getConfig(): AppResult<AppConfig> {
        return try {
            val response = appConfigApi.getConfig()
            AppResult.Success(
                AppConfig(
                    minimumAge = response.minimumAge,
                    maximumAge = response.maximumAge,
                    maxPhotosPerUser = response.maxPhotosPerUser,
                    sessionExtensionTokenCost = response.sessionExtensionTokenCost,
                    sessionExtensionMinutes = response.sessionExtensionMinutes,
                    extraInvitationTokenCost = response.extraInvitationTokenCost,
                    maintenanceMode = response.maintenanceMode,
                    minVersion = response.minVersion,
                    maxRetryInvitations = response.maxRetryInvitations,
                    superLikeTokenCost = response.superLikeTokenCost,
                ),
            )
        } catch (httpError: HttpException) {
            AppResult.Failure(UiError.Unknown)
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }
}
