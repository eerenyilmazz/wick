package com.wick.match.service.impl;

import com.wick.testsupport.TestEntities;

import com.wick.match.domain.Like;
import com.wick.match.domain.Match;
import com.wick.match.repository.LikeRepository;
import com.wick.match.repository.MatchRepository;
import com.wick.match.repository.PassRepository;
import com.wick.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchRaceSafeWriterTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private PassRepository passRepository;

    @Mock
    private MatchRepository matchRepository;

    private MatchRaceSafeWriter matchRaceSafeWriter;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        matchRaceSafeWriter = new MatchRaceSafeWriter(likeRepository, passRepository, matchRepository);
        userA = TestEntities.user("a@example.com", "hash", "A");
        userB = TestEntities.user("b@example.com", "hash", "B");
        ReflectionTestUtils.setField(userA, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(userB, "id", UUID.randomUUID());
    }

    @Test
    void trySaveLike_delegatesToInsertIfAbsent_regardlessOfWhetherRowAlreadyExists() {
        when(likeRepository.insertIfAbsent(any(UUID.class), eq(userA.getId()), eq(userB.getId()))).thenReturn(0);

        matchRaceSafeWriter.trySaveLike(TestEntities.like(userA, userB));
    }

    @Test
    void trySaveMatch_returnsEmpty_whenConcurrentMatchAlreadyExists() {
        when(matchRepository.insertIfAbsent(any(UUID.class), eq(userA.getId()), eq(userB.getId()))).thenReturn(0);

        Optional<Match> result = matchRaceSafeWriter.trySaveMatch(userA, userB);

        assertThat(result).isEmpty();
    }

    @Test
    void trySaveMatch_returnsSavedMatch_whenNoRaceOccurs() {
        Match match = TestEntities.match(userA, userB);
        when(matchRepository.insertIfAbsent(any(UUID.class), eq(userA.getId()), eq(userB.getId()))).thenReturn(1);
        when(matchRepository.findById(any(UUID.class))).thenReturn(Optional.of(match));

        Optional<Match> result = matchRaceSafeWriter.trySaveMatch(userA, userB);

        assertThat(result).contains(match);
    }
}
