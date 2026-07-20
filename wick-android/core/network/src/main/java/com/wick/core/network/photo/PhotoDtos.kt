package com.wick.core.network.photo

import kotlinx.serialization.Serializable

@Serializable
data class PhotoResponseDto(
    val id: String,
    val url: String,
    val displayOrder: Int,
)

@Serializable
data class ReorderPhotosRequestDto(
    val photoIds: List<String>,
)
