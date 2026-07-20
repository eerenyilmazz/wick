package com.wick.match.service;

import com.wick.testsupport.TestEntities;

import com.wick.AbstractIntegrationTest;
import com.wick.match.domain.Match;
import com.wick.match.repository.MatchRepository;
import com.wick.user.domain.Gender;
import com.wick.user.domain.User;
import com.wick.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class MatchConcurrencyIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MatchService matchService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Test
    void swipe_createsExactlyOneMatch_whenBothUsersSwipeConcurrentlyManyTimes() throws InterruptedException {
        User userA = userRepository.save(newCompleteProfile("concurrency-a@wick.com", Gender.FEMALE));
        User userB = userRepository.save(newCompleteProfile("concurrency-b@wick.com", Gender.MALE));

        matchService.swipe(userA.getId(), userB.getId());

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
                    matchService.swipe(userB.getId(), userA.getId());
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

        List<Match> matches = matchRepository.findAllForUser(userA.getId());
        assertThat(matches).hasSize(1);

        Optional<Match> match = matchRepository.findByUsers(userA.getId(), userB.getId());
        assertThat(match).isPresent();
    }

    private User newCompleteProfile(String email, Gender gender) {
        User user = TestEntities.user(email, "hash", "Tester");
        user.setDateOfBirth(LocalDate.now().minusYears(25));
        user.setGender(gender);
        return user;
    }
}
