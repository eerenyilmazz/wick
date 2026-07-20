package com.wick.core.data.safety

import com.wick.core.model.AppResult
import com.wick.core.model.ReportReason

interface SafetyRepository {
    suspend fun block(userId: String): AppResult<Unit>
    suspend fun unblock(userId: String): AppResult<Unit>
    suspend fun report(userId: String, reason: ReportReason, details: String?): AppResult<Unit>
}
