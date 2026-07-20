package com.wick.legal.service.impl;

import com.wick.common.exception.ApiException;
import com.wick.common.exception.ResourceNotFoundException;
import com.wick.legal.domain.LegalDocument;
import com.wick.legal.domain.LegalDocumentType;
import com.wick.legal.domain.UserConsent;
import com.wick.legal.repository.LegalDocumentRepository;
import com.wick.legal.repository.UserConsentRepository;
import com.wick.legal.service.LegalService;
import com.wick.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LegalServiceImpl implements LegalService {

    private final LegalDocumentRepository legalDocumentRepository;
    private final UserConsentRepository userConsentRepository;

    @Override
    public LegalDocument getLatest(LegalDocumentType type) {
        return legalDocumentRepository.findTopByTypeOrderByVersionDesc(type)
                .orElseThrow(() -> new ResourceNotFoundException("No published document for " + type));
    }

    @Override
    @Transactional
    public void recordConsent(User user, LegalDocumentType type, int acceptedVersion) {
        LegalDocument latest = getLatest(type);
        if (latest.getVersion() != acceptedVersion) {
            throw new ApiException(type + " has been updated, please review the latest version", HttpStatus.CONFLICT);
        }
        UserConsent consent = new UserConsent();
        consent.setUser(user);
        consent.setDocumentType(type);
        consent.setVersion(acceptedVersion);
        userConsentRepository.save(consent);
    }
}
