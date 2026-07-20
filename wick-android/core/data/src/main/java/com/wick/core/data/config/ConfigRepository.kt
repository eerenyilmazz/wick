package com.wick.core.data.config

import com.wick.core.model.AppConfig
import com.wick.core.model.AppResult

interface ConfigRepository {
    suspend fun getConfig(): AppResult<AppConfig>
}
