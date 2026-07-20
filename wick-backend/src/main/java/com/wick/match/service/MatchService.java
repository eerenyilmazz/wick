package com.wick.match.service;

import com.wick.match.domain.Match;
import com.wick.match.domain.SwipeDirection;
import com.wick.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchService {

    Optional<Match> swipe(UUID likerId, UUID targetUserId);

    Optional<Match> swipe(UUID likerId, UUID targetUserId, SwipeDirection direction);

    Page<User> getDiscoverableUsers(UUID userId, Pageable pageable);

    Page<Match> getMatchesForUser(UUID userId, Pageable pageable);

    List<Match> getAllMatchesForUser(UUID userId);

    Match getById(UUID matchId);

    boolean involves(Match match, UUID userId);

    User otherUser(Match match, UUID userId);

    void unmatch(UUID matchId, UUID userId);
}
