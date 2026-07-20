package com.wick.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class PageResponseDto<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
