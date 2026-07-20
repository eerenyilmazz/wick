package com.wick.core.data.legal

import com.wick.core.model.AppResult
import com.wick.core.model.LegalDocument
import com.wick.core.model.LegalDocumentType

interface LegalRepository {
    suspend fun getLatest(type: LegalDocumentType): AppResult<LegalDocument>
}
