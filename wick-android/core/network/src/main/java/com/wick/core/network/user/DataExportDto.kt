package com.wick.core.network.user

import com.wick.core.network.dto.UserResponseDto
import kotlinx.serialization.Serializable

@Serializable
data class DataExportResponseDto(
    val profile: UserResponseDto,
    val tokenBalance: Int,
)
