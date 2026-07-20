package com.wick.token.service;

import com.wick.testsupport.TestEntities;

import com.wick.common.exception.InsufficientTokensException;
import com.wick.token.domain.TokenReason;
import com.wick.token.domain.TokenTransaction;
import com.wick.token.repository.TokenBalanceRepository;
import com.wick.token.repository.TokenTransactionRepository;
import com.wick.token.service.impl.TokenBalanceInitializer;
import com.wick.token.service.impl.TokenGrantGate;
import com.wick.token.service.impl.TokenServiceImpl;
import com.wick.user.domain.User;
import com.wick.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {

    @Mock
    private TokenBalanceRepository tokenBalanceRepository;

    @Mock
    private TokenTransactionRepository tokenTransactionRepository;

    @Mock
    private UserService userService;

    @Mock
    private TokenBalanceInitializer tokenBalanceInitializer;

    @Mock
    private TokenGrantGate tokenGrantGate;

    private TokenServiceImpl tokenService;

    private User user;

    @BeforeEach
    void setUp() {
        tokenService = new TokenServiceImpl(tokenBalanceRepository, tokenTransactionRepository, userService, tokenBalanceInitializer, tokenGrantGate);
        user = TestEntities.user("a@example.com", "hash", "A");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    }

    @Test
    void spend_throwsInsufficientTokens_whenBalanceTooLow() {
        when(userService.getById(user.getId())).thenReturn(user);
        when(tokenBalanceRepository.debit(user.getId(), 5)).thenReturn(0);

        assertThatThrownBy(() -> tokenService.spend(user.getId(), 5, TokenReason.SESSION_EXTENSION))
                .isInstanceOf(InsufficientTokensException.class);

        verify(tokenBalanceInitializer).ensureBalanceExists(user);
        verify(tokenTransactionRepository, never()).save(any());
    }

    @Test
    void spend_recordsNegativeTransaction_whenBalanceSufficient() {
        when(userService.getById(user.getId())).thenReturn(user);
        when(tokenBalanceRepository.debit(user.getId(), 1)).thenReturn(1);

        tokenService.spend(user.getId(), 1, TokenReason.SESSION_EXTENSION);

        verify(tokenTransactionRepository).save(argThatAmountIs(-1));
    }

    @Test
    void earn_recordsPositiveTransaction() {
        when(userService.getById(user.getId())).thenReturn(user);

        tokenService.earn(user.getId(), 5, TokenReason.VERIFICATION_BONUS);

        verify(tokenBalanceInitializer).ensureBalanceExists(user);
        verify(tokenBalanceRepository).credit(user.getId(), 5);
        verify(tokenTransactionRepository).save(argThatAmountIs(5));
    }

    @Test
    void grantDailyLoginIfEligible_returnsFalse_whenAlreadyGrantedToday() {
        when(tokenTransactionRepository.existsByUserIdAndReasonAndCreatedAtAfter(eq(user.getId()), eq(TokenReason.DAILY_LOGIN), any(Instant.class)))
                .thenReturn(true);

        boolean granted = tokenService.grantDailyLoginIfEligible(user.getId());

        org.assertj.core.api.Assertions.assertThat(granted).isFalse();
        verify(tokenBalanceRepository, never()).credit(any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void grantDailyLoginIfEligible_returnsFalse_whenLostRaceToConcurrentClaim() {
        when(tokenTransactionRepository.existsByUserIdAndReasonAndCreatedAtAfter(eq(user.getId()), eq(TokenReason.DAILY_LOGIN), any(Instant.class)))
                .thenReturn(false);
        when(tokenGrantGate.tryClaimDailyLogin(user.getId())).thenReturn(false);

        boolean granted = tokenService.grantDailyLoginIfEligible(user.getId());

        org.assertj.core.api.Assertions.assertThat(granted).isFalse();
        verify(tokenBalanceRepository, never()).credit(any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void claimDailyLogin_returnsUpdatedBalance_afterGranting() {
        com.wick.token.domain.TokenBalance balanceAfterCredit = TestEntities.tokenBalance(user);
        ReflectionTestUtils.setField(balanceAfterCredit, "balance", 1);

        when(tokenTransactionRepository.existsByUserIdAndReasonAndCreatedAtAfter(eq(user.getId()), eq(TokenReason.DAILY_LOGIN), any(Instant.class)))
                .thenReturn(false);
        when(tokenGrantGate.tryClaimDailyLogin(user.getId())).thenReturn(true);
        when(tokenBalanceRepository.findByUserId(user.getId())).thenReturn(Optional.of(balanceAfterCredit));
        when(userService.getById(user.getId())).thenReturn(user);

        int balance = tokenService.claimDailyLogin(user.getId());

        verify(tokenBalanceRepository).credit(user.getId(), 1);
        org.assertj.core.api.Assertions.assertThat(balance).isEqualTo(1);
    }

    @Test
    void grantVerificationBonusIfEligible_returnsFalse_whenAlreadyGrantedBefore() {
        when(tokenTransactionRepository.existsByUserIdAndReason(user.getId(), TokenReason.VERIFICATION_BONUS))
                .thenReturn(true);

        boolean granted = tokenService.grantVerificationBonusIfEligible(user.getId());

        org.assertj.core.api.Assertions.assertThat(granted).isFalse();
    }

    @Test
    void grantVerificationBonusIfEligible_returnsFalse_whenLostRaceToConcurrentClaim() {
        when(tokenTransactionRepository.existsByUserIdAndReason(user.getId(), TokenReason.VERIFICATION_BONUS))
                .thenReturn(false);
        when(tokenGrantGate.tryClaimVerificationBonus(user.getId())).thenReturn(false);

        boolean granted = tokenService.grantVerificationBonusIfEligible(user.getId());

        org.assertj.core.api.Assertions.assertThat(granted).isFalse();
        verify(tokenBalanceRepository, never()).credit(any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void grantVerificationBonusIfEligible_grantsBonus_whenEligibleAndClaimWon() {
        when(tokenTransactionRepository.existsByUserIdAndReason(user.getId(), TokenReason.VERIFICATION_BONUS))
                .thenReturn(false);
        when(tokenGrantGate.tryClaimVerificationBonus(user.getId())).thenReturn(true);
        when(userService.getById(user.getId())).thenReturn(user);

        boolean granted = tokenService.grantVerificationBonusIfEligible(user.getId());

        org.assertj.core.api.Assertions.assertThat(granted).isTrue();
        verify(tokenBalanceRepository).credit(user.getId(), 5);
    }

    private static TokenTransaction argThatAmountIs(int expectedAmount) {
        return org.mockito.ArgumentMatchers.argThat(tx -> tx.getAmount() == expectedAmount);
    }
}
