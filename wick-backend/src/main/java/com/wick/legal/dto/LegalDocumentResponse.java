package com.wick.legal.dto;

import com.wick.legal.domain.LegalDocumentType;

import java.time.Instant;

public record LegalDocumentResponse(LegalDocumentType type, int version, String content, Instant publishedAt) {
}
