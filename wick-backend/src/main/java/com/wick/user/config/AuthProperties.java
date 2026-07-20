package com.wick.user.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    private int loginAttemptLimit;
    private int loginLimitWindowMinutes;
    private int registerAttemptLimit;
    private int registerLimitWindowMinutes;
    private int emailCheckAttemptLimit;
    private int emailCheckLimitWindowMinutes;
}
