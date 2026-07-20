package com.wick.core.data.location

import com.wick.core.model.AppResult

interface LocationRepository {
    suspend fun getProvinces(): AppResult<List<String>>
    suspend fun getDistricts(province: String): AppResult<List<String>>
}
