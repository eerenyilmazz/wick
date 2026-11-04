package com.wick.match.service;

import com.wick.testsupport.TestEntities;

import com.wick.common.exception.ApiException;
import com.wick.match.domain.Match;
import com.wick.match.domain.Like;
import com.wick.match.repository.LikeRepository;
import com.wick.match.repository.MatchRepository;
import com.wick.match.repository.PassRepository;
import com.wick.eventlog.service.EventLogService;
import com.wick.invitation.repository.InvitationRepository;
import com.wick.match.service.impl.MatchRaceSafeWriter;
import com.wick.match.service.impl.MatchServiceImpl;
import com.wick.match.service.SwipeRateLimiter;
import com.wick.notification.service.NotificationService;
import com.wick.safety.service.BlockService;
import com.wick.session.repository.MessageRepository;
import com.wick.session.repository.SessionRepository;
import com.wick.user.domain.Gender;
import com.wick.user.domain.User;
import com.wick.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchServiceImplTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private PassRepository passRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserService userService;

    @Mock
    private MatchRaceSafeWriter matchRaceSafeWriter;

    @Mock
    private EventLogService eventLogService;

    @Mock
    private BlockService blockService;

    @Mock
    private SwipeRateLimiter swipeRateLimiter;

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private com.wick.token.service.TokenService tokenService;

    private MatchServiceImpl matchService;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        matchService = new MatchServiceImpl(likeRepository, passRepository, matchRepository, userService, matchRaceSafeWriter, eventLogService, notificationService, blockService, swipeRateLimiter, invitationRepository, sessionRepository, messageRepository, tokenService, new com.wick.token.config.TokenProperties());
        userA = TestEntities.user("a@example.com", "hash", "A");
        userB = TestEntities.user("b@example.com", "hash", "B");
        userA.setDateOfBirth(LocalDate.now().minusYears(25));
        userA.setGender(Gender.FEMALE);
        userB.setDateOfBirth(LocalDate.now().minusYears(27));
        userB.setGender(Gender.MALE);
        ReflectionTestUtils.setField(userA, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(userB, "id", UUID.randomUUID());
    }

    @Test
    void swipe_throwsBadRequest_whenSwipingSelf() {
        assertThatThrownBy(() -> matchService.swipe(userA.getId(), userA.getId()))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void swipe_throwsForbidden_whenEitherUserHasBlockedTheOther() {
        when(blockService.isBlockedEitherDirection(userA.getId(), userB.getId())).thenReturn(true);

        assertThatThrownBy(() -> matchService.swipe(userA.getId(), userB.getId()))
                .isInstanceOf(ApiException.class);

        verify(userService, never()).getById(any());
    }

    @Test
    void swipe_returnsEmpty_whenLikeIsNotMutual() {
        when(userService.getById(userA.getId())).thenReturn(userA);
        when(userService.getById(userB.getId())).thenReturn(userB);
        when(likeRepository.existsByLikerIdAndLikedId(userA.getId(), userB.getId())).thenReturn(false);
        when(likeRepository.existsByLikerIdAndLikedId(userB.getId(), userA.getId())).thenReturn(false);

        Optional<Match> result = matchService.swipe(userA.getId(), userB.getId());

        assertThat(result).isEmpty();
        verify(matchRaceSafeWriter).trySaveLike(any(Like.class));
        verify(swipeRateLimiter).consumeOrThrow(userA.getId());
    }

    @Test
    void swipe_propagatesRateLimitException_whenDailySwipeLimitExceeded() {
        when(userService.getById(userA.getId())).thenReturn(userA);
        when(userService.getById(userB.getId())).thenReturn(userB);
        when(likeRepository.existsByLikerIdAndLikedId(userA.getId(), userB.getId())).thenReturn(false);
        org.mockito.Mockito.doThrow(new com.wick.common.exception.TooManyRequestsException("limit"))
                .when(swipeRateLimiter).consumeOrThrow(userA.getId());

        assertThatThrownBy(() -> matchService.swipe(userA.getId(), userB.getId()))
                .isInstanceOf(com.wick.common.exception.TooManyRequestsException.class);

        verify(matchRaceSafeWriter, never()).trySaveLike(any());
    }

    @Test
    void swipe_doesNotConsumeRateLimit_whenAlreadySwipedOnTarget() {
        when(userService.getById(userA.getId())).thenReturn(userA);
        when(userService.getById(userB.getId())).thenReturn(userB);
        when(likeRepository.existsByLikerIdAndLikedId(userA.getId(), userB.getId())).thenReturn(true);
        when(likeRepository.existsByLikerIdAndLikedId(userB.getId(), userA.getId())).thenReturn(false);

        matchService.swipe(userA.getId(), userB.getId());

        verify(swipeRateLimiter, never()).consumeOrThrow(any());
    }

    @Test
    void swipe_createsMatch_whenLikeBecomesMutual() {
        when(userService.getById(userA.getId())).thenReturn(userA);
        when(userService.getById(userB.getId())).thenReturn(userB);
        when(likeRepository.existsByLikerIdAndLikedId(userA.getId(), userB.getId())).thenReturn(false);
        when(likeRepository.existsByLikerIdAndLikedId(userB.getId(), userA.getId())).thenReturn(true);
        when(matchRepository.findByUsers(userA.getId(), userB.getId())).thenReturn(Optional.empty());
        when(matchRaceSafeWriter.trySaveMatch(any(User.class), any(User.class)))
                .thenAnswer(invocation -> Optional.of(TestEntities.match(invocation.getArgument(0), invocation.getArgument(1))));

        Optional<Match> result = matchService.swipe(userA.getId(), userB.getId());

        assertThat(result).isPresent();
        assertThat(matchService.involves(result.get(), userA.getId())).isTrue();
        assertThat(matchService.involves(result.get(), userB.getId())).isTrue();
    }

    @Test
    void swipe_fallsBackToExistingMatch_whenMatchCreationRacesWithConcurrentSwipe() {
        Match createdByConcurrentRequest = TestEntities.match(userA, userB);
        when(userService.getById(userA.getId())).thenReturn(userA);
        when(userService.getById(userB.getId())).thenReturn(userB);
        when(likeRepository.existsByLikerIdAndLikedId(userA.getId(), userB.getId())).thenReturn(false);
        when(likeRepository.existsByLikerIdAndLikedId(userB.getId(), userA.getId())).thenReturn(true);
        when(matchRepository.findByUsers(userA.getId(), userB.getId()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(createdByConcurrentRequest));
        when(matchRaceSafeWriter.trySaveMatch(any(User.class), any(User.class))).thenReturn(Optional.empty());

        Optional<Match> result = matchService.swipe(userA.getId(), userB.getId());

        assertThat(result).contains(createdByConcurrentRequest);
    }

    @Test
    void swipe_reusesExistingMatch_whenAlreadyMatched() {
        Match existing = TestEntities.match(userA, userB);
        when(userService.getById(userA.getId())).thenReturn(userA);
        when(userService.getById(userB.getId())).thenReturn(userB);
        when(likeRepository.existsByLikerIdAndLikedId(userA.getId(), userB.getId())).thenReturn(true);
        when(likeRepository.existsByLikerIdAndLikedId(userB.getId(), userA.getId())).thenReturn(true);
        when(matchRepository.findByUsers(userA.getId(), userB.getId())).thenReturn(Optional.of(existing));

        Optional<Match> result = matchService.swipe(userA.getId(), userB.getId());

        assertThat(result).contains(existing);
    }

    @Test
    void swipe_throwsBadRequest_whenLikerProfileIsIncomplete() {
        User incompleteUser = TestEntities.user("c@example.com", "hash", "C");
        ReflectionTestUtils.setField(incompleteUser, "id", UUID.randomUUID());
        when(userService.getById(incompleteUser.getId())).thenReturn(incompleteUser);
        when(userService.getById(userB.getId())).thenReturn(userB);

        assertThatThrownBy(() -> matchService.swipe(incompleteUser.getId(), userB.getId()))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void getDiscoverableUsers_delegatesToUserService() {
        Page<User> page = new PageImpl<>(List.of(userB));
        when(userService.getDiscoverableUsers(userA.getId(), PageRequest.of(0, 20))).thenReturn(page);

        Page<User> result = matchService.getDiscoverableUsers(userA.getId(), PageRequest.of(0, 20));

        assertThat(result.getContent()).containsExactly(userB);
    }

    @Test
    void otherUser_returnsCounterpart() {
        Match match = TestEntities.match(userA, userB);

        assertThat(matchService.otherUser(match, userA.getId())).isEqualTo(userB);
        assertThat(matchService.otherUser(match, userB.getId())).isEqualTo(userA);
    }
}
