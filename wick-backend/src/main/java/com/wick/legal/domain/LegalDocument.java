package com.wick.legal.domain;

import com.wick.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "legal_documents", uniqueConstraints = @UniqueConstraint(columnNames = {"type", "version"}))
public class LegalDocument extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LegalDocumentType type;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(nullable = false)
    private Instant publishedAt;
}
