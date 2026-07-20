package com.wick.core.data.match

import com.wick.core.model.AppResult
import com.wick.core.model.MatchSummary

interface MatchListRepository {
    suspend fun getMatches(): AppResult<List<MatchSummary>>
    suspend fun unmatch(matchId: String): AppResult<Unit>
}
