package com.wick.safety.domain;

import com.wick.common.domain.BaseEntity;
import com.wick.user.domain.User;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "blocks", uniqueConstraints = @UniqueConstraint(columnNames = {"blocker_id", "blocked_id"}))
public class Block extends BaseEntity {

    @ManyToOne(optional = false)
    private User blocker;

    @ManyToOne(optional = false)
    private User blocked;
}
