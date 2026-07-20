package com.wick.notification.domain;

import com.wick.common.domain.BaseEntity;
import com.wick.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "notification_preferences")
public class NotificationPreferences extends BaseEntity {

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(nullable = false)
    private boolean newMatches = true;

    @Column(nullable = false)
    private boolean invites = true;

    @Column(nullable = false)
    private boolean messages = true;

    @Column(nullable = false)
    private boolean reminders = false;

    @Column(nullable = false)
    private boolean promos = false;
}
