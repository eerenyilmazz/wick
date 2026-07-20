package com.wick.core.data.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.wick.core.model.AppResult
import com.wick.core.model.Photo
import com.wick.core.model.UiError
import com.wick.core.network.di.BaseUrl
import com.wick.core.network.photo.PhotoApi
import com.wick.core.network.photo.ReorderPhotosRequestDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

class PhotoRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @BaseUrl private val baseUrl: String,
    private val photoApi: PhotoApi,
) : PhotoRepository {

    override suspend fun getPhotos(): AppResult<List<Photo>> {
        return try {
            AppResult.Success(photoApi.getPhotos().map { it.toDomain(baseUrl) })
        } catch (httpError: HttpException) {
            AppResult.Failure(httpError.toUiError())
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    override suspend fun uploadPhoto(uri: Uri): AppResult<Photo> {
        return try {
            val part = withContext(Dispatchers.IO) { uri.toMultipartPart() }
            AppResult.Success(photoApi.uploadPhoto(part).toDomain(baseUrl))
        } catch (httpError: HttpException) {
            AppResult.Failure(httpError.toUiError())
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    override suspend fun deletePhoto(photoId: String): AppResult<Unit> {
        return try {
            photoApi.deletePhoto(photoId)
            AppResult.Success(Unit)
        } catch (httpError: HttpException) {
            AppResult.Failure(httpError.toUiError())
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    override suspend fun reorderPhotos(photoIds: List<String>): AppResult<List<Photo>> {
        return try {
            val response = photoApi.reorderPhotos(ReorderPhotosRequestDto(photoIds))
            AppResult.Success(response.map { it.toDomain(baseUrl) })
        } catch (httpError: HttpException) {
            AppResult.Failure(httpError.toUiError())
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    private fun Uri.toMultipartPart(): MultipartBody.Part {
        val bitmap = decodeDownsampledBitmap(this, maxDimension = MAX_UPLOAD_DIMENSION_PX)
        val cacheFile = File.createTempFile("upload_photo_", ".jpg", context.cacheDir)
        FileOutputStream(cacheFile).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, UPLOAD_JPEG_QUALITY, output)
        }
        bitmap.recycle()

        val requestBody = cacheFile.asRequestBody("image/jpeg".toMediaType())
        return MultipartBody.Part.createFormData("file", cacheFile.name, requestBody)
    }

    private fun decodeDownsampledBitmap(uri: Uri, maxDimension: Int): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)!!.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }

        var sampleSize = 1
        while (bounds.outWidth / sampleSize > maxDimension || bounds.outHeight / sampleSize > maxDimension) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = context.contentResolver.openInputStream(uri)!!.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: error("Could not decode image")

        val rotationDegrees = readExifRotationDegrees(uri)
        if (rotationDegrees == 0) return decoded

        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        val rotated = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        if (rotated != decoded) decoded.recycle()
        return rotated
    }

    private fun readExifRotationDegrees(uri: Uri): Int {
        val exif = context.contentResolver.openInputStream(uri)?.use { input ->
            ExifInterface(input)
        } ?: return 0

        return when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    private fun HttpException.toUiError(): UiError = when (code()) {
        401 -> UiError.Unauthorized
        409 -> UiError.Conflict
        429 -> UiError.RateLimited
        else -> UiError.Unknown
    }

    private companion object {
        const val MAX_UPLOAD_DIMENSION_PX = 1600
        const val UPLOAD_JPEG_QUALITY = 85
    }
}
