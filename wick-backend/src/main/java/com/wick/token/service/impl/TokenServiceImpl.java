package com.wick.token.service.impl;

import com.wick.common.dto.PageResponse;
import com.wick.common.exception.InsufficientTokensException;
import com.wick.token.domain.TokenBalance;
import com.wick.token.domain.TokenReason;
import com.wick.token.domain.TokenTransaction;
import com.wick.token.dto.TokenTransactionResponse;
import com.wick.token.repository.TokenBalanceRepository;
import com.wick.token.repository.TokenTransactionRepository;
import com.wick.token.service.TokenService;
import com.wick.user.domain.User;
import com.wick.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final TokenBalanceRepository tokenBalanceRepository;
    private final TokenTransactionRepository tokenTransactionRepository;
    private final UserService userService;
    private final TokenBalanceInitializer tokenBalanceInitializer;
    private final TokenGrantGate tokenGrantGate;

    @Override
    public int getBalance(UUID userId) {
        return tokenBalanceRepository.findByUserId(userId).map(TokenBalance::getBalance).orElse(0);
    }

    @Override
    @Transactional
    public void spend(UUID userId, int amount, TokenReason reason) {
        User user = userService.getById(userId);
        tokenBalanceInitializer.ensureBalanceExists(user);
        int updated = tokenBalanceRepository.debit(userId, amount);
        if (updated == 0) {
            throw new InsufficientTokensException("Not enough tokens for this action");
        }
        tokenTransactionRepository.save(newTransaction(user, -amount, reason));
    }

    @Override
    @Transactional
    public void earn(UUID userId, int amount, TokenReason reason) {
        User user = userService.getById(userId);
        tokenBalanceInitializer.ensureBalanceExists(user);
        tokenBalanceRepository.credit(userId, amount);
        tokenTransactionRepository.save(newTransaction(user, amount, reason));
    }

    private TokenTransaction newTransaction(User user, int amount, TokenReason reason) {
        TokenTransaction transaction = new TokenTransaction();
        transaction.setUser(user);
        transaction.setAmount(amount);
        transaction.setReason(reason);
        return transaction;
    }

    @Override
    public boolean isDailyLoginClaimedToday(UUID userId) {
        Instant startOfToday = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant();
        return tokenTransactionRepository.existsByUserIdAndReasonAndCreatedAtAfter(userId, TokenReason.DAILY_LOGIN, startOfToday);
    }

    @Override
    @Transactional
    public boolean grantDailyLoginIfEligible(UUID userId) {
        Instant startOfToday = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant();
        if (tokenTransactionRepository.existsByUserIdAndReasonAndCreatedAtAfter(userId, TokenReason.DAILY_LOGIN, startOfToday)) {
            return false;
        }
        if (!tokenGrantGate.tryClaimDailyLogin(userId)) {
            return false;
        }
        earn(userId, 1, TokenReason.DAILY_LOGIN);
        return true;
    }

    @Override
    @Transactional
    public int claimDailyLogin(UUID userId) {
        grantDailyLoginIfEligible(userId);
        return getBalance(userId);
    }

    @Override
    @Transactional
    public boolean grantVerificationBonusIfEligible(UUID userId) {
        if (tokenTransactionRepository.existsByUserIdAndReason(userId, TokenReason.VERIFICATION_BONUS)) {
            return false;
        }
        if (!tokenGrantGate.tryClaimVerificationBonus(userId)) {
            return false;
        }
        earn(userId, 5, TokenReason.VERIFICATION_BONUS);
        return true;
    }

    @Override
    public PageResponse<TokenTransactionResponse> getTransactions(UUID userId, Pageable pageable) {
        return PageResponse.from(tokenTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(tx -> new TokenTransactionResponse(tx.getId(), tx.getAmount(), tx.getReason(), tx.getCreatedAt())));
    }
}
