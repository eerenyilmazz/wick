package com.wick.match.repository;

import com.wick.match.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface LikeRepository extends JpaRepository<Like, UUID> {

    boolean existsByLikerIdAndLikedId(UUID likerId, UUID likedId);

    @Modifying(clearAutomatically = true)
    @Query(value = "insert into likes (id, liker_id, liked_id, created_at, updated_at) "
            + "values (:id, :likerId, :likedId, now(), now()) "
            + "on conflict (liker_id, liked_id) do nothing", nativeQuery = true)
    int insertIfAbsent(@Param("id") UUID id, @Param("likerId") UUID likerId, @Param("likedId") UUID likedId);
}
