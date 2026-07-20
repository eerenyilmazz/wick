package com.wick.session.domain;

import com.wick.common.domain.BaseEntity;
import com.wick.match.domain.Match;
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
@Table(name = "chat_sessions")
public class Session extends BaseEntity {

    @ManyToOne(optional = false)
    private Match match;

    private Instant startedAt;

    private Instant endsAt;

    @Enumerated(EnumType.STRING)
    private SessionStatus status = SessionStatus.ACTIVE;
}
