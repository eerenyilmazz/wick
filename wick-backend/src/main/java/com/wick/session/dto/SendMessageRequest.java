package com.wick.session.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
        @NotBlank @Size(max = 2000) String content,
        @Size(max = 36) String clientMessageId
) {
}
