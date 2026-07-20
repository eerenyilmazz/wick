package com.wick.core.data.photo

import com.wick.core.model.Photo
import com.wick.core.network.photo.PhotoResponseDto

fun PhotoResponseDto.toDomain(baseUrl: String): Photo = Photo(
    id = id,
    url = baseUrl.trimEnd('/') + url,
    displayOrder = displayOrder,
)
