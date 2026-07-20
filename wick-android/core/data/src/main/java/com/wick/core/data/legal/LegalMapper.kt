package com.wick.core.data.legal

import com.wick.core.model.LegalDocument
import com.wick.core.model.LegalDocumentType
import com.wick.core.network.legal.LegalDocumentResponseDto

fun LegalDocumentResponseDto.toDomain(): LegalDocument = LegalDocument(
    type = LegalDocumentType.valueOf(type),
    version = version,
    content = content,
)
