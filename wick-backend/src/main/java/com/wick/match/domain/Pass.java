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
@Table(name = "passes", uniqueConstraints = @UniqueConstraint(columnNames = {"passer_id", "passed_id"}))
public class Pass extends BaseEntity {

    @ManyToOne(optional = false)
    private User passer;

    @ManyToOne(optional = false)
    private User passed;
}
