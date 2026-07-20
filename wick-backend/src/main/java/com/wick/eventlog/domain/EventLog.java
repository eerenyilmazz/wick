package com.wick.eventlog.domain;

import com.wick.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "event_logs")
public class EventLog extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    private UUID userId;

    @Column(columnDefinition = "text")
    private String detail;
}
