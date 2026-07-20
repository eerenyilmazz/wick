package com.wick.core.network.safety

import kotlinx.serialization.Serializable

@Serializable
data class ReportRequestDto(val reason: String, val details: String? = null)
