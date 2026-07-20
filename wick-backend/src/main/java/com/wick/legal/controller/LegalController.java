package com.wick.legal.controller;

import com.wick.legal.domain.LegalDocumentType;
import com.wick.legal.dto.LegalDocumentResponse;
import com.wick.legal.mapper.LegalDocumentMapper;
import com.wick.legal.service.LegalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/legal")
@RequiredArgsConstructor
public class LegalController {

    private final LegalService legalService;
    private final LegalDocumentMapper legalDocumentMapper;

    @GetMapping("/{type}")
    public LegalDocumentResponse getLatest(@PathVariable LegalDocumentType type) {
        return legalDocumentMapper.toResponse(legalService.getLatest(type));
    }
}
