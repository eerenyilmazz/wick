package com.wick.safety.service.impl;

import com.wick.common.exception.ApiException;
import com.wick.match.repository.MatchRepository;
import com.wick.safety.repository.BlockRepository;
import com.wick.safety.service.BlockService;
import com.wick.session.domain.SessionStatus;
import com.wick.session.repository.SessionRepository;
import com.wick.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {

    private final BlockRepository blockRepository;
    private final UserService userService;
    private final MatchRepository matchRepository;
    private final SessionRepository sessionRepository;

    @Override
    @Transactional
    public void block(UUID blockerId, UUID blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new ApiException("Cannot block yourself", HttpStatus.BAD_REQUEST);
        }
        userService.getById(blockedId);
        blockRepository.insertIfAbsent(UUID.randomUUID(), blockerId, blockedId);
        matchRepository.findByUsers(blockerId, blockedId).ifPresent(match ->
            sessionRepository.findByMatchIdAndStatus(match.getId(), SessionStatus.ACTIVE)
                .ifPresent(session -> sessionRepository.lockIfActive(session.getId()))
        );
    }

    @Override
    @Transactional
    public void unblock(UUID blockerId, UUID blockedId) {
        blockRepository.deleteByBlockerIdAndBlockedId(blockerId, blockedId);
    }

    @Override
    public boolean isBlockedEitherDirection(UUID userA, UUID userB) {
        return blockRepository.existsEitherDirection(userA, userB);
    }
}
