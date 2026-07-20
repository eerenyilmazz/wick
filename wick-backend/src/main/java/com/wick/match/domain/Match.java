package com.wick.match.domain;

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
@Table(name = "matches", uniqueConstraints = @UniqueConstraint(columnNames = {"user_one_id", "user_two_id"}))
public class Match extends BaseEntity {

    @ManyToOne(optional = false)
    private User userOne;

    @ManyToOne(optional = false)
    private User userTwo;
}
