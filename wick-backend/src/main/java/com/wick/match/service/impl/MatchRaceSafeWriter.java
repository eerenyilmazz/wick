package com.wick.match.service.impl;

import com.wick.match.domain.Like;
import com.wick.match.domain.Match;
import com.wick.match.repository.LikeRepository;
import com.wick.match.repository.MatchRepository;
import com.wick.match.repository.PassRepository;
import com.wick.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MatchRaceSafeWriter {

    private final LikeRepository likeRepository;
    private final PassRepository passRepository;
    private final MatchRepository matchRepository;

    @Transactional
    public void trySaveLike(Like like) {
        likeRepository.insertIfAbsent(UUID.randomUUID(), like.getLiker().getId(), like.getLiked().getId());
    }

    @Transactional
    public void trySavePass(User passer, User passed) {
        passRepository.insertIfAbsent(UUID.randomUUID(), passer.getId(), passed.getId());
    }

    @Transactional
    public Optional<Match> trySaveMatch(User first, User second) {
        UUID id = UUID.randomUUID();
        int inserted = matchRepository.insertIfAbsent(id, first.getId(), second.getId());
        if (inserted == 0) {
            return Optional.empty();
        }
        return matchRepository.findById(id);
    }
}
