package com.wick.token.service.impl;

import com.wick.testsupport.TestEntities;

import com.wick.token.domain.TokenBalance;
import com.wick.token.repository.TokenBalanceRepository;
import com.wick.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenBalanceInitializerTest {

    @Mock
    private TokenBalanceRepository tokenBalanceRepository;

    private TokenBalanceInitializer tokenBalanceInitializer;

    private User user;

    @BeforeEach
    void setUp() {
        tokenBalanceInitializer = new TokenBalanceInitializer(tokenBalanceRepository);
        user = TestEntities.user("a@example.com", "hash", "A");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    }

    @Test
    void ensureBalanceExists_doesNothing_whenBalanceAlreadyExists() {
        when(tokenBalanceRepository.findByUserId(user.getId())).thenReturn(Optional.of(TestEntities.tokenBalance(user)));

        tokenBalanceInitializer.ensureBalanceExists(user);

        verify(tokenBalanceRepository, never()).insertIfAbsent(any(), any());
    }

    @Test
    void ensureBalanceExists_createsBalance_whenMissing() {
        when(tokenBalanceRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        tokenBalanceInitializer.ensureBalanceExists(user);

        verify(tokenBalanceRepository).insertIfAbsent(any(UUID.class), eq(user.getId()));
    }

    @Test
    void ensureBalanceExists_doesNotThrow_whenConcurrentFirstEarnAlreadyCreatedTheRow() {
        when(tokenBalanceRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(tokenBalanceRepository.insertIfAbsent(any(UUID.class), eq(user.getId()))).thenReturn(0);

        tokenBalanceInitializer.ensureBalanceExists(user);
    }
}
