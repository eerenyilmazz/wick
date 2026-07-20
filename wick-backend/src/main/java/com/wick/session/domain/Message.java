package com.wick.session.domain;

import com.wick.common.domain.BaseEntity;
import com.wick.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "messages")
public class Message extends BaseEntity {

    @ManyToOne(optional = false)
    private Session session;

    @ManyToOne(optional = false)
    private User sender;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(unique = true)
    private String clientMessageId;
}
