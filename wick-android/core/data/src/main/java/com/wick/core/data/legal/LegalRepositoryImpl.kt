package com.wick.core.data.legal

import com.wick.core.model.AppResult
import com.wick.core.model.LegalDocument
import com.wick.core.model.LegalDocumentType
import com.wick.core.model.UiError
import com.wick.core.network.legal.LegalApi
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class LegalRepositoryImpl @Inject constructor(
    private val legalApi: LegalApi,
) : LegalRepository {

    override suspend fun getLatest(type: LegalDocumentType): AppResult<LegalDocument> {
        return try {
            val response = legalApi.getLatest(type.name)
            AppResult.Success(response.toDomain())
        } catch (httpError: HttpException) {
            AppResult.Failure(UiError.Unknown)
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }
}
