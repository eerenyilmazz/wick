package com.wick.legal.repository;

import com.wick.legal.domain.LegalDocument;
import com.wick.legal.domain.LegalDocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LegalDocumentRepository extends JpaRepository<LegalDocument, UUID> {

    Optional<LegalDocument> findTopByTypeOrderByVersionDesc(LegalDocumentType type);
}
