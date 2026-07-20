package com.wick.invitation.domain;

import com.wick.common.domain.BaseEntity;
import com.wick.match.domain.Match;
import com.wick.user.domain.User;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "invitations")
public class Invitation extends BaseEntity {

    @ManyToOne(optional = false)
    private Match match;

    @ManyToOne(optional = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    private InvitationStatus status = InvitationStatus.PENDING;

    private Instant respondedAt;
}
