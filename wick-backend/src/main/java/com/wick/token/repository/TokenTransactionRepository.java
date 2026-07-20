package com.wick.token.repository;

import com.wick.token.domain.TokenReason;
import com.wick.token.domain.TokenTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface TokenTransactionRepository extends JpaRepository<TokenTransaction, UUID> {

    boolean existsByUserIdAndReason(UUID userId, TokenReason reason);

    boolean existsByUserIdAndReasonAndCreatedAtAfter(UUID userId, TokenReason reason, Instant after);

    Page<TokenTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
