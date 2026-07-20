package com.wick.core.model

enum class LegalDocumentType {
    PRIVACY_POLICY, TERMS_OF_SERVICE
}

data class LegalDocument(
    val type: LegalDocumentType,
    val version: Int,
    val content: String,
)
