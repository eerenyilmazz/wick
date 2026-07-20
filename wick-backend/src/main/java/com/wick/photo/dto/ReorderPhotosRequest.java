package com.wick.photo.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record ReorderPhotosRequest(
        @NotEmpty List<UUID> photoIds
) {
}
