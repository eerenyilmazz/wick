package com.wick.photo.domain;

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
@Table(name = "user_photos")
public class UserPhoto extends BaseEntity {

    @ManyToOne(optional = false)
    private User user;

    @Column(nullable = false)
    private String storageKey;

    @Column(nullable = false)
    private int displayOrder;
}
