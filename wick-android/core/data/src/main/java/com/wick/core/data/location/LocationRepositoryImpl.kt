package com.wick.core.data.location

import com.wick.core.model.AppResult
import com.wick.core.model.UiError
import com.wick.core.network.location.LocationApi
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val locationApi: LocationApi,
) : LocationRepository {

    override suspend fun getProvinces(): AppResult<List<String>> {
        return try {
            AppResult.Success(locationApi.getProvinces())
        } catch (httpError: HttpException) {
            AppResult.Failure(httpError.toUiError())
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    override suspend fun getDistricts(province: String): AppResult<List<String>> {
        return try {
            AppResult.Success(locationApi.getDistricts(province))
        } catch (httpError: HttpException) {
            AppResult.Failure(httpError.toUiError())
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    private fun HttpException.toUiError(): UiError = when (code()) {
        401 -> UiError.Unauthorized
        429 -> UiError.RateLimited
        else -> UiError.Unknown
    }
}
