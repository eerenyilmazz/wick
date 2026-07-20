package com.wick.token.service;

import com.wick.common.dto.PageResponse;
import com.wick.token.domain.TokenReason;
import com.wick.token.dto.TokenTransactionResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TokenService {

    int getBalance(UUID userId);

    void spend(UUID userId, int amount, TokenReason reason);

    void earn(UUID userId, int amount, TokenReason reason);

    boolean isDailyLoginClaimedToday(UUID userId);

    boolean grantDailyLoginIfEligible(UUID userId);

    int claimDailyLogin(UUID userId);

    boolean grantVerificationBonusIfEligible(UUID userId);

    PageResponse<TokenTransactionResponse> getTransactions(UUID userId, Pageable pageable);
}
