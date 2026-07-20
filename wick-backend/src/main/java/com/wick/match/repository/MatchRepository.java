package com.wick.match.repository;

import com.wick.match.domain.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {

    @Query("select m from Match m where (m.userOne.id = :userA and m.userTwo.id = :userB) "
            + "or (m.userOne.id = :userB and m.userTwo.id = :userA)")
    Optional<Match> findByUsers(@Param("userA") UUID userA, @Param("userB") UUID userB);

    @Query("select m from Match m where m.userOne.id = :userId or m.userTwo.id = :userId")
    List<Match> findAllForUser(@Param("userId") UUID userId);

    @Query("select m from Match m where m.userOne.id = :userId or m.userTwo.id = :userId")
    Page<Match> findAllForUser(@Param("userId") UUID userId, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query(value = "insert into matches (id, user_one_id, user_two_id, created_at, updated_at) "
            + "values (:id, :userOneId, :userTwoId, now(), now()) "
            + "on conflict (user_one_id, user_two_id) do nothing", nativeQuery = true)
    int insertIfAbsent(@Param("id") UUID id, @Param("userOneId") UUID userOneId, @Param("userTwoId") UUID userTwoId);
}
