package com.wick.payment.domain;

import com.wick.common.domain.BaseEntity;
import com.wick.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "purchases", uniqueConstraints = @UniqueConstraint(columnNames = "purchase_token"))
public class Purchase extends BaseEntity {

    @ManyToOne(optional = false)
    private User user;

    @Column(nullable = false)
    private String productId;

    @Column(name = "purchase_token", nullable = false)
    private String purchaseToken;
}
