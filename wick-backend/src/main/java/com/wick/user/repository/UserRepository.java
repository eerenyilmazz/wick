package com.wick.user.repository;

import com.wick.user.domain.User;
import com.wick.user.domain.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findAllByDeletedAtIsNotNullAndDeletedAtBefore(Instant cutoff);

    List<User> findAllByVerificationStatus(VerificationStatus status);

    List<User> findAllByVerificationStatusAndVerificationSubmittedAtBefore(VerificationStatus status, Instant cutoff);

    @Query(
            value = "select u.* from users u where u.id <> :userId "
                    + "and u.deleted_at is null "
                    + "and u.date_of_birth is not null and u.gender is not null "
                    + "and u.province is not null and u.district is not null "
                    + "and u.bio is not null and u.bio <> '' "
                    + "and exists (select 1 from user_photos ph where ph.user_id = u.id) "
                    + "and u.id not in (select l.liked_id from likes l where l.liker_id = :userId) "
                    + "and u.id not in (select p.passed_id from passes p where p.passer_id = :userId) "
                    + "and u.id not in (select b.blocked_id from blocks b where b.blocker_id = :userId) "
                    + "and u.id not in (select b.blocker_id from blocks b where b.blocked_id = :userId) "
                    + "and u.date_of_birth <= :maxBirthDate and u.date_of_birth >= :minBirthDate "
                    + "and u.gender in (:genders) "
                    + "and (:radiusKm is null or :viewerLat is null or :viewerLng is null or ("
                    + "  u.latitude is not null and u.longitude is not null and "
                    + "  6371 * acos(greatest(-1, least(1, "
                    + "    cos(radians(:viewerLat)) * cos(radians(u.latitude)) * cos(radians(u.longitude) - radians(:viewerLng)) "
                    + "    + sin(radians(:viewerLat)) * sin(radians(u.latitude))"
                    + "  ))) <= :radiusKm"
                    + "))",
            countQuery = "select count(u.id) from users u where u.id <> :userId "
                    + "and u.deleted_at is null "
                    + "and u.date_of_birth is not null and u.gender is not null "
                    + "and u.province is not null and u.district is not null "
                    + "and u.bio is not null and u.bio <> '' "
                    + "and exists (select 1 from user_photos ph where ph.user_id = u.id) "
                    + "and u.id not in (select l.liked_id from likes l where l.liker_id = :userId) "
                    + "and u.id not in (select p.passed_id from passes p where p.passer_id = :userId) "
                    + "and u.id not in (select b.blocked_id from blocks b where b.blocker_id = :userId) "
                    + "and u.id not in (select b.blocker_id from blocks b where b.blocked_id = :userId) "
                    + "and u.date_of_birth <= :maxBirthDate and u.date_of_birth >= :minBirthDate "
                    + "and u.gender in (:genders) "
                    + "and (:radiusKm is null or :viewerLat is null or :viewerLng is null or ("
                    + "  u.latitude is not null and u.longitude is not null and "
                    + "  6371 * acos(greatest(-1, least(1, "
                    + "    cos(radians(:viewerLat)) * cos(radians(u.latitude)) * cos(radians(u.longitude) - radians(:viewerLng)) "
                    + "    + sin(radians(:viewerLat)) * sin(radians(u.latitude))"
                    + "  ))) <= :radiusKm"
                    + "))",
            nativeQuery = true)
    Page<User> findDiscoverableUsers(@Param("userId") UUID userId,
                                      @Param("maxBirthDate") LocalDate maxBirthDate,
                                      @Param("minBirthDate") LocalDate minBirthDate,
                                      @Param("genders") List<String> genders,
                                      @Param("radiusKm") Integer radiusKm,
                                      @Param("viewerLat") Double viewerLat,
                                      @Param("viewerLng") Double viewerLng,
                                      Pageable pageable);
}
