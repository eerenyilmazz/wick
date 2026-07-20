package com.wick.core.data.user

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.wick.core.data.auth.toDomain
import com.wick.core.model.AppResult
import com.wick.core.model.Gender
import com.wick.core.model.UiError
import com.wick.core.model.User
import com.wick.core.network.di.BaseUrl
import com.wick.core.network.user.DeleteAccountRequestDto
import com.wick.core.network.user.UpdateProfileRequestDto
import com.wick.core.network.user.UserApi
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
import java.time.LocalDate
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    @BaseUrl private val baseUrl: String,
    @ApplicationContext private val context: Context,
) : ProfileRepository {

    override suspend fun getCurrentUser(): AppResult<User> {
        return try {
            AppResult.Success(userApi.getCurrentUser().toDomain(baseUrl))
        } catch (httpError: HttpException) {
            AppResult.Failure(if (httpError.code() == 401) UiError.Unauthorized else UiError.Unknown)
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    override suspend fun deleteAccount(password: String): AppResult<Unit> {
        return try {
            userApi.deleteAccount(DeleteAccountRequestDto(password))
            AppResult.Success(Unit)
        } catch (httpError: HttpException) {
            AppResult.Failure(if (httpError.code() == 401) UiError.Unauthorized else UiError.Unknown)
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    override suspend fun updateProfile(
        dateOfBirth: LocalDate?,
        gender: Gender?,
        bio: String?,
        interests: List<String>?,
        displayName: String?,
        province: String?,
        district: String?,
        latitude: Double?,
        longitude: Double?,
    ): AppResult<User> {
        return try {
            val response = userApi.updateProfile(
                UpdateProfileRequestDto(
                    displayName = displayName,
                    dateOfBirth = dateOfBirth?.toString(),
                    gender = gender?.name,
                    bio = bio,
                    interests = interests,
                    province = province,
                    district = district,
                    latitude = latitude,
                    longitude = longitude,
                ),
            )
            AppResult.Success(response.toDomain(baseUrl))
        } catch (httpError: HttpException) {
            AppResult.Failure(if (httpError.code() == 401) UiError.Unauthorized else UiError.Unknown)
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    override suspend fun exportData(): AppResult<Unit> {
        return try {
            userApi.exportData()
            AppResult.Success(Unit)
        } catch (httpError: HttpException) {
            AppResult.Failure(if (httpError.code() == 401) UiError.Unauthorized else UiError.Unknown)
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        }
    }

    override suspend fun submitVerification(photoUri: Uri): AppResult<Unit> {
        return try {
            val part = withContext(Dispatchers.IO) { photoUri.toMultipartPart() }
            userApi.submitVerification(part)
            AppResult.Success(Unit)
        } catch (httpError: HttpException) {
            AppResult.Failure(if (httpError.code() == 401) UiError.Unauthorized else UiError.Unknown)
        } catch (networkError: IOException) {
            AppResult.Failure(UiError.NoConnection)
        } catch (e: Exception) {
            AppResult.Failure(UiError.Unknown)
        }
    }

    private fun Uri.toMultipartPart(): MultipartBody.Part {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(this)!!.use {
            BitmapFactory.decodeStream(it, null, options)
        }
        var sampleSize = 1
        while (options.outWidth / sampleSize > 1024 || options.outHeight / sampleSize > 1024) {
            sampleSize *= 2
        }
        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val bitmap = context.contentResolver.openInputStream(this)!!.use {
            BitmapFactory.decodeStream(it, null, decodeOpts)!!
        }
        val cacheFile = File.createTempFile("verify_", ".jpg", context.cacheDir)
        FileOutputStream(cacheFile).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output)
        }
        bitmap.recycle()
        val requestBody = cacheFile.asRequestBody("image/jpeg".toMediaType())
        return MultipartBody.Part.createFormData("photo", cacheFile.name, requestBody)
    }
}
