package com.wick.token.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.token")
public class TokenProperties {

    private int sessionExtensionCost;
    private int extraInvitationCost;
    private int superLikeCost;
    private int premiumMonthlyBonus;
    private int dailyLoginClaimTtlHours;
}
