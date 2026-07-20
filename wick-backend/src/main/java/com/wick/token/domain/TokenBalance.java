package com.wick.token.domain;

import com.wick.common.domain.BaseEntity;
import com.wick.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "token_balances", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
public class TokenBalance extends BaseEntity {

    @OneToOne(optional = false)
    private User user;

    @Column(nullable = false)
    private int balance = 0;
}
