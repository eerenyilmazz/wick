package com.wick.token.service.impl;

import com.wick.token.repository.TokenBalanceRepository;
import com.wick.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TokenBalanceInitializer {

    private final TokenBalanceRepository tokenBalanceRepository;

    @Transactional
    public void ensureBalanceExists(User user) {
        if (tokenBalanceRepository.findByUserId(user.getId()).isPresent()) {
            return;
        }
        tokenBalanceRepository.insertIfAbsent(UUID.randomUUID(), user.getId());
    }
}
