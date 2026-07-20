package com.wick.legal.mapper;

import com.wick.legal.domain.LegalDocument;
import com.wick.legal.dto.LegalDocumentResponse;
import org.springframework.stereotype.Component;

@Component
public class LegalDocumentMapper {

    public LegalDocumentResponse toResponse(LegalDocument document) {
        return new LegalDocumentResponse(document.getType(), document.getVersion(), document.getContent(), document.getPublishedAt());
    }
}
