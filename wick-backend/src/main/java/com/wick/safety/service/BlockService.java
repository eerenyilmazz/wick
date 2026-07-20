package com.wick.safety.service;

import java.util.UUID;

public interface BlockService {

    void block(UUID blockerId, UUID blockedId);

    void unblock(UUID blockerId, UUID blockedId);

    boolean isBlockedEitherDirection(UUID userA, UUID userB);
}
