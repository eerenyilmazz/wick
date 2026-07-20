package com.wick.safety.service.impl;

import com.wick.testsupport.TestEntities;

import com.wick.common.exception.ApiException;
import com.wick.safety.repository.BlockRepository;
import com.wick.user.domain.User;
import com.wick.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockServiceImplTest {

    @Mock
    private BlockRepository blockRepository;

    @Mock
    private UserService userService;

    private BlockServiceImpl blockService;

    private UUID blockerId;
    private UUID blockedId;

    @BeforeEach
    void setUp() {
        blockService = new BlockServiceImpl(blockRepository, userService);
        blockerId = UUID.randomUUID();
        blockedId = UUID.randomUUID();
    }

    @Test
    void block_throwsBadRequest_whenBlockingSelf() {
        assertThatThrownBy(() -> blockService.block(blockerId, blockerId))
                .isInstanceOf(ApiException.class);

        verify(blockRepository, never()).insertIfAbsent(any(), any(), any());
    }

    @Test
    void block_insertsBlockRow_whenValid() {
        when(userService.getById(blockedId)).thenReturn(TestEntities.user("b@example.com", "hash", "B"));

        blockService.block(blockerId, blockedId);

        verify(blockRepository).insertIfAbsent(any(UUID.class), eq(blockerId), eq(blockedId));
    }

    @Test
    void unblock_deletesBlockRow() {
        blockService.unblock(blockerId, blockedId);

        verify(blockRepository).deleteByBlockerIdAndBlockedId(blockerId, blockedId);
    }

    @Test
    void isBlockedEitherDirection_delegatesToRepository() {
        when(blockRepository.existsEitherDirection(blockerId, blockedId)).thenReturn(true);

        assertThat(blockService.isBlockedEitherDirection(blockerId, blockedId)).isTrue();
    }
}
