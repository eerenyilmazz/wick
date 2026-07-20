package com.wick.photo.dto;

import java.util.UUID;

public record PhotoResponse(
        UUID id,
        String url,
        int displayOrder
) {
}
