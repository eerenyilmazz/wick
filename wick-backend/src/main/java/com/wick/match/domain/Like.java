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
@Table(name = "likes", uniqueConstraints = @UniqueConstraint(columnNames = {"liker_id", "liked_id"}))
public class Like extends BaseEntity {

    @ManyToOne(optional = false)
    private User liker;

    @ManyToOne(optional = false)
    private User liked;
}
