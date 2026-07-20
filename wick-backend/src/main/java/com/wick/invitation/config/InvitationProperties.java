package com.wick.invitation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.invitation")
public class InvitationProperties {

    private int dailyLimit;
    private int limitWindowHours;
    private int maxRetryInvitations;
}
