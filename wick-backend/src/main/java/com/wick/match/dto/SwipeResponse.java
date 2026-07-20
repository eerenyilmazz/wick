package com.wick.match.dto;

import java.util.UUID;

public record SwipeResponse(boolean matched, UUID matchId) {
}
