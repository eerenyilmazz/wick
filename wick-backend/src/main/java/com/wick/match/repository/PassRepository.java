package com.wick.match.repository;

import com.wick.match.domain.Pass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PassRepository extends JpaRepository<Pass, UUID> {

    boolean existsByPasserIdAndPassedId(UUID passerId, UUID passedId);

    @Modifying(clearAutomatically = true)
    @Query(value = "insert into passes (id, passer_id, passed_id, created_at, updated_at) "
            + "values (:id, :passerId, :passedId, now(), now()) "
            + "on conflict (passer_id, passed_id) do nothing", nativeQuery = true)
    int insertIfAbsent(@Param("id") UUID id, @Param("passerId") UUID passerId, @Param("passedId") UUID passedId);
}
