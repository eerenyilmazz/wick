package com.wick.core.network.legal

import kotlinx.serialization.Serializable

@Serializable
data class LegalDocumentResponseDto(
    val type: String,
    val version: Int,
    val content: String,
    val publishedAt: String,
)
