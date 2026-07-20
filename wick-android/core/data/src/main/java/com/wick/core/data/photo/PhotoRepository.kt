package com.wick.core.data.photo

import android.net.Uri
import com.wick.core.model.AppResult
import com.wick.core.model.Photo

interface PhotoRepository {
    suspend fun getPhotos(): AppResult<List<Photo>>
    suspend fun uploadPhoto(uri: Uri): AppResult<Photo>
    suspend fun deletePhoto(photoId: String): AppResult<Unit>
    suspend fun reorderPhotos(photoIds: List<String>): AppResult<List<Photo>>
}
