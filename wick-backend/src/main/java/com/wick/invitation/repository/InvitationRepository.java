package com.wick.invitation.repository;

import com.wick.invitation.domain.Invitation;
import com.wick.invitation.domain.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvitationRepository extends JpaRepository<Invitation, UUID> {

    Optional<Invitation> findByMatchIdAndStatus(UUID matchId, InvitationStatus status);

    long countByMatchIdAndSenderIdAndStatus(UUID matchId, UUID senderId, InvitationStatus status);

    List<Invitation> findAllBySenderId(UUID senderId);

    Optional<Invitation> findTopByMatchIdOrderByCreatedAtDesc(UUID matchId);

    List<Invitation> findAllByMatchId(UUID matchId);

    @Query("select i from Invitation i where i.status = 'PENDING' and i.sender.id <> :userId "
            + "and (i.match.userOne.id = :userId or i.match.userTwo.id = :userId)")
    List<Invitation> findIncomingPending(@Param("userId") UUID userId);
}
