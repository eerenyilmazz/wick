package com.wick.legal.service;

import com.wick.legal.domain.LegalDocument;
import com.wick.legal.domain.LegalDocumentType;
import com.wick.user.domain.User;

public interface LegalService {

    LegalDocument getLatest(LegalDocumentType type);

    void recordConsent(User user, LegalDocumentType type, int acceptedVersion);
}
