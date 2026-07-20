package com.wick.token.service;

import com.wick.testsupport.TestEntities;

import com.wick.AbstractIntegrationTest;
import com.wick.token.domain.TokenReason;
import com.wick.token.repository.TokenBalanceRepository;
import com.wick.user.domain.User;
import com.wick.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBalanceConcurrencyIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenBalanceRepository tokenBalanceRepository;

    @Test
    void earn_createsExactlyOneBalanceRow_whenManyConcurrentFirstEarnsRace() throws InterruptedException {
        User user = userRepository.save(TestEntities.user("token-race@wick.com", "hash", "Tester"));

        int concurrentRequests = 10;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentRequests);
        CountDownLatch ready = new CountDownLatch(concurrentRequests);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger errors = new AtomicInteger();

        for (int i = 0; i < concurrentRequests; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    tokenService.earn(user.getId(), 1, TokenReason.DAILY_LOGIN);
                } catch (Exception ex) {
                    errors.incrementAndGet();
                }
            });
        }

        ready.await();
        start.countDown();
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        assertThat(errors.get()).isZero();
        assertThat(tokenBalanceRepository.findByUserId(user.getId())).isPresent();
        assertThat(tokenService.getBalance(user.getId())).isEqualTo(concurrentRequests);
    }
}
