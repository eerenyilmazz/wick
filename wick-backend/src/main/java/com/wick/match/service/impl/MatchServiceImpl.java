package com.wick.match.service.impl;

import com.wick.common.exception.ApiException;
import com.wick.common.exception.ResourceNotFoundException;
import com.wick.eventlog.domain.EventType;
import com.wick.eventlog.service.EventLogService;
import com.wick.match.domain.Like;
import com.wick.match.domain.Match;
import com.wick.match.domain.SwipeDirection;
import com.wick.match.repository.LikeRepository;
import com.wick.match.repository.MatchRepository;
import com.wick.match.repository.PassRepository;
import com.wick.invitation.repository.InvitationRepository;
import com.wick.match.service.MatchService;
import com.wick.match.service.SwipeRateLimiter;
import com.wick.notification.service.NotificationService;
import com.wick.safety.service.BlockService;
import com.wick.session.domain.Session;
import com.wick.session.repository.MessageRepository;
import com.wick.session.repository.SessionRepository;
import com.wick.token.config.TokenProperties;
import com.wick.token.domain.TokenReason;
import com.wick.token.service.TokenService;
import com.wick.user.domain.User;
import com.wick.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final LikeRepository likeRepository;
    private final PassRepository passRepository;
    private final MatchRepository matchRepository;
    private final UserService userService;
    private final MatchRaceSafeWriter matchRaceSafeWriter;
    private final EventLogService eventLogService;
    private final NotificationService notificationService;
    private final BlockService blockService;
    private final SwipeRateLimiter swipeRateLimiter;
    private final InvitationRepository invitationRepository;
    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final TokenService tokenService;
    private final TokenProperties tokenProperties;

    @Override
    @Transactional
    public Optional<Match> swipe(UUID likerId, UUID targetUserId) {
        return swipe(likerId, targetUserId, SwipeDirection.LIKE);
    }

    @Override
    @Transactional
    public Optional<Match> swipe(UUID likerId, UUID targetUserId, SwipeDirection direction) {
        if (likerId.equals(targetUserId)) {
            throw new ApiException("Cannot swipe on yourself", HttpStatus.BAD_REQUEST);
        }
        if (blockService.isBlockedEitherDirection(likerId, targetUserId)) {
            throw new ApiException("You cannot interact with this user", HttpStatus.FORBIDDEN);
        }
        User liker = userService.getById(likerId);
        User target = userService.getById(targetUserId);

        if (liker.getDateOfBirth() == null || liker.getGender() == null) {
            throw new ApiException("Complete your profile (date of birth and gender) before you can start matching", HttpStatus.BAD_REQUEST);
        }

        if (direction == SwipeDirection.PASS) {
            if (!passRepository.existsByPasserIdAndPassedId(likerId, targetUserId)) {
                matchRaceSafeWriter.trySavePass(liker, target);
            }
            return Optional.empty();
        }

        if (!likeRepository.existsByLikerIdAndLikedId(likerId, targetUserId)) {
            swipeRateLimiter.consumeOrThrow(likerId);
            if (direction == SwipeDirection.SUPER_LIKE) {
                tokenService.spend(likerId, tokenProperties.getSuperLikeCost(), TokenReason.SUPER_LIKE);
                notificationService.notifySuperLike(target, liker);
            }
            Like like = new Like();
            like.setLiker(liker);
            like.setLiked(target);
            matchRaceSafeWriter.trySaveLike(like);
        }

        boolean mutual = likeRepository.existsByLikerIdAndLikedId(targetUserId, likerId);
        if (!mutual) {
            return Optional.empty();
        }

        return Optional.of(findOrCreateMatch(liker, target));
    }

    private Match findOrCreateMatch(User userA, User userB) {
        Optional<Match> existing = matchRepository.findByUsers(userA.getId(), userB.getId());
        if (existing.isPresent()) {
            return existing.get();
        }

        User first = userA.getId().compareTo(userB.getId()) <= 0 ? userA : userB;
        User second = first == userA ? userB : userA;
        Optional<Match> created = matchRaceSafeWriter.trySaveMatch(first, second);
        if (created.isPresent()) {
            Match match = created.get();
            eventLogService.log(EventType.MATCH_CREATED, match.getId(), userA.getId() + " <-> " + userB.getId());
            notificationService.notifyMatchCreated(userA, userB);
            return match;
        }
        return matchRepository.findByUsers(userA.getId(), userB.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));
    }

    @Override
    public Page<User> getDiscoverableUsers(UUID userId, Pageable pageable) {
        return userService.getDiscoverableUsers(userId, pageable);
    }

    @Override
    public Page<Match> getMatchesForUser(UUID userId, Pageable pageable) {
        return matchRepository.findAllForUser(userId, pageable);
    }

    @Override
    public List<Match> getAllMatchesForUser(UUID userId) {
        return matchRepository.findAllForUser(userId);
    }

    @Override
    public Match getById(UUID matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));
    }

    @Override
    public boolean involves(Match match, UUID userId) {
        return match.getUserOne().getId().equals(userId) || match.getUserTwo().getId().equals(userId);
    }

    @Override
    public User otherUser(Match match, UUID userId) {
        return match.getUserOne().getId().equals(userId) ? match.getUserTwo() : match.getUserOne();
    }

    @Override
    @Transactional
    public void unmatch(UUID matchId, UUID userId) {
        Match match = getById(matchId);
        if (!involves(match, userId)) {
            throw new ApiException("You are not part of this match", HttpStatus.FORBIDDEN);
        }

        for (Session session : sessionRepository.findAllByMatchId(matchId)) {
            messageRepository.deleteAllBySessionId(session.getId());
            sessionRepository.delete(session);
        }
        invitationRepository.deleteAll(invitationRepository.findAllByMatchId(matchId));
        matchRepository.delete(match);
    }
}
