package com.wick.core.data.match

import com.wick.core.model.AppResult
import com.wick.core.model.SwipeDirection
import com.wick.core.model.SwipeResult
import com.wick.core.model.User

interface DiscoverRepository {
    suspend fun getDiscoverableUsers(): AppResult<List<User>>
    suspend fun swipe(targetUserId: String, direction: SwipeDirection): AppResult<SwipeResult>
}
