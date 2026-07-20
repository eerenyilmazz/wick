package com.wick.safety.repository;

import com.wick.safety.domain.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface BlockRepository extends JpaRepository<Block, UUID> {

    boolean existsByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId);

    @Query("select case when count(b) > 0 then true else false end from Block b "
            + "where (b.blocker.id = :userA and b.blocked.id = :userB) "
            + "or (b.blocker.id = :userB and b.blocked.id = :userA)")
    boolean existsEitherDirection(@Param("userA") UUID userA, @Param("userB") UUID userB);

    @Modifying
    long deleteByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId);

    @Modifying(clearAutomatically = true)
    @Query(value = "insert into blocks (id, blocker_id, blocked_id, created_at, updated_at) "
            + "values (:id, :blockerId, :blockedId, now(), now()) "
            + "on conflict (blocker_id, blocked_id) do nothing", nativeQuery = true)
    int insertIfAbsent(@Param("id") UUID id, @Param("blockerId") UUID blockerId, @Param("blockedId") UUID blockedId);
}
